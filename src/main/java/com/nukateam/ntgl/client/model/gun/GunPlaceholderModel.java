package com.nukateam.ntgl.client.model.gun;

import com.nukateam.geo.interfaces.IResourceProvider;
import software.bernie.geckolib.core.animatable.GeoAnimatable;
import software.bernie.geckolib.model.GeoModel;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;

public class GunPlaceholderModel<T extends IResourceProvider & GeoAnimatable> extends GeoModel<T> {
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

        return ResourceLocation.tryBuild(modId, path + name + extension);
    }
}
