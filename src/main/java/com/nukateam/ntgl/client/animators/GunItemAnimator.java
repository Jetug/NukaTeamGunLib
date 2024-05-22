package com.nukateam.ntgl.client.animators;

import com.nukateam.example.common.data.interfaces.IResourceProvider;
import com.nukateam.ntgl.client.audio.GunShotSound;
import com.nukateam.ntgl.client.data.handler.AimingHandler;
import com.nukateam.ntgl.client.data.handler.ClientReloadHandler;
import com.nukateam.ntgl.client.data.handler.ShootingHandler;
import com.nukateam.ntgl.client.model.GeoGunModel;
import com.nukateam.ntgl.client.render.renderers.DynamicGunRenderer;
import com.nukateam.ntgl.client.render.renderers.GeoDynamicItemRenderer;
import com.nukateam.ntgl.common.base.gun.Gun;
import com.nukateam.ntgl.common.foundation.item.GunItem;

import mod.azure.azurelib.cache.AzureLibCache;
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

import javax.annotation.Nullable;

import static com.nukateam.example.common.data.constants.Animations.*;
import static com.nukateam.ntgl.client.data.util.TransformUtils.*;
import static com.nukateam.ntgl.client.render.Render.*;
import static mod.azure.azurelib.core.animation.AnimatableManager.*;
import static mod.azure.azurelib.core.animation.Animation.LoopType.*;
import static mod.azure.azurelib.core.animation.RawAnimation.*;

@OnlyIn(Dist.CLIENT)
public class GunItemAnimator extends ItemAnimator implements IResourceProvider {
    public static final String RELOAD_START = "reload_start";
    public static final String RELOAD_END = "reload_end";
    private final Minecraft minecraft = Minecraft.getInstance();
    private final DynamicGunRenderer<GunItemAnimator> renderer;
    private int chamberId = 1;
    private GunItem currentGun = null;


    public GunItemAnimator(ItemDisplayContext transformType, GeoDynamicItemRenderer<GunItemAnimator> renderer) {
        super(transformType);
        this.renderer = (DynamicGunRenderer<GunItemAnimator>) renderer;
    }

    @Override
    public void registerControllers(ControllerRegistrar controllerRegistrar) {
        var mainController = new AnimationController<>(this, "mainController", 0, animate())
                .setSoundKeyframeHandler(this::soundHandler);
        controllerRegistrar.add(mainController);
        controllerRegistrar.add(new AnimationController<>(this, "aimController", 0, holdAnimation()));
        controllerRegistrar.add(new AnimationController<>(this, "revolverController", 0, animateRevolver()));
    }

    @Override
    public String getName() {
        return ((IResourceProvider) renderer.getRenderStack().getItem()).getName();
    }

    @Override
    public String getNamespace() {
        return ((IResourceProvider) renderer.getRenderStack().getItem()).getNamespace();
    }

    private ItemStack getStack() {
        return renderer.getRenderStack();
    }

    private LivingEntity getEntity() {
        return renderer.getRenderEntity();
    }

    private GunItem getGunItem() {
        return (GunItem) getStack().getItem();
    }

    private AnimationController.AnimationStateHandler<GunItemAnimator> holdAnimation() {
        return event -> {
            event.getController().setAnimationSpeed(1);
//            var stack = GUN_RENDERER.getRenderStack();
//            if (stack == null || stack.isEmpty()) return PlayState.STOP;

            if (isFirstPerson(transformType) && AimingHandler.get().isAiming()) {
                var animation = begin().then("aim", HOLD_ON_LAST_FRAME);
                return event.setAndContinue(animation);
            } else {
                return PlayState.STOP;
            }
        };
}

