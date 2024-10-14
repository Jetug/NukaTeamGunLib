package com.nukateam.ntgl.client.animators;

import mod.azure.azurelib.animatable.GeoEntity;
import mod.azure.azurelib.core.animatable.instance.AnimatableInstanceCache;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.world.item.ItemDisplayContext;

import static mod.azure.azurelib.util.AzureLibUtil.createInstanceCache;

public abstract class ItemAnimator implements GeoEntity, IItemAnimator {
    protected final AnimatableInstanceCache cache = createInstanceCache(this);
    protected final ItemDisplayContext transformType;

    public ItemAnimator(ItemDisplayContext transformType) {
        this.transformType = transformType;
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return cache;
    }

    public ItemDisplayContext getTransformType() {
        return transformType;
    }
}
