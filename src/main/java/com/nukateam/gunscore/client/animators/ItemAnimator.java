package com.nukateam.gunscore.client.animators;

import mod.azure.azurelib.animatable.GeoEntity;
import mod.azure.azurelib.core.animatable.instance.AnimatableInstanceCache;
import net.minecraft.client.renderer.block.model.ItemTransforms;

import static mod.azure.azurelib.util.AzureLibUtil.createInstanceCache;

public abstract class ItemAnimator implements GeoEntity {
    protected final AnimatableInstanceCache cache = createInstanceCache(this);
    protected final ItemTransforms.TransformType transformType;

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return cache;
    }

    public ItemAnimator(ItemTransforms.TransformType transformType) {
        this.transformType = transformType;
    }

    public ItemTransforms.TransformType getTransformType() {
        return transformType;
    }
}
