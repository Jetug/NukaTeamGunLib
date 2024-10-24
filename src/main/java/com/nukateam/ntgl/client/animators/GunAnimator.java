package com.nukateam.ntgl.client.animators;

import com.nukateam.example.common.data.interfaces.IResourceProvider;
import com.nukateam.ntgl.client.ClientHandler;
import com.nukateam.ntgl.client.audio.GunShotSound;
import com.nukateam.ntgl.client.data.handler.AimingHandler;
import com.nukateam.ntgl.client.data.handler.ClientReloadHandler;
import com.nukateam.ntgl.client.data.handler.ShootingHandler;
import com.nukateam.ntgl.client.model.GeoGunModel;
import com.nukateam.ntgl.client.render.renderers.DynamicGunRenderer;
import com.nukateam.ntgl.client.render.renderers.GeoDynamicItemRenderer;
import com.nukateam.ntgl.common.base.config.gun.Gun;
import com.nukateam.ntgl.common.base.holders.GripType;
import com.nukateam.ntgl.common.data.util.AnimationHelper;
import com.nukateam.ntgl.common.data.util.GunModifierHelper;
import com.nukateam.ntgl.common.foundation.item.GunItem;
import com.nukateam.ntgl.common.helpers.PlayerHelper;
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

import static com.nukateam.example.common.data.constants.Animations.*;
import static com.nukateam.ntgl.client.data.util.TransformUtils.*;
import static mod.azure.azurelib.core.animation.AnimatableManager.ControllerRegistrar;
import static mod.azure.azurelib.core.animation.Animation.LoopType.*;
import static mod.azure.azurelib.core.animation.RawAnimation.begin;

@OnlyIn(Dist.CLIENT)
public class GunAnimator extends ItemAnimator implements IResourceProvider, IConfigProvider<Gun> {
    public static final String RELOAD_START = "reload_start";
    public static final String RELOAD_END = "reload_end";
    public static final String CHARGE = "charge";
    public static final String ONE_HAND_SUFFIX = "_one_hand";
    public static final String INSPECT = "inspect";
    private static final String SHOT_START = "shot_start";
    private static final String SHOT_END = "shot_end";
    private final Minecraft minecraft = Minecraft.getInstance();
    private final DynamicGunRenderer<GunAnimator> renderer;
    private int chamberId = 1;
    private GunItem currentGun = null;
    private AnimationHelper<GunAnimator> animationHelper = new AnimationHelper<>(this, GeoGunModel.INSTANCE);
    private AnimationController<GunAnimator> triggerController = new AnimationController<>(this, "triggerController", event -> PlayState.CONTINUE);


    public GunAnimator(ItemDisplayContext transformType, GeoDynamicItemRenderer<GunAnimator> renderer) {
        super(transformType);
        this.renderer = (DynamicGunRenderer<GunAnimator>) renderer;
    }

//    AnimationController<GunAnimator> triggersController = new AnimationController<>(this, "aimController", aimAnimation())
//            .triggerableAnim("aim", begin().then("aim", HOLD_ON_LAST_FRAME));

    @Override
    public void registerControllers(ControllerRegistrar controllerRegistrar) {
        var mainController = new AnimationController<>(this, "mainController", 0, animate())
                .setSoundKeyframeHandler(this::soundHandler);

        controllerRegistrar.add(mainController);
        controllerRegistrar.add(triggerController);
//        controllerRegistrar.add(new AnimationController<>(this, "aimController", aimAnimation()));
        controllerRegistrar.add(new AnimationController<>(this, "revolverController", 0, animateRevolver()));
    }

    @Override
    public Gun getConfig() {
        if(getStack().getItem() instanceof IConfigProvider config) {
            if(config.getConfig() instanceof Gun gun)
                return gun;
        }

        return new Gun();
    }

    @Override
    public String getName() {
        return ((IResourceProvider) getStack().getItem()).getName();
    }

