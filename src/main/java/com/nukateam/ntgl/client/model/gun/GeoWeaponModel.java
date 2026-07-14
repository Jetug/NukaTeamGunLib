package com.nukateam.ntgl.client.model.gun;

import com.nukateam.geo.render.ItemAnimator;
import com.nukateam.ntgl.client.animators.WeaponAnimator;
import com.nukateam.ntgl.client.model.IGlowingModel;
import com.nukateam.ntgl.client.util.helpers.GeoModelHelper;
import com.nukateam.ntgl.common.util.util.WeaponStateHelper;
import software.bernie.geckolib.model.GeoModel;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;

public class GeoWeaponModel<Animator extends WeaponAnimator> extends GeoModel<Animator> implements IGlowingModel<Animator> {
    public static final GeoWeaponModel INSTANCE = new GeoWeaponModel();

    @Override
    public ResourceLocation getModelResource(Animator animator) {
        return GeoModelHelper.getGunResource(animator, "geo/weapons/", ".geo.json");
    }

    @Override
    public ResourceLocation getTextureResource(Animator animator) {
        var textures = animator.getConfig().getTextures();
        var variant = WeaponStateHelper.getVariant(animator.getStack());
        var resource = textures.containsKey(variant) ?
                textures.get(variant) :
                GeoModelHelper.getGunResource(animator, "textures/weapons/" + animator.getId().getPath() + "/", ".png".formatted());

        return resource;
    }

    @Override
    public ResourceLocation getAnimationResource(Animator animator) {
        return GeoModelHelper.getGunResource(animator, "animations/weapons/", ".animation.json");
    }

    @Override
    public RenderType getRenderType(Animator animatable, ResourceLocation texture) {
        return RenderType.entityTranslucent(getTextureResource(animatable));
    }

    @Override
    public ResourceLocation getGlowingTextureResource(Animator animator) {
        var name = animator.getId().getPath();
        var modId = animator.getId().getNamespace();

        return ResourceLocation.tryBuild(modId, "textures/weapons/" + name + "/" + name + "_glowmask" + ".png");
    }
}