    private AnimationController.AnimationStateHandler<GunItemAnimator> animate() {
        return event -> {
            try {
                var controller = event.getController();
                controller.setAnimationSpeed(1);
                var general = getGunItem().getModifiedGun(getStack()).getGeneral();
                var entity = getEntity();
                var reloadHandler = ClientReloadHandler.get();

                if (!isHandTransform(transformType))
                    return event.setAndContinue(begin().then(HOLD, LOOP));

                var arm = isRightHand(transformType) ? HumanoidArm.RIGHT : HumanoidArm.LEFT;
                var shootingHandler = ShootingHandler.get();
                var isShooting = shootingHandler.isShooting(entity, arm);
                var data = shootingHandler.getShootingData(arm);
                RawAnimation animation;

                if(data.fireTimer > 0){
                    animation = begin().then("charge", LOOP);
                } else if (reloadHandler.isReloading(entity, arm)) {
                    animation = getReloadAnimation(event, general);
                } else if (isShooting) {
                    animation = begin().then(SHOT, LOOP);
                    syncAnimation(event, SHOT, general.getRate());
                } else if (reloadHandler.isReloading(entity, arm.getOpposite())) {
                    animation = begin().then("hide", HOLD_ON_LAST_FRAME);
                } else {
                    if (currentGun == getGunItem())
                        animation = begin().then(HOLD, LOOP);
                    else {
                        currentGun = getGunItem();
                        animation = begin().then(SHOT, LOOP);
                    }
                }
//                else if(!Gun.hasAmmo(stack)){
//                    animation = begin().then("slide_off", LOOP);
//                }

                if (controller.hasAnimationFinished())
                    controller.forceAnimationReset();

                return event.setAndContinue(animation);
            } catch (Exception e) {
                return PlayState.STOP;
            }
        };
    }

    @NotNull
    private RawAnimation getReloadAnimation(AnimationState<GunItemAnimator> event, Gun.General general) {
        RawAnimation animation;
        animation = begin();

        if(containsAnimation(RELOAD_START))
            animation.then(RELOAD_START, PLAY_ONCE);

        animation.then(RELOAD, LOOP);

        if(containsAnimation(RELOAD_END))
            animation.then(RELOAD_END, PLAY_ONCE);

        if(event.getController().getCurrentAnimation().animation().name().equals(RELOAD))
            syncAnimation(event, RELOAD, general.getReloadTime());
        return animation;
    }

    private AnimationController.AnimationStateHandler<GunItemAnimator> animateRevolver() {
        return event -> {
            event.getController().setAnimationSpeed(1);
            var general = getGunItem().getModifiedGun(getStack()).getGeneral();
            var entity = getEntity();

            if (!isHandTransform(transformType)) return PlayState.STOP;

            var arm = isRightHand(transformType) ? HumanoidArm.RIGHT : HumanoidArm.LEFT;
            var cooldown = ShootingHandler.get().getCooldown(entity, arm);
            var isShooting = ShootingHandler.get().isShooting(entity, arm);

            RawAnimation animation = null;

            if (cooldown == general.getRate()) {
                if (chamberId < 6)
                    chamberId++;
                else chamberId = 1;
            }

            if (isShooting) {
                var chamber = "chamber" + chamberId;
                animation = begin().then(chamber, HOLD_ON_LAST_FRAME);
                syncAnimation(event, chamber, general.getRate());
            }
            return event.setAndContinue(animation);

        };
    }

    private void syncAnimation(AnimationState<GunItemAnimator> event, String animationName, int reloadDuration) {
        var multiplier = (float) getSpeedMultiplier(animationName, reloadDuration);
        event.setControllerSpeed(multiplier);
    }

    private double getSpeedMultiplier(String animationName, double targetDuration) {
        var duration = getAnimationDuration(animationName);
        return duration / targetDuration;
    }

    private double getAnimationDuration(String animationName) {
        var animation = getAnimation(animationName);
        return animation != null ? animation.length() : 1;
    }

    private boolean containsAnimation(String animationName) {
        return getAnimation(animationName) != null;
    }

    @Nullable
    private Animation getAnimation(String animationName){
        var map = AzureLibCache.getBakedAnimations();
        var animationResource = GeoGunModel.INSTANCE.getAnimationResource(this);
        var bakedAnimations = map.get(animationResource);
        return bakedAnimations.animations().get(animationName);
    }

    private void soundHandler(SoundKeyframeEvent<GunItemAnimator> event) {
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
