package com.nukateam.geo.render;

import software.bernie.geckolib.core.animatable.GeoAnimatable;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;

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
