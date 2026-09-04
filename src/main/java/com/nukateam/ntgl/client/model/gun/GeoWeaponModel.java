package com.nukateam.ntgl.client.model.gun;

import com.nukateam.ntgl.Ntgl;
import com.nukateam.ntgl.client.animators.WeaponAnimator;
import com.nukateam.ntgl.client.model.IGlowingModel;
import com.nukateam.ntgl.client.util.helpers.GeoModelHelper;
import com.nukateam.ntgl.common.util.util.WeaponStateHelper;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.cache.GeckoLibCache;
import software.bernie.geckolib.model.GeoModel;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class GeoWeaponModel<Animator extends WeaponAnimator> extends GeoModel<Animator> implements IGlowingModel<Animator> {
    public static final GeoWeaponModel INSTANCE = new GeoWeaponModel();

    /** Placeholder assets rendered when a weapon model/animation file is missing or failed to load, so it does not crash the render thread. */
    private static final ResourceLocation ERROR_MODEL = ResourceLocation.fromNamespaceAndPath(Ntgl.MOD_ID, "geo/error.geo.json");
    private static final ResourceLocation ERROR_ANIMATION = ResourceLocation.fromNamespaceAndPath(Ntgl.MOD_ID, "animations/error.animation.json");

    /** Resources already reported, so each one is logged once per game session. */
    private static final Set<ResourceLocation> REPORTED_MISSING = ConcurrentHashMap.newKeySet();

    @Override
    public ResourceLocation getModelResource(Animator animator) {
        var model = GeoModelHelper.getGunResource(animator, "geo/weapons/", ".geo.json");
        if (model != null && GeckoLibCache.getBakedModels().containsKey(model)) return model;
        return reportMissing(model, ERROR_MODEL);
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
        var animation = GeoModelHelper.getGunResource(animator, "animations/weapons/", ".animation.json");
        if (animation != null && GeckoLibCache.getBakedAnimations().containsKey(animation)) return animation;
        return reportMissing(animation, ERROR_ANIMATION);
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

    private static ResourceLocation reportMissing(ResourceLocation requested, ResourceLocation fallback) {
        if (requested != null && REPORTED_MISSING.add(requested))
            Ntgl.LOGGER.warn("Weapon resource {} is not loaded (missing or failed to parse), rendering {} instead", requested, fallback);
        return fallback;
    }
}
