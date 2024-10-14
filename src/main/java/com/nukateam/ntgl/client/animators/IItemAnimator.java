package com.nukateam.ntgl.client.animators;

import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;

public interface IItemAnimator {
    ItemStack getStack();
    ItemDisplayContext getTransformType();
}
