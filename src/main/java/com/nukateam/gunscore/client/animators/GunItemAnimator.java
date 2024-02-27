package com.nukateam.gunscore.client.animators;

import com.jetug.chassis_core.common.data.json.ItemConfig;
import com.jetug.chassis_core.common.foundation.item.IConfigProvider;
import com.nukateam.example.common.data.interfaces.IResourceProvider;
import com.nukateam.gunscore.client.audio.GunShotSound;
import com.nukateam.gunscore.client.data.handler.AimingHandler;
import com.nukateam.gunscore.client.data.handler.ClientReloadHandler;
import com.nukateam.gunscore.client.data.handler.ShootingHandler;
import com.nukateam.gunscore.client.model.GeoGunModel;
import com.nukateam.gunscore.common.foundation.item.GunItem;

import mod.azure.azurelib.cache.AzureLibCache;
import mod.azure.azurelib.core.animation.AnimationController;
import mod.azure.azurelib.core.animation.AnimationState;
import mod.azure.azurelib.core.animation.RawAnimation;
import mod.azure.azurelib.core.keyframe.event.SoundKeyframeEvent;
import mod.azure.azurelib.core.object.PlayState;
import net.minecraft.client.Minecraft;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.util.Lazy;

import javax.annotation.Nullable;

import static com.jetug.chassis_core.client.ClientConfig.*;
import static com.nukateam.example.common.data.constants.Animations.*;
import static com.nukateam.gunscore.client.data.util.TransformUtils.*;
import static com.nukateam.gunscore.client.render.Render.*;
import static mod.azure.azurelib.core.animation.AnimatableManager.*;
import static mod.azure.azurelib.core.animation.Animation.LoopType.*;
import static mod.azure.azurelib.core.animation.RawAnimation.*;
import static net.minecraft.client.renderer.block.model.ItemTransforms.*;

@OnlyIn(Dist.CLIENT)
public class GunItemAnimator extends ItemAnimator implements IResourceProvider, IConfigProvider {
    private final Lazy<ItemConfig> config = Lazy.of(() -> modResourceManager.getItemConfig(getName()));
    private final Minecraft minecraft = Minecraft.getInstance();
    private int chamberId = 1;
    private GunItem currentGun = null;

    public GunItemAnimator(TransformType transformType) {
        super(transformType);
    }

    @Override
    public void registerControllers(ControllerRegistrar controllerRegistrar) {
        var mainController = new AnimationController<>(this, "controller1", 0, animate())
                .setSoundKeyframeHandler(this::soundHandler);
        controllerRegistrar.add(mainController);
        controllerRegistrar.add(new AnimationController<>(this, "controller2", 0, holdAnimation()));
        controllerRegistrar.add(new AnimationController<>(this, "controller3", 0, animateRevolver()));
    }

    @Override
    public String getName() {
        return ((IResourceProvider) GUN_RENDERER.getRenderStack().getItem()).getName();
    }

    @Override
    public String getNamespace() {
        return ((IResourceProvider) GUN_RENDERER.getRenderStack().getItem()).getNamespace();
    }

    @Override
    @Nullable
    public ItemConfig getConfig() {
        return config.get();
    }

    private ItemStack getStack() {
        return GUN_RENDERER.getRenderStack();
    }

    private LivingEntity getEntity() {
        return GUN_RENDERER.getRenderEntity();
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
                var isShooting = ShootingHandler.get().isShooting(entity, arm);

                RawAnimation animation;

                if (reloadHandler.isReloading(entity, arm)) {
                    animation = begin().then(RELOAD, HOLD_ON_LAST_FRAME);
                    syncAnimation(event, RELOAD, general.getReloadTime());
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

    private AnimationController.AnimationStateHandler<GunItemAnimator> animateRevolver() {
        return event -> {
            event.getController().setAnimationSpeed(1);
            var general = getGunItem().getModifiedGun(getStack()).getGeneral();
            var entity = getEntity();

            if (!isHandTransform(transformType)) return PlayState.STOP;

            var arm = isRightHand(transformType) ? HumanoidArm.RIGHT : HumanoidArm.LEFT;
            var cooldown = ShootingHandler.get().getCooldownRight(entity, arm);
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
        var map = AzureLibCache.getBakedAnimations();
        var animationResource = GeoGunModel.INSTANCE.getAnimationResource(this);
        var bakedAnimations = map.get(animationResource);
        var animation = bakedAnimations.animations().get(animationName);

        return animation != null ? animation.length() : 1;
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
