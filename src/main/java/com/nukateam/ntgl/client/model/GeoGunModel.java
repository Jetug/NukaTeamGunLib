package com.nukateam.ntgl.client.model;

import com.ibm.icu.impl.Pair;
import com.nukateam.example.common.data.interfaces.IResourceProvider;
import com.nukateam.ntgl.client.animators.GunAnimator;
import com.nukateam.ntgl.common.foundation.item.GunItem;
import mod.azure.azurelib.core.animatable.GeoAnimatable;
import mod.azure.azurelib.model.GeoModel;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;

import java.util.HashMap;
import java.util.Map;

public class GeoGunModel<T extends GunAnimator> extends GeoModel<T> implements IGlowingModel<T> {
    public static final GeoGunModel<GunAnimator> INSTANCE = new GeoGunModel<>();
    public static final Map<Pair<String, String>, ResourceLocation> textureMap = new HashMap<>();

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
        var resource = textures.containsKey(variant) ?
                textures.get(variant) :
                getGunResource(animator, "textures/guns/" + itemName + "/", ".png".formatted());

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

    @Override
    public ResourceLocation getGlowingTextureResource(T animator) {
        var itemName = animator.getName();
        var name = animator.getName();
        var modId = animator.getNamespace();

        ResourceLocation resource;

        resource = new ResourceLocation(modId, "textures/guns/" + name + "/" + name + "_glowmask" + ".png");

        return resource;
    }

    public static ResourceLocation getGunResource(IResourceProvider animator, String path, String extension) {
        var name = animator.getName();
        var modId = animator.getNamespace();

        return new ResourceLocation(modId, path + name + extension);
    }
}
