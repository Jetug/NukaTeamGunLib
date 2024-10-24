package com.nukateam.ntgl.client.animators;

import mod.azure.azurelib.animatable.GeoEntity;
import mod.azure.azurelib.core.animatable.instance.AnimatableInstanceCache;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;

import static mod.azure.azurelib.util.AzureLibUtil.createInstanceCache;

public abstract class ItemAnimator implements GeoEntity, IItemAnimator {
    protected final AnimatableInstanceCache cache = createInstanceCache(this);
    protected final ItemDisplayContext transformType;
    protected ItemStack itemStack;

    public ItemAnimator(ItemDisplayContext transformType) {
        this.transformType = transformType;
    }

    @Override
    public ItemStack getStack() {
        return itemStack;
    }

    @Override
    public void setStack(ItemStack stack){
        this.itemStack = stack;
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return cache;
    }

    @Override
    public ItemDisplayContext getTransformType() {
        return transformType;
    }
}
