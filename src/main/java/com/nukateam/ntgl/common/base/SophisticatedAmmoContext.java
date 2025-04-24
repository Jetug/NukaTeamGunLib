package com.nukateam.ntgl.common.base;

import net.minecraftforge.items.IItemHandler;
import net.minecraft.world.item.ItemStack;

public record SophisticatedAmmoContext(ItemStack stack, IItemHandler inventory) implements IAmmoContext {
    public static final SophisticatedAmmoContext NONE = new SophisticatedAmmoContext(ItemStack.EMPTY, null);

    public void shrink(int amount){
        for (int i = 0; i < inventory.getSlots() && amount > 0; i++) {
            var foundStack = inventory.getStackInSlot(i);

            if (foundStack.getItem() == stack.getItem()) {
                inventory.extractItem(i, amount, false);
                return;
            }
        }
    }
}
