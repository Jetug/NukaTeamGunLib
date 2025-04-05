package com.nukateam.ntgl.client.animators;

import com.nukateam.geo.render.DynamicGeoItemRenderer;
import com.nukateam.geo.render.ItemAnimator;
import com.nukateam.ntgl.client.event.ClientHandler;
import com.nukateam.ntgl.client.audio.GunShotSound;
import com.nukateam.ntgl.client.event.ClientTickHandler;
import com.nukateam.ntgl.client.util.handler.ClientReloadHandler;
import com.nukateam.ntgl.client.util.handler.ShootingData;
import com.nukateam.ntgl.client.util.handler.ShootingHandler;
import com.nukateam.ntgl.client.model.gun.GeoGunModel;
import com.nukateam.ntgl.client.render.renderers.gun.DynamicGunRenderer;
import com.nukateam.ntgl.client.util.util.TransformUtils;
import com.nukateam.ntgl.common.data.config.gun.Gun;
import com.nukateam.ntgl.common.base.holders.GripType;
import com.nukateam.ntgl.common.data.constants.Animations;
import com.nukateam.ntgl.common.foundation.init.ModSyncedDataKeys;
import com.nukateam.ntgl.common.util.interfaces.IConfigProvider;
import com.nukateam.ntgl.common.util.util.AnimationHelper;
import com.nukateam.ntgl.common.util.util.Cycler;
import com.nukateam.ntgl.common.util.util.GunModifierHelper;
import com.nukateam.ntgl.common.foundation.item.GunItem;
import com.nukateam.ntgl.common.util.helpers.PlayerHelper;
import mod.azure.azurelib.core.animation.AnimationController;
import mod.azure.azurelib.core.animation.AnimationController.AnimationStateHandler;
import mod.azure.azurelib.core.animation.AnimationState;
import mod.azure.azurelib.core.animation.RawAnimation;
import mod.azure.azurelib.core.keyframe.event.SoundKeyframeEvent;
import mod.azure.azurelib.core.object.PlayState;
import net.minecraft.client.Minecraft;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.event.TickEvent;
import org.jetbrains.annotations.NotNull;

import static com.nukateam.example.common.util.constants.Animations.*;
import static com.nukateam.ntgl.client.util.util.TransformUtils.*;
import static mod.azure.azurelib.core.animation.AnimatableManager.ControllerRegistrar;
import static mod.azure.azurelib.core.animation.Animation.*;
import static mod.azure.azurelib.core.animation.Animation.LoopType.*;
import static mod.azure.azurelib.core.animation.RawAnimation.begin;

@OnlyIn(Dist.CLIENT)
public class GunAnimator extends ItemAnimator implements IConfigProvider<Gun> {
    protected final DynamicGunRenderer<GunAnimator> renderer;
    protected final Minecraft minecraft = Minecraft.getInstance();
    protected final ShootingHandler shootingHandler = ShootingHandler.get();
    protected final ClientReloadHandler reloadHandler = ClientReloadHandler.get();
    protected final AnimationHelper<GunAnimator> animationHelper;

    protected final AnimationController<GunAnimator> TRIGGER_CONTROLLER;
    protected final AnimationController<GunAnimator> MAIN_CONTROLLER;
    protected final AnimationController<GunAnimator> REVOLVER_CONTROLLER;
    protected final AnimationController<GunAnimator> BARREL_CONTROLLER;
    protected final HumanoidArm arm;

    protected Cycler barrelCycler = new Cycler(1, getBarrelAmount());
    protected Cycler chamberCycler = null;

    protected GunItem currentGun = null;
    protected int rate;
    protected int fireDelay;

    public GunAnimator(ItemDisplayContext transformType, DynamicGeoItemRenderer<GunAnimator> renderer) {
        super(transformType);
        this.renderer = (DynamicGunRenderer<GunAnimator>) renderer;
        this.arm = getArm();

        ClientTickHandler.addTicker(this, this::tick);
        TRIGGER_CONTROLLER = createController("triggerController", event -> PlayState.CONTINUE);
        MAIN_CONTROLLER = createController("mainController", animate()).setSoundKeyframeHandler(this::handleSoundEvent);
        REVOLVER_CONTROLLER = createController("revolverController", animateRevolver());
        BARREL_CONTROLLER = createController("barrelController", animateBarrels());
        animationHelper = new AnimationHelper<>(this, GeoGunModel.INSTANCE);
    }

