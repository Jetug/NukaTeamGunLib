package com.nukateam.ntgl.client.model.misc;

import com.nukateam.example.client.EntityModel;
import com.nukateam.example.common.entities.Brahmin;
import com.nukateam.ntgl.Ntgl;
import com.nukateam.ntgl.common.foundation.entity.misc.AshPile;
import mod.azure.azurelib.model.GeoModel;
import net.minecraft.resources.ResourceLocation;

public class AshPileModel extends GeoModel<AshPile> {
    @Override
    public ResourceLocation getModelResource(AshPile object) {
        return new ResourceLocation(Ntgl.MOD_ID, "geo/misc/ash.geo.json");
    }

    @Override
    public ResourceLocation getTextureResource(AshPile object) {
        return new ResourceLocation(Ntgl.MOD_ID, "textures/misc/ash.png");
    }

    @Override
    public ResourceLocation getAnimationResource(AshPile object) {
        return new ResourceLocation(Ntgl.MOD_ID, "animations/misc/void.animation.json");
    }
}