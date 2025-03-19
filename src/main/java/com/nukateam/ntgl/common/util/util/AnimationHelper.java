package com.nukateam.ntgl.common.util.util;

import com.nukateam.geo.interfaces.IResourceProvider;
import mod.azure.azurelib.cache.AzureLibCache;
import mod.azure.azurelib.core.animatable.GeoAnimatable;
import mod.azure.azurelib.core.animation.Animation;
import mod.azure.azurelib.core.animation.AnimationState;
import mod.azure.azurelib.model.GeoModel;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

public class AnimationHelper<T extends IResourceProvider & GeoAnimatable> {
    private final T animatable;
    private final GeoModel model;

    public AnimationHelper(T animatable, GeoModel model) {
        this.animatable = animatable;
        this.model = model;
    }

    public void syncAnimation(AnimationState event, String animationName, int targetDuration) {
        var multiplier = (float) getSpeedMultiplier(animationName, targetDuration);
        event.setControllerSpeed(multiplier);
    }

    public void syncAnimations(AnimationState event, int targetDuration, String... animations) {
        var multiplier = getSpeedMultiplier(targetDuration, List.of(animations));
        event.setControllerSpeed((float) multiplier);
    }

    public void syncAnimations(AnimationState event, int targetDuration, Iterable<String> animations) {
        var multiplier = getSpeedMultiplier(targetDuration, animations);
        event.setControllerSpeed((float) multiplier);
    }

    public double getSpeedMultiplier(double targetDuration, Iterable<String> animations) {
        var generalDuration = 0.0;
        for (String name : animations)
            generalDuration += getAnimationDuration(name);

        return generalDuration / targetDuration;
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
