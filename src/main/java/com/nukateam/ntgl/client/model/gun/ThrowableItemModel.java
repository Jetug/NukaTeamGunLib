package com.nukateam.ntgl.client.model.gun;

import com.nukateam.ntgl.client.animators.ThrowableAnimator;
import com.nukateam.ntgl.client.model.IGlowingModel;
import com.nukateam.ntgl.client.util.util.GeoModelHelper;
import com.nukateam.ntgl.common.foundation.item.WeaponItem;
import mod.azure.azurelib.model.GeoModel;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;

public class ThrowableItemModel<T extends ThrowableAnimator> extends GeoModel<T> implements IGlowingModel<T> {
    public static final ThrowableItemModel<ThrowableAnimator> INSTANCE = new ThrowableItemModel<>();

    @Override
    public ResourceLocation getModelResource(T animator) {
        return GeoModelHelper.getGunResource(animator, "geo/throwable/", ".geo.json");
    }

    @Override
    public ResourceLocation getTextureResource(T animator) {
        var textures = animator.getConfig().getTextures();
        var variant = WeaponItem.getVariant(animator.getStack());
        var resource = textures.containsKey(variant) ?
                textures.get(variant) :
                GeoModelHelper.getGunResource(animator, "textures/throwable/" + animator.getName() + "/", ".png");

        return resource;
    }

    @Override
    public ResourceLocation getAnimationResource(T animator) {
        return GeoModelHelper.getGunResource(animator, "animations/throwable/", ".animation.json");
    }

    @Override
    public RenderType getRenderType(T animatable, ResourceLocation texture) {
        return RenderType.entityTranslucent(getTextureResource(animatable));
    }

    @Override
    public ResourceLocation getGlowingTextureResource(T animator) {
        var name = animator.getName();
        var modId = animator.getNamespace();
        return ResourceLocation.tryBuild(modId, "textures/throwable/" + name + "/" + name + "_glowmask" + ".png");
    }
}
