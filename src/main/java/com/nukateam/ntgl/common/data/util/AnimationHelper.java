package com.nukateam.ntgl.common.data.util;

import com.nukateam.example.common.data.interfaces.IResourceProvider;
import mod.azure.azurelib.cache.AzureLibCache;
import mod.azure.azurelib.core.animatable.GeoAnimatable;
import mod.azure.azurelib.core.animation.Animation;
import mod.azure.azurelib.core.animation.AnimationState;
import mod.azure.azurelib.model.GeoModel;

import javax.annotation.Nullable;

public class AnimationHelper<T extends IResourceProvider & GeoAnimatable> {
    private final T animatable;
    private final GeoModel model;

    public AnimationHelper(T animatable, GeoModel model) {
        this.animatable = animatable;
        this.model = model;
    }

    public void syncAnimation(AnimationState event, String animationName, int reloadDuration) {
        var multiplier = (float) getSpeedMultiplier(animationName, reloadDuration);
        event.setControllerSpeed(multiplier);
    }

    public double getSpeedMultiplier(String animationName, double targetDuration) {
        var duration = getAnimationDuration(animationName);
        return duration / targetDuration;
    }

    public double getAnimationDuration(String animationName) {
        var animation = getAnimation(animationName);
        return animation != null ? animation.length() : 1;
    }

    public boolean containsAnimation(String animationName) {
        return getAnimation(animationName) != null;
    }

    @Nullable
    public Animation getAnimation(String animationName){
        var map = AzureLibCache.getBakedAnimations();
        var animationResource = model.getAnimationResource(animatable);
        var bakedAnimations = map.get(animationResource);
        return bakedAnimations.animations().get(animationName);
    }

    public boolean hasAnimation(String animationName){
        return getAnimation(animationName) != null;
    }
}
