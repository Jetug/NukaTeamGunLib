package com.nukateam.ntgl.common.base;

import net.minecraft.world.item.ItemStack;

public interface IAmmoContext {
    ItemStack stack();
    void shrink(int amount);
}
