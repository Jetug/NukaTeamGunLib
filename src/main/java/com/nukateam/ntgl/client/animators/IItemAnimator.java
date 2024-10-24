package com.nukateam.ntgl.client.animators;

import mod.azure.azurelib.animatable.GeoEntity;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;

public interface IItemAnimator extends GeoEntity {
    ItemStack getStack();
    void setStack(ItemStack stack);
    ItemDisplayContext getTransformType();
}
