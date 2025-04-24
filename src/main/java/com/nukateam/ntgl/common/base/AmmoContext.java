package com.nukateam.ntgl.common.base;

import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;

import javax.annotation.Nullable;

/**
 * Author: MrCrayfish
 */
public record AmmoContext(ItemStack stack, @Nullable Container container) implements IAmmoContext{
    public static final AmmoContext NONE = new AmmoContext(ItemStack.EMPTY, null);

    public void shrink(int amount){
        stack.shrink(amount);
        if (container != null) {
            container.setChanged();
        }
    }
}
