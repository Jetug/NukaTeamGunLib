package com.nukateam.example.client;

import com.nukateam.example.common.entities.ExampleGeoEntity;
import net.minecraft.resources.ResourceLocation;

public class ExampleGeoModel extends EntityModel<ExampleGeoEntity> {
    @Override
    public ResourceLocation getModelResource(ExampleGeoEntity object) {
        return getResource(object, "geo/entity/", ".geo.json");
    }

    @Override
    public ResourceLocation getTextureResource(ExampleGeoEntity object) {
        return getResource(object, "textures/entity/", ".png");
    }

    @Override
    public ResourceLocation getAnimationResource(ExampleGeoEntity object) {
        return getResource(object, "animations/entity/", ".animation.json");
    }
}
