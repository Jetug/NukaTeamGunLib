package com.nukateam.gunscore.client.model;

import com.nukateam.example.common.data.interfaces.IResourceProvider;
import mod.azure.azurelib.core.animatable.GeoAnimatable;
import mod.azure.azurelib.model.GeoModel;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;

import static com.nukateam.example.common.data.utils.ResourceUtils.modResource;

public class LeftHandModel<T extends IResourceProvider & GeoAnimatable> extends GeoModel<T> {
    @Override
    public ResourceLocation getModelResource(T geoAnimatable) {
        return modResource("geo/hand/left_hand.geo.json");
    }

    @Override
    public ResourceLocation getTextureResource(T geoAnimatable) {
        var player = Minecraft.getInstance().player;
        if(player != null) return player.getSkinTextureLocation();
        return null;
    }

    @Override
    public ResourceLocation getAnimationResource(T geoAnimatable) {
        return null;
    }
}
