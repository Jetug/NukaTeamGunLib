
package com.nukateam.example.client;

import mod.azure.azurelib.core.animatable.GeoAnimatable;
import mod.azure.azurelib.core.animation.AnimationState;
import mod.azure.azurelib.model.GeoModel;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.registries.ForgeRegistries;
import org.apache.commons.io.FilenameUtils;


public class EntityModel<T extends LivingEntity & GeoAnimatable> extends GeoModel<T> {
    public static String getResourceName(ResourceLocation resourceLocation) {
        String path = resourceLocation.getPath();
        return FilenameUtils.removeExtension(FilenameUtils.getName(path));
    }

    public static ResourceLocation getResource(Entity animatable, String path, String extension) {
        var name = getResourceName(ForgeRegistries.ENTITY_TYPES.getKey(animatable.getType()));
        var modId = ForgeRegistries.ENTITY_TYPES.getKey(animatable.getType()).getNamespace();

        return new ResourceLocation(modId, path + name + extension);
    }

    @Override
    public ResourceLocation getModelResource(T object) {
        return getResource(object, "geo/entity/", ".geo.json");
    }

    @Override
    public ResourceLocation getTextureResource(T object) {
        return getResource(object, "textures/entity/", ".png");
    }

    @Override
    public ResourceLocation getAnimationResource(T object) {
        return getResource(object, "animations/entity/", ".animation.json");
    }

    @Override
    public void setCustomAnimations(T animatable, long instanceId, AnimationState<T> animationState) {
        super.setCustomAnimations(animatable, instanceId, animationState);
    }
}