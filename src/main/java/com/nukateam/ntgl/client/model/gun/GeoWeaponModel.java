package com.nukateam.ntgl.client.model.gun;

import com.nukateam.geo.render.ItemAnimator;
import com.nukateam.ntgl.client.animators.WeaponAnimator;
import com.nukateam.ntgl.client.model.IGlowingModel;
import com.nukateam.ntgl.client.util.helpers.GeoModelHelper;
import com.nukateam.ntgl.common.util.util.WeaponStateHelper;
import software.bernie.geckolib.model.GeoModel;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;

public class GeoWeaponModel extends GeoModel<WeaponAnimator> implements IGlowingModel<WeaponAnimator> {
    public static final GeoWeaponModel INSTANCE = new GeoWeaponModel();

    @Override
    public ResourceLocation getModelResource(WeaponAnimator animator) {
        return GeoModelHelper.getGunResource(animator, "geo/weapons/", ".geo.json");
    }

    @Override
    public ResourceLocation getTextureResource(WeaponAnimator animator) {
        var textures = animator.getConfig().getTextures();
        var variant = WeaponStateHelper.getVariant(animator.getStack());
        var resource = textures.containsKey(variant) ?
                textures.get(variant) :
                GeoModelHelper.getGunResource(animator, "textures/weapons/" + animator.getId().getPath() + "/", ".png".formatted());

        return resource;
    }

    @Override
    public ResourceLocation getAnimationResource(WeaponAnimator animator) {
        return GeoModelHelper.getGunResource(animator, "animations/weapons/", ".animation.json");
    }

    @Override
    public RenderType getRenderType(WeaponAnimator animatable, ResourceLocation texture) {
        return RenderType.entityTranslucent(getTextureResource(animatable));
    }

    @Override
    public ResourceLocation getGlowingTextureResource(WeaponAnimator animator) {
        var name = animator.getId().getPath();
        var modId = animator.getId().getNamespace();

        return ResourceLocation.tryBuild(modId, "textures/weapons/" + name + "/" + name + "_glowmask" + ".png");
    }
}