    @Override
    public void registerControllers(ControllerRegistrar controllerRegistrar) {
        controllerRegistrar.add(MAIN_CONTROLLER);
        controllerRegistrar.add(TRIGGER_CONTROLLER);
        controllerRegistrar.add(REVOLVER_CONTROLLER);
        controllerRegistrar.add(BARREL_CONTROLLER);
    }

    @Override
    public Gun getConfig() {
        if (getStack().getItem() instanceof IConfigProvider config) {
            if (config.getConfig() instanceof Gun gun)
                return gun;
        }

        return new Gun();
    }

    public void tick(TickEvent event) {
        if (event.phase == TickEvent.Phase.START) {
            tickStart();
        } else {
            tickEnd();
        }
    }

    protected void tickStart() {
        if (!(getStack().getItem() instanceof GunItem))
            return;

        this.rate = GunModifierHelper.getRate(getStack());
        this.fireDelay = GunModifierHelper.getFireDelay(getStack());

        setupCycledAnimations();
    }

    protected void tickEnd() {
    }

    protected int getBarrelAmount() {
        return 1;
    }

    protected LivingEntity getEntity() {
        return renderer.getRenderEntity();
    }

    protected GunItem getGunItem() {
        return (GunItem) getStack().getItem();
    }

    protected boolean isOneHanded(ItemStack stack) {
        return stack.getItem() instanceof GunItem
                && GunModifierHelper.getGripType(stack) == GripType.ONE_HANDED;
    }

    @NotNull
    protected AnimationController<GunAnimator> createController(String name, AnimationStateHandler<GunAnimator> animate) {
        return new AnimationController<>(this, name, 0, animate);
    }

    protected HumanoidArm getArm() {
        return isRightHand(transformType) ? HumanoidArm.RIGHT : HumanoidArm.LEFT;
    }

    protected AnimationStateHandler<GunAnimator> animate() {
        return event -> {
            try {
                var controller = event.getController();
                controller.setAnimationSpeed(1);
                var entity = getEntity();
                var holdAnimation = getHoldAnimation(event);

                if (!isHandTransform(transformType))
                    return event.setAndContinue(holdAnimation);

                var isShooting = shootingHandler.isShooting(entity, arm);
                var data = shootingHandler.getShootingData(arm);
                var animation = begin();

                if (fireDelay > 0 && data.fireTimer > 0 && fireDelay != data.fireTimer) {
                    animation = getChargingAnimation(event, data);
                } else if (reloadHandler.isReloading(entity, arm) && isFirstPerson(transformType)) {
                    animation = getReloadingAnimation(event);
                } else if (isShooting) {
                    animation = getShootingAnimation(event);
                } else if (reloadHandler.isReloading(entity, arm.getOpposite()) && isFirstPerson(transformType)) {
                    animation = getHideAnimation();
                } else if (ClientHandler.getInspectionTicks(getArm()) > 0) {
                    animation = getInspectionAnimation(event);
                } else {
                    if (currentGun == getGunItem())
                        animation = holdAnimation;
                    else {
                        currentGun = getGunItem();
                        animation = playGunAnim(SHOT, LOOP);
                    }
                }

                return event.setAndContinue(animation);
            } catch (Exception e) {
                return PlayState.STOP;
            }
        };
    }

    protected static @NotNull RawAnimation getHideAnimation() {
        return begin().then(Animations.HIDE, HOLD_ON_LAST_FRAME);
    }

    protected AnimationStateHandler<GunAnimator> animateRevolver() {
        return (event) -> getCycledAnimation(event, Animations.CHAMBER, this.chamberCycler);
    }

    protected AnimationStateHandler<GunAnimator> animateBarrels() {
        return (event) -> getCycledAnimation(event, Animations.BARREL, this.barrelCycler);
    }

    protected PlayState getCycledAnimation(AnimationState<GunAnimator> event, String animationName, Cycler cycler) {
        event.getController().setAnimationSpeed(1.0);

        if (TransformUtils.isHandTransform(this.transformType) && cycler != null) {
            var entity = this.getEntity();
            var isShooting = shootingHandler.isShooting(entity, TransformUtils.getHand(this.transformType));
            var finalAnim = animationName + cycler.getCurrent();

            RawAnimation animation = null;
            if (isShooting && this.animationHelper.hasAnimation(finalAnim)) {
                animation = RawAnimation.begin().then(finalAnim, LoopType.HOLD_ON_LAST_FRAME);
                this.animationHelper.syncAnimation(event, finalAnim, rate);
            }

            return event.setAndContinue(animation);
        }
        return PlayState.STOP;
    }

