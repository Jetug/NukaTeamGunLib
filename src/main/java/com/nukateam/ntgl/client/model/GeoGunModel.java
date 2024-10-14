package com.nukateam.ntgl.client.model;

import com.nukateam.example.common.data.interfaces.IResourceProvider;
import com.nukateam.ntgl.client.animators.GunAnimator;

import com.nukateam.ntgl.client.animators.IConfigProvider;
import com.nukateam.ntgl.client.animators.IItemAnimator;
import com.nukateam.ntgl.common.base.config.Gun;
import mod.azure.azurelib.core.animatable.GeoAnimatable;
import mod.azure.azurelib.model.GeoModel;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

public class GeoGunModel<T extends GunAnimator> extends GeoModel<T> {
    public static final GeoGunModel<GunAnimator> INSTANCE = new GeoGunModel<>();

    @Override
    public ResourceLocation getModelResource(T animator) {
        return getGunResource(animator, "geo/guns/", ".geo.json");
    }

    @Override
    public ResourceLocation getTextureResource(T animator) {
        var stack = animator.getStack();
        var tag = stack.getOrCreateTag();
        var variant = tag.getString("Variant");
        var config = animator.getConfig();
        var textures = config.getTextures();
        var textureName = animator.getName();

        if(textures.containsKey(variant))
            textureName = textures.get(variant);

        return getGunResource(animator, "textures/guns/" + textureName + "/", ".png");
    }

    @Override
    public ResourceLocation getAnimationResource(T animator) {
        return getGunResource(animator, "animations/guns/", ".animation.json");
    }

    @Override
    public RenderType getRenderType(T animatable, ResourceLocation texture) {
        return RenderType.entityTranslucent(getTextureResource(animatable));
    }

    public static ResourceLocation getGunResource(IResourceProvider animator, String path, String extension){
        var name = animator.getName();
        var modId = animator.getNamespace();

        return new ResourceLocation(modId, path + name + extension);
    }
}