    @Override
    public String getNamespace() {
        return ((IResourceProvider) getStack().getItem()).getNamespace();
    }

//    @Override
//    public ItemStack getStack() {
//        return renderer.getRenderStack();
//    }

    private LivingEntity getEntity() {
        return renderer.getRenderEntity();
    }

    private GunItem getGunItem() {
        return (GunItem) getStack().getItem();
    }

    private AnimationController.AnimationStateHandler<GunAnimator> aimAnimation() {
        return event -> {
            event.getController().setAnimationSpeed(1);
//            var stack = GUN_RENDERER.getRenderStack();
//            if (stack == null || stack.isEmpty()) return PlayState.STOP;

            if (isFirstPerson(transformType) && AimingHandler.get().isAiming()){
//                    &&
//                    (triggersController.getCurrentAnimation() == null ||
//                    !triggersController.getCurrentAnimation().animation().name().equals("aim"))) {
//                triggersController.tryTriggerAnimation("aim");

//                event.getController()
                var animation = begin().then("aim", HOLD_ON_LAST_FRAME);
                return event.setAndContinue(animation);
            } else {
//                return event.setAndContinue(begin().then("void", PLAY_ONCE));
                return PlayState.STOP;
            }
        };
    }

    private boolean isOneHanded(ItemStack stack){
        return stack.getItem() instanceof GunItem && GunModifierHelper.getGripType(stack) == GripType.ONE_HANDED;
    }

    private AnimationController.AnimationStateHandler<GunAnimator> animate() {
        return event -> {
            try {
                var controller = event.getController();
                controller.setAnimationSpeed(1);
                var general = getGunItem().getModifiedGun(getStack()).getGeneral();
                var entity = getEntity();
                var reloadHandler = ClientReloadHandler.get();
                var holdAnimation = playGunAnim(HOLD, LOOP);

                if (!isFirstPerson(transformType))
                    return event.setAndContinue(holdAnimation);

                var arm = isRightHand(transformType) ? HumanoidArm.RIGHT : HumanoidArm.LEFT;
                var shootingHandler = ShootingHandler.get();
                var isShooting = shootingHandler.isShooting(entity, arm);
                var data = shootingHandler.getShootingData(arm);
                var animation = begin();

                if(general.getFireTimer() > 0 && data.fireTimer > 0 && general.getFireTimer() != data.fireTimer){
                    var speed = 1 - ((float)data.fireTimer / (float)general.getFireTimer());
                    controller.setAnimationSpeed(speed);
                    if(animationHelper.hasAnimation(CHARGE))
                        animation = playGunAnim(CHARGE, LOOP);
                } else if (reloadHandler.isReloading(entity, arm)) {
                    animation = getReloadAnimation(event, getStack());
                } else if (isShooting) {
                    animation = playGunAnim(SHOT, LOOP);
//                    animation = begin().then(SHOT, LOOP);
                    var rate = GunModifierHelper.getRate(getStack());
                    animationHelper.syncAnimation(event, SHOT, rate);
                } else if (reloadHandler.isReloading(entity, arm.getOpposite())) {
                    animation = begin().then("hide", HOLD_ON_LAST_FRAME);
                }
                else if(ClientHandler.getInspectionTicks() > 0){
                    animation = playGunAnim(INSPECT, PLAY_ONCE);
                    animationHelper.syncAnimation(event, INSPECT, ClientHandler.getMaxInspectionTicks());
                }
                else {
                    if (currentGun == getGunItem())
                        animation = holdAnimation;
                    else {
                        currentGun = getGunItem();
                        animation = playGunAnim(SHOT, LOOP);
                    }
                }

//                if (controller.hasAnimationFinished())
//                    controller.forceAnimationReset();

                return event.setAndContinue(animation);
            } catch (Exception e) {
                return PlayState.STOP;
            }
        };
    }

