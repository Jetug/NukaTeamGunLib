
package com.nukateam.example.client;

import com.nukateam.ntgl.Ntgl;
import mod.azure.azurelib.core.animation.AnimationState;
import mod.azure.azurelib.model.GeoModel;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;

public class DeathclawModel<Type extends Deathclaw> extends EntityModel<Type> {
    private static final ResourceLocation model = new ResourceLocation(Ntgl.MOD_ID, "geo/entity/deathclaw.geo.json");
    private static final ResourceLocation animation = new ResourceLocation(Ntgl.MOD_ID, "animations/entity/deathclaw.animation.json");

    @Override
    public ResourceLocation getModelResource(Type animatable) {
        return model;
    }

    @Override
    public ResourceLocation getTextureResource(Type object) {
        return new ResourceLocation(Ntgl.MOD_ID, "textures/entity/deathclaw.png");
    }

    @Override
    public ResourceLocation getAnimationResource(Type animatable) {
        return animation;
    }
}