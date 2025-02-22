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
import com.nukateam.ntgl.common.util.interfaces.IConfigProvider;
import com.nukateam.ntgl.common.util.util.AnimationHelper;
import com.nukateam.ntgl.common.util.util.Cycler;
import com.nukateam.ntgl.common.util.util.GunModifierHelper;
import com.nukateam.ntgl.common.foundation.item.GunItem;
import com.nukateam.ntgl.common.util.helpers.PlayerHelper;
import mod.azure.azurelib.core.animation.Animation;
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
import org.jetbrains.annotations.NotNull;

import static com.nukateam.example.common.util.constants.Animations.*;
import static com.nukateam.ntgl.client.util.util.TransformUtils.*;
import static mod.azure.azurelib.core.animation.AnimatableManager.ControllerRegistrar;
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

    protected Cycler barrelCycler = new Cycler(1, getBarrelAmount());
    protected Cycler chamberCycler = null;

    private GunItem currentGun = null;

    public GunAnimator(ItemDisplayContext transformType, DynamicGeoItemRenderer<GunAnimator> renderer) {
        super(transformType);
        this.renderer = (DynamicGunRenderer<GunAnimator>) renderer;
        ClientTickHandler.addTicker(this, this::tick);
        TRIGGER_CONTROLLER = createController( "triggerController", event -> PlayState.CONTINUE);
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
        if(getStack().getItem() instanceof IConfigProvider config) {
            if(config.getConfig() instanceof Gun gun)
                return gun;
        }

        return new Gun();
    }

    public void tick(){
        if (!(getStack().getItem() instanceof GunItem))
            return;
//
//        var itemInHandRenderer = minecraft.gameRenderer.itemInHandRenderer;
//
//        if (reloadHandler.isReloading(getEntity(), HumanoidArm.LEFT))
//            itemInHandRenderer.mainHandHeight = 0;

        setupCycledAnimations();
    }

    private void setupCycledAnimations() {
        var entity = getEntity();
        var arm = isRightHand(transformType) ? HumanoidArm.RIGHT : HumanoidArm.LEFT;
        var cooldown = shootingHandler.getCooldown(entity, arm);
        var rate = GunModifierHelper.getRate(getStack());

        if(chamberCycler == null) chamberCycler = new Cycler(1, GunModifierHelper.getMaxAmmo(getStack()));

        if(cooldown == rate){
            barrelCycler.cycle();
            chamberCycler.cycle();
        }
    }

    protected int getBarrelAmount(){
        return 1;
    }

    protected LivingEntity getEntity() {
        return renderer.getRenderEntity();
    }

    protected GunItem getGunItem() {
        return (GunItem) getStack().getItem();
    }

    protected boolean isOneHanded(ItemStack stack){
        return stack.getItem() instanceof GunItem
                && GunModifierHelper.getGripType(stack) == GripType.ONE_HANDED;
    }

    @NotNull
    protected AnimationController<GunAnimator> createController(String name, AnimationStateHandler<GunAnimator> animate) {
        return new AnimationController<>(this, name, 0, animate);
    }

    protected HumanoidArm getArm(){
        return isRightHand(transformType) ? HumanoidArm.RIGHT : HumanoidArm.LEFT;
    }

    int delay = 0;

    protected AnimationStateHandler<GunAnimator> animate() {
        return event -> {
            try {
                var controller = event.getController();
                controller.setAnimationSpeed(1);
                var entity = getEntity();
                var holdAnimation = playGunAnim(HOLD, LOOP);

                if (!isHandTransform(transformType))
                    return event.setAndContinue(holdAnimation);

                var arm = getArm();
                var isShooting = shootingHandler.isShooting(entity, arm);
                var data = shootingHandler.getShootingData(arm);
                var fireTimer = GunModifierHelper.getFireDelay(getStack());
                var animation = begin();

                if(fireTimer > 0 && data.fireTimer > 0 && fireTimer != data.fireTimer){
                    animation = getChargingAnimation(event, data);
                } else if (reloadHandler.isReloading(entity, arm) && isFirstPerson(transformType)) {
                    animation = getReloadingAnimation(event);
                } else if (isShooting) {
                    animation = getShootingAnimation(event);
                } else if (reloadHandler.isReloading(entity, arm.getOpposite()) && isFirstPerson(transformType)) {
                    animation = begin().then(com.nukateam.ntgl.common.data.constants.Animation.HIDE, HOLD_ON_LAST_FRAME);
                }
                else if(ClientHandler.getInspectionTicks(getArm()) > 0){
                    animation = getInspectionAnimation(event);
                }
                else {
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

    protected AnimationStateHandler<GunAnimator> animateRevolver() {
        return (event) -> getCycledAnimation(event, com.nukateam.ntgl.common.data.constants.Animation.CHAMBER, this.chamberCycler);
    }

    protected AnimationStateHandler<GunAnimator> animateBarrels() {
        return (event) -> getCycledAnimation(event, com.nukateam.ntgl.common.data.constants.Animation.BARREL, this.barrelCycler);
    }

    protected PlayState getCycledAnimation(AnimationState<GunAnimator> event, String animationName, Cycler cycler) {
        event.getController().setAnimationSpeed(1.0);

        if (TransformUtils.isHandTransform(this.transformType) && cycler != null) {
            var entity = this.getEntity();
            var isShooting = shootingHandler.isShooting(entity, TransformUtils.getHand(this.transformType));
            var rate = GunModifierHelper.getRate(this.getStack());
            var finalAnim = animationName + cycler.getCurrent();

            RawAnimation animation = null;
            if (isShooting && this.animationHelper.hasAnimation(finalAnim)) {
                animation = RawAnimation.begin().then(finalAnim, Animation.LoopType.HOLD_ON_LAST_FRAME);
                this.animationHelper.syncAnimation(event, finalAnim, rate);
            }

            return event.setAndContinue(animation);
        }
        return PlayState.STOP;
    }

    protected RawAnimation getInspectionAnimation(AnimationState<GunAnimator> event) {
        RawAnimation animation;
        animation = playGunAnim(com.nukateam.ntgl.common.data.constants.Animation.INSPECT, PLAY_ONCE);
        animationHelper.syncAnimation(event, com.nukateam.ntgl.common.data.constants.Animation.INSPECT, ClientHandler.getMaxInspectionTicks());
        return animation;
    }

    protected RawAnimation getChargingAnimation(AnimationState<GunAnimator> event , ShootingData data) {
        var animation = begin();
        var fireTimer = GunModifierHelper.getFireDelay(getStack());
        var speed = 1 - ((float) data.fireTimer / (float) fireTimer);
        var controller = event.getController();
        controller.setAnimationSpeed(speed);

        if(animationHelper.hasAnimation(com.nukateam.ntgl.common.data.constants.Animation.CHARGE)) {
            BARREL_CONTROLLER.stop();
            BARREL_CONTROLLER.setAnimation(begin().then("void", PLAY_ONCE));
            animation = playGunAnim(com.nukateam.ntgl.common.data.constants.Animation.CHARGE, LOOP);
        }
        return animation;
    }

    protected RawAnimation getShootingAnimation(AnimationState<GunAnimator> event) {
        var animation = playGunAnim(SHOT, LOOP);
        var rate = GunModifierHelper.getRate(getStack());
        animationHelper.syncAnimation(event, SHOT, rate);
        return animation;
    }

    protected RawAnimation getReloadingAnimation(AnimationState<GunAnimator> event) {
        var animation = begin();

        if(animationHelper.containsAnimation(com.nukateam.ntgl.common.data.constants.Animation.RELOAD_START))
            animation.then(com.nukateam.ntgl.common.data.constants.Animation.RELOAD_START, PLAY_ONCE);

        animation.then(RELOAD, LOOP);

        if(animationHelper.containsAnimation(com.nukateam.ntgl.common.data.constants.Animation.RELOAD_END))
            animation.then(com.nukateam.ntgl.common.data.constants.Animation.RELOAD_END, PLAY_ONCE);

        if(event.getController().getCurrentAnimation().animation().name().equals(RELOAD))
            animationHelper.syncAnimation(event, RELOAD, GunModifierHelper.getReloadTime(getStack()));
        return animation;
    }

    protected void handleSoundEvent(SoundKeyframeEvent<GunAnimator> event) {
        var player = minecraft.player;
        var name = event.getKeyframeData().getSound();
        var sounds = getGunItem().getGun().getSoundsMap();
        var sound = sounds.get(name);

        if(sound != null && player != null){
            minecraft.getSoundManager().play(new GunShotSound(sound, SoundSource.PLAYERS,
                    player.position(), 1, 1, true));
        }
    }

    protected RawAnimation playGunAnim(String name, Animation.LoopType loopType){
        var arm = isRightHand(transformType) ? HumanoidArm.RIGHT : HumanoidArm.LEFT;
        var entity = getEntity();
        var currentItem = entity.getItemInHand(PlayerHelper.convertHand(arm));
        var oppositeItem = entity.getItemInHand(PlayerHelper.convertHand(arm.getOpposite()));
        var isOneHanded = isOneHanded(currentItem) && isOneHanded(oppositeItem) || arm == HumanoidArm.LEFT;

        if(isOneHanded && animationHelper.hasAnimation(name + com.nukateam.ntgl.common.data.constants.Animation.ONE_HAND_SUFFIX))
            return begin().then(name + com.nukateam.ntgl.common.data.constants.Animation.ONE_HAND_SUFFIX, loopType);
        return begin().then(name, loopType);
    }

//    @NotNull
//    private RawAnimation getShootingAnimation(AnimationState<GunAnimator> event, ItemStack stack) {
//        RawAnimation animation;
//        animation = begin();
//
//        if(animationHelper.containsAnimation(SHOT_START))
//            animation.then(SHOT_START, PLAY_ONCE);
//
//        animation.then(SHOT, LOOP);
//
//        if(animationHelper.containsAnimation(SHOT_END))
//            animation.then(SHOT_END, PLAY_ONCE);
//
//        if(event.getController().getCurrentAnimation().animation().name().equals(RELOAD))
//            animationHelper.syncAnimation(event, SHOT, GunModifierHelper.getReloadTime(stack));
//        return animation;
//    }
}
