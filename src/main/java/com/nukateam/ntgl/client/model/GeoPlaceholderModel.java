package com.nukateam.ntgl.client.model;

import com.nukateam.example.common.data.interfaces.IResourceProvider;
import com.nukateam.ntgl.client.animators.GunAnimator;
import com.nukateam.ntgl.client.animators.IConfigProvider;
import com.nukateam.ntgl.common.base.config.Gun;
import mod.azure.azurelib.core.animatable.GeoAnimatable;
import mod.azure.azurelib.model.GeoModel;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;

public class GeoPlaceholderModel<T extends IResourceProvider & GeoAnimatable> extends GeoModel<T> {
    public static final GeoPlaceholderModel<GunAnimator> INSTANCE = new GeoPlaceholderModel<>();

    @Override
    public ResourceLocation getModelResource(T gunItem) {
        return getGunResource(gunItem, "geo/guns/", ".geo.json");
    }

    @Override
    public ResourceLocation getTextureResource(T gunItem) {

        return getGunResource(gunItem, "textures/guns/" + gunItem.getName() + "/", ".png");
    }

    @Override
    public ResourceLocation getAnimationResource(T gunItem) {
        return getGunResource(gunItem, "animations/guns/", ".animation.json");
    }

    @Override
    public RenderType getRenderType(T animatable, ResourceLocation texture) {
        return RenderType.entityTranslucent(getTextureResource(animatable));
    }

    public static ResourceLocation getGunResource(IResourceProvider gunItem, String path, String extension){
        var name = gunItem.getName();
        var modId = gunItem.getNamespace();

        return new ResourceLocation(modId, path + name + extension);
    }
}