    protected RawAnimation getInspectionAnimation(AnimationState<GunAnimator> event) {
        RawAnimation animation;
        animation = playGunAnim(Animations.INSPECT, PLAY_ONCE);
        animationHelper.syncAnimation(event, Animations.INSPECT, ClientHandler.getMaxInspectionTicks());
        return animation;
    }

    protected RawAnimation getHoldAnimation(AnimationState<GunAnimator> event) {
        return playGunAnim(HOLD, LOOP);
    }

    protected RawAnimation getChargingAnimation(AnimationState<GunAnimator> event, ShootingData data) {
        var animation = begin();
        if (animationHelper.hasAnimation(Animations.CHARGE)) {
            BARREL_CONTROLLER.stop();
            BARREL_CONTROLLER.setAnimation(begin().then("void", PLAY_ONCE));
            animation = playGunAnim(Animations.CHARGE, LOOP);
            var fireDelay = GunModifierHelper.getFireDelay(getStack());
            animationHelper.syncAnimation(event, Animations.CHARGE, fireDelay);
        }
        return animation;
    }

    protected RawAnimation getShootingAnimation(AnimationState<GunAnimator> event) {
        var animation = playGunAnim(SHOT, LOOP);
        animationHelper.syncAnimation(event, SHOT, rate);
        return animation;
    }

    protected RawAnimation getReloadingAnimation(AnimationState<GunAnimator> event) {
        var animation = begin();

        if(ModSyncedDataKeys.RELOAD_START.getValue(getEntity())){
            animation = getStartReloadAnimation(event);
        }
        else if(ModSyncedDataKeys.RELOAD_END.getValue(getEntity())){
            animation = getEndReloadAnimation(event);
        }
        else {
            animation = getDefaultReloadAminmation(event);
        }

        return animation;
    }

    protected RawAnimation getDefaultReloadAminmation(AnimationState<GunAnimator> event) {
        var time = GunModifierHelper.getReloadTime(getStack());
        animationHelper.syncAnimation(event, RELOAD, time);
        return begin().then(RELOAD, LOOP);
    }

    protected RawAnimation getEndReloadAnimation(AnimationState<GunAnimator> event) {
        var time = GunModifierHelper.getReloadEnd(getStack());
        animationHelper.syncAnimation(event, Animations.RELOAD_END, time);
        return begin().then(Animations.RELOAD_END, PLAY_ONCE);
    }

    protected RawAnimation getStartReloadAnimation(AnimationState<GunAnimator> event) {
        var time = GunModifierHelper.getReloadStart(getStack());
        animationHelper.syncAnimation(event, Animations.RELOAD_START, time);
        return begin().then(Animations.RELOAD_START, PLAY_ONCE);
    }

    protected void handleSoundEvent(SoundKeyframeEvent<GunAnimator> event) {
        var player = minecraft.player;
        var name = event.getKeyframeData().getSound();
        var sounds = getGunItem().getGun().getSoundsMap();
        var sound = sounds.get(name);

        if (sound != null && player != null) {
            minecraft.getSoundManager().play(new GunShotSound(sound, SoundSource.PLAYERS,
                    player.position(), 1, 1, true));
        }
    }

    protected RawAnimation playGunAnim(String name, LoopType loopType) {
        return begin().then(getGunAnim(name), loopType);
    }

    protected String getGunAnim(String name){
        var entity = getEntity();
        var currentItem = entity.getItemInHand(PlayerHelper.convertHand(arm));
        var oppositeItem = entity.getItemInHand(PlayerHelper.convertHand(arm.getOpposite()));
        var isOneHanded = isOneHanded(currentItem) && isOneHanded(oppositeItem) || arm == HumanoidArm.LEFT;

        if (isOneHanded && animationHelper.hasAnimation(name + Animations.ONE_HAND_SUFFIX))
            return name + Animations.ONE_HAND_SUFFIX;
        return name;
    }

    private void setupCycledAnimations() {
        var entity = getEntity();
        var cooldown = shootingHandler.getCooldown(entity, arm);
        var maxAmmo = GunModifierHelper.getMaxAmmo(getStack());

        if (chamberCycler == null || chamberCycler.getMax() != maxAmmo)
            chamberCycler = new Cycler(1, maxAmmo);

        if (cooldown == rate) {
            barrelCycler.cycle();
            chamberCycler.cycle();
        }
    }
}
