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
    public static final String RELOAD_START = "reload_start";
    public static final String RELOAD_END = "reload_end";
    public static final String CHARGE = "charge";
    public static final String ONE_HAND_SUFFIX = "_one_hand";
    public static final String INSPECT = "inspect";
    private static final String SHOT_START = "shot_start";
    private static final String SHOT_END = "shot_end";
    public static final String HIDE = "hide";
    public static final String CHAMBER = "chamber";
    public static final String BARREL = "barrel";
    private GunItem currentGun = null;
    protected final Minecraft minecraft = Minecraft.getInstance();
    protected final DynamicGunRenderer<GunAnimator> renderer;
    protected Cycler barrelCycler = new Cycler(1, getBarrelAmount());
    protected Cycler chamberCycler = null;

    protected AnimationHelper<GunAnimator> animationHelper = new AnimationHelper<>(this, GeoGunModel.INSTANCE);

    protected final AnimationController<GunAnimator> TRIGGER_CONTROLLER = createController( "triggerController", event -> PlayState.CONTINUE);
    protected final AnimationController<GunAnimator> MAIN_CONTROLLER = createController("mainController", animate()).setSoundKeyframeHandler(this::handleSoundEvent);
    protected final AnimationController<GunAnimator> REVOLVER_CONTROLLER = createController("revolverController", animateRevolver());
    protected final AnimationController<GunAnimator> BARREL_CONTROLLER = createController("barrelController", animateBarrels());

    public GunAnimator(ItemDisplayContext transformType, DynamicGeoItemRenderer<GunAnimator> renderer) {
        super(transformType);
        this.renderer = (DynamicGunRenderer<GunAnimator>) renderer;
        ClientTickHandler.addTicker(this, this::tick);
    }

    @NotNull
    private AnimationController<GunAnimator> createController(String name, AnimationController.AnimationStateHandler<GunAnimator> animate) {
        return new AnimationController<>(this, name, 0, animate);
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

    protected int getBarrelAmount(){
        return 1;
    }

    public void tick(){
        if (!(getStack().getItem() instanceof GunItem))
            return;

        var entity = getEntity();
        var arm = isRightHand(transformType) ? HumanoidArm.RIGHT : HumanoidArm.LEFT;
        var cooldown = ShootingHandler.get().getCooldown(entity, arm);
        var rate = GunModifierHelper.getRate(getStack());

        if(chamberCycler == null) chamberCycler = new Cycler(1, GunModifierHelper.getMaxAmmo(getStack()));

        if(cooldown == rate){
            barrelCycler.cycle();
            chamberCycler.cycle();
        }
    }

    protected LivingEntity getEntity() {
        return renderer.getRenderEntity();
    }

    protected GunItem getGunItem() {
        return (GunItem) getStack().getItem();
    }

    protected boolean isOneHanded(ItemStack stack){
        return stack.getItem() instanceof GunItem && GunModifierHelper.getGripType(stack) == GripType.ONE_HANDED;
    }

    protected AnimationController.AnimationStateHandler<GunAnimator> animate() {
        return event -> {
            try {
                var controller = event.getController();
                controller.setAnimationSpeed(1);
                var entity = getEntity();
                var reloadHandler = ClientReloadHandler.get();
                var holdAnimation = playGunAnim(HOLD, LOOP);

                if (!isFirstPerson(transformType))
                    return event.setAndContinue(holdAnimation);

                var arm = isRightHand(transformType) ? HumanoidArm.RIGHT : HumanoidArm.LEFT;
                var shootingHandler = ShootingHandler.get();
                var isShooting = shootingHandler.isShooting(entity, arm);
                var data = shootingHandler.getShootingData(arm);
                var fireTimer = GunModifierHelper.getFireDelay(getStack());
                var animation = begin();

                if(fireTimer > 0 && data.fireTimer > 0 && fireTimer != data.fireTimer){
                    animation = getChargingAnimation(event, data);
                } else if (reloadHandler.isReloading(entity, arm)) {
                    animation = getReloadingAnimation(event);
                } else if (isShooting) {
                    animation = getShootingAnimation(event);
                } else if (reloadHandler.isReloading(entity, arm.getOpposite())) {
                    animation = begin().then(HIDE, HOLD_ON_LAST_FRAME);
                }
                else if(ClientHandler.getInspectionTicks() > 0){
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

    protected AnimationController.AnimationStateHandler<GunAnimator> animateRevolver() {
        return (event) -> getCycledAnimation(event, CHAMBER, this.chamberCycler);
    }

    protected AnimationController.AnimationStateHandler<GunAnimator> animateBarrels() {
        return (event) -> getCycledAnimation(event, BARREL, this.barrelCycler);
    }



    private PlayState getCycledAnimation(AnimationState<GunAnimator> event, String animationName, Cycler cycler) {
        event.getController().setAnimationSpeed(1.0);

        if (TransformUtils.isHandTransform(this.transformType) && cycler != null) {
            var entity = this.getEntity();
            var isShooting = ShootingHandler.get().isShooting(entity, TransformUtils.getHand(this.transformType));
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
        animation = playGunAnim(INSPECT, PLAY_ONCE);
        animationHelper.syncAnimation(event, INSPECT, ClientHandler.getMaxInspectionTicks());
        return animation;
    }

    protected RawAnimation getChargingAnimation(AnimationState<GunAnimator> event , ShootingData data) {
        var animation = begin();
        var fireTimer = GunModifierHelper.getFireDelay(getStack());
        var speed = 1 - ((float) data.fireTimer / (float) fireTimer);
        var controller = event.getController();
        controller.setAnimationSpeed(speed);

        if(animationHelper.hasAnimation(CHARGE)) {
            BARREL_CONTROLLER.stop();
            BARREL_CONTROLLER.setAnimation(begin().then("void", PLAY_ONCE));
            animation = playGunAnim(CHARGE, LOOP);
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

        if(animationHelper.containsAnimation(RELOAD_START))
            animation.then(RELOAD_START, PLAY_ONCE);

        animation.then(RELOAD, LOOP);

        if(animationHelper.containsAnimation(RELOAD_END))
            animation.then(RELOAD_END, PLAY_ONCE);

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

        if(isOneHanded && animationHelper.hasAnimation(name + ONE_HAND_SUFFIX))
            return begin().then(name + ONE_HAND_SUFFIX, loopType);
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
