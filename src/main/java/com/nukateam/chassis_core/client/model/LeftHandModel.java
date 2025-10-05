package com.nukateam.chassis_core.client.model;

import com.nukateam.chassis_core.client.animators.HandAnimator;
import mod.azure.azurelib.model.GeoModel;
import net.minecraft.resources.ResourceLocation;

import static com.nukateam.chassis_core.client.render.utils.ResourceHelper.getChassisResource;

public class LeftHandModel extends GeoModel<HandAnimator> {
    @Override
    public ResourceLocation getModelResource(HandAnimator geoAnimatable) {
        return getChassisResource("geo/hand/", "_left_hand.geo.json");
    }

    @Override
    public ResourceLocation getTextureResource(HandAnimator geoAnimatable) {
        return getChassisResource("textures/hand/", "_left_hand.png");
    }

    @Override
    public ResourceLocation getAnimationResource(HandAnimator geoAnimatable) {
        return getChassisResource("animations/hand/", "_left_hand.animation.json");
    }
}
