package com.nukateam.chassis_core.client.animators;

import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animation.AnimatableManager;
import software.bernie.geckolib.core.animation.AnimationController;
import software.bernie.geckolib.core.object.PlayState;
import net.minecraft.client.player.LocalPlayer;
import org.jetbrains.annotations.NotNull;

import static software.bernie.geckolib.util.GeckoLibUtil.createInstanceCache;

public class HandAnimator implements GeoEntity {
    private final AnimatableInstanceCache cache = createInstanceCache(this);
    public LocalPlayer player;

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<>(this, "controllerName", 0, predicate()));
    }

    @NotNull
    protected AnimationController.AnimationStateHandler<HandAnimator> predicate() {
        return event -> PlayState.STOP;
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return cache;
    }
}