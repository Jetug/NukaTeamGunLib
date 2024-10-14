package com.nukateam.ntgl.client.model;

import com.nukateam.example.common.data.interfaces.IResourceProvider;
import com.nukateam.ntgl.client.animators.GunAnimator;
import com.nukateam.ntgl.common.foundation.item.GunItem;
import mod.azure.azurelib.model.GeoModel;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;

public class GeoGunModel<T extends GunAnimator> extends GeoModel<T> {
    public static final GeoGunModel<GunAnimator> INSTANCE = new GeoGunModel<>();

    @Override
    public ResourceLocation getModelResource(T animator) {
        return getGunResource(animator, "geo/guns/", ".geo.json");
    }

    @Override
    public ResourceLocation getTextureResource(T animator) {
        var stack = animator.getStack();
        var config = animator.getConfig();
        var textures = config.getTextures();
        var itemName = animator.getName();
        var variant = GunItem.getVariant(stack);
        ResourceLocation resource;

        if (textures.containsKey(variant)) {
            var textureName = textures.get(variant);
            resource = new ResourceLocation(animator.getNamespace(), "textures/guns/" + itemName + "/" + textureName + ".png");
        } else resource = getGunResource(animator, "textures/guns/" + itemName + "/", ".png");

        return resource;
    }

    @Override
    public ResourceLocation getAnimationResource(T animator) {
        return getGunResource(animator, "animations/guns/", ".animation.json");
    }

    @Override
    public RenderType getRenderType(T animatable, ResourceLocation texture) {
        return RenderType.entityTranslucent(getTextureResource(animatable));
    }

    public static ResourceLocation getGunResource(IResourceProvider animator, String path, String extension) {
        var name = animator.getName();
        var modId = animator.getNamespace();

        return new ResourceLocation(modId, path + name + extension);
    }
}
