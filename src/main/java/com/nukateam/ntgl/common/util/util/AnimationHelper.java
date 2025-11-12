package com.nukateam.ntgl.common.util.util;

import software.bernie.geckolib.cache.GeckoLibCache;
import software.bernie.geckolib.animatable.GeoAnimatable;
import software.bernie.geckolib.core.animation.Animation;
import software.bernie.geckolib.core.animation.AnimationState;
import software.bernie.geckolib.model.GeoModel;

import javax.annotation.Nullable;
import java.util.List;

public class AnimationHelper<T extends GeoAnimatable> {
    private final T animatable;
    private final GeoModel model;

    public AnimationHelper(T animatable, GeoModel model) {
        this.animatable = animatable;
        this.model = model;
    }

    /**
     * Deprecated: Use #syncAnimation(AnimationState, int, String...) instead
     * <p>
     * Sets the animation controller speed so that the animation duration matches the target duration
     */
    @Deprecated
    public void syncAnimation(AnimationState event, String animationName, int targetDuration) {
        var multiplier = (float) getSpeedMultiplier(animationName, targetDuration);
        event.setControllerSpeed(multiplier);
    }

    /**
     * Sets the animation controller speed so that the animation duration matches the target duration
     */
    public void syncAnimation(AnimationState event, int targetDuration, String... animations) {
        var multiplier = getSpeedMultiplier(targetDuration, List.of(animations));
        event.setControllerSpeed((float) multiplier);
    }

    public void syncAnimation(AnimationState event, int targetDuration, Iterable<String> animations) {
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
        var map = GeckoLibCache.getBakedAnimations();
        var animationResource = model.getAnimationResource(animatable);
        var bakedAnimations = map.get(animationResource);
        return bakedAnimations.animations().get(animationName);
    }

    public boolean hasAnimation(String animationName){
        return getAnimation(animationName) != null;
    }
}
