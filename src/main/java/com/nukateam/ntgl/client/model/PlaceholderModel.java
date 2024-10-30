package com.nukateam.ntgl.client.model;

import com.nukateam.example.common.data.interfaces.IResourceProvider;
import mod.azure.azurelib.core.animatable.GeoAnimatable;
import mod.azure.azurelib.model.GeoModel;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;

public class PlaceholderModel<T extends GeoAnimatable> extends GeoModel<T> {
    @Override
    public ResourceLocation getModelResource(T gunItem) {
//        return getGunResource(gunItem, "geo/guns/", ".geo.json");
        return null;
    }

    @Override
    public ResourceLocation getTextureResource(T gunItem) {
//        return getGunResource(gunItem, "textures/guns/" + gunItem.getName() + "/", ".png");
        return null;
    }

    @Override
    public ResourceLocation getAnimationResource(T gunItem) {
//        return getGunResource(gunItem, "animations/guns/", ".animation.json");
        return null;
    }
}