    private RawAnimation playGunAnim(String name, Animation.LoopType loopType){
        var arm = isRightHand(transformType) ? HumanoidArm.RIGHT : HumanoidArm.LEFT;
        var entity = getEntity();
        var currentItem = entity.getItemInHand(PlayerHelper.convertHand(arm));
        var oppositeItem = entity.getItemInHand(PlayerHelper.convertHand(arm.getOpposite()));

        var isOneHanded = isOneHanded(currentItem) && isOneHanded(oppositeItem) || arm == HumanoidArm.LEFT;

        if(isOneHanded && animationHelper.hasAnimation(name + ONE_HAND_SUFFIX))
            return begin().then(name + ONE_HAND_SUFFIX, loopType);
        return begin().then(name, loopType);
    }

    @NotNull
    private RawAnimation getReloadAnimation(AnimationState<GunAnimator> event, ItemStack stack) {
        RawAnimation animation;
        animation = begin();

        if(animationHelper.containsAnimation(RELOAD_START))
            animation.then(RELOAD_START, PLAY_ONCE);

        animation.then(RELOAD, LOOP);

        if(animationHelper.containsAnimation(RELOAD_END))
            animation.then(RELOAD_END, PLAY_ONCE);

        if(event.getController().getCurrentAnimation().animation().name().equals(RELOAD))
            animationHelper.syncAnimation(event, RELOAD, GunModifierHelper.getReloadTime(stack));
        return animation;
    }

    @NotNull
    private RawAnimation getShotAnimation(AnimationState<GunAnimator> event, ItemStack stack) {
        RawAnimation animation;
        animation = begin();

        if(animationHelper.containsAnimation(SHOT_START))
            animation.then(SHOT_START, PLAY_ONCE);

        animation.then(SHOT, LOOP);

        if(animationHelper.containsAnimation(SHOT_END))
            animation.then(SHOT_END, PLAY_ONCE);

        if(event.getController().getCurrentAnimation().animation().name().equals(RELOAD))
            animationHelper.syncAnimation(event, SHOT, GunModifierHelper.getReloadTime(stack));
        return animation;
    }

    private AnimationController.AnimationStateHandler<GunAnimator> animateRevolver() {
        return event -> {
            event.getController().setAnimationSpeed(1);
            var general = getGunItem().getModifiedGun(getStack()).getGeneral();
            var entity = getEntity();

            if (!isHandTransform(transformType)) return PlayState.STOP;

            var arm = isRightHand(transformType) ? HumanoidArm.RIGHT : HumanoidArm.LEFT;
            var cooldown = ShootingHandler.get().getCooldown(entity, arm);
            var isShooting = ShootingHandler.get().isShooting(entity, arm);

            RawAnimation animation = null;

            var rate = GunModifierHelper.getRate(getStack());
            if (cooldown == rate) {
                if (chamberId < 6)
                    chamberId++;
                else chamberId = 1;
            }

            var chamber = "chamber" + chamberId;

            if (isShooting && animationHelper.hasAnimation(chamber)) {
                animation = begin().then(chamber, HOLD_ON_LAST_FRAME);
                animationHelper.syncAnimation(event, chamber, rate);
            }
            return event.setAndContinue(animation);
        };
    }

    private void soundHandler(SoundKeyframeEvent<GunAnimator> event) {
        var player = minecraft.player;
        if (player == null) return;
        var sound = event.getKeyframeData().getSound();
        var gunSounds = getGunItem().getGun().getSounds();

        switch (sound) {
            case "reload" -> {
                var reloadSound = gunSounds.getReload();

                minecraft.getSoundManager().play(new GunShotSound(reloadSound, SoundSource.PLAYERS,
                        player.position(), 1, 1, true));
            }
            case "cock" -> {
                var cockSound = gunSounds.getCock();

                minecraft.getSoundManager().play(new GunShotSound(cockSound, SoundSource.PLAYERS,
                        player.position(), 1, 1, true));
            }
        }
    }
}
