
package com.nukateam.example.client;

import software.bernie.geckolib.core.animatable.GeoAnimatable;
import software.bernie.geckolib.core.animation.AnimationState;
import software.bernie.geckolib.model.GeoModel;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.neoforge.registries.Registries;
import org.apache.commons.io.FilenameUtils;


public class EntityModel<T extends LivingEntity & GeoAnimatable> extends GeoModel<T> {
    public static String getResourceName(ResourceLocation resourceLocation) {
        String path = resourceLocation.getPath();
        return FilenameUtils.removeExtension(FilenameUtils.getName(path));
    }

    public static ResourceLocation getResource(Entity animatable, String path, String extension) {
        var name = getResourceName(Registries.ENTITY_TYPE.getKey(animatable.getType()));
        var modId = Registries.ENTITY_TYPE.getKey(animatable.getType()).getNamespace();

        return ResourceLocation.tryBuild(modId, path + name + extension);
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