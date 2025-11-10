package com.nukateam.ntgl.common.util.helpers.context;

import com.nukateam.ntgl.common.data.holders.AmmoHolder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.items.IItemHandler;

public record YyzBackpackAmmoContext(ItemStack stack, IItemHandler inventory, int slot) implements IAmmoContext {
    @Override
    public void shrink(int amount, AmmoHolder ammoHolder, LivingEntity entity) {
        if (inventory != null && slot >= 0) {
            ItemStack currentStack = inventory.getStackInSlot(slot);
            if (!currentStack.isEmpty()) {
                currentStack.shrink(amount);
                if (currentStack.isEmpty()) {
                    inventory.extractItem(slot, amount, false);
                }
            }
        }
    }
}