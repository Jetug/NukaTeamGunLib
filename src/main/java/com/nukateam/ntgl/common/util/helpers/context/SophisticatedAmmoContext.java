package com.nukateam.ntgl.common.util.helpers.context;

import com.nukateam.ntgl.common.data.holders.AmmoHolder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.items.IItemHandler;
import net.minecraft.world.item.ItemStack;

public record SophisticatedAmmoContext(ItemStack stack, IItemHandler inventory) implements IAmmoContext {
    public void shrink(int amount, AmmoHolder ammoHolder, LivingEntity entity){
        for (int i = 0; i < inventory.getSlots() && amount > 0; i++) {
            var foundStack = inventory.getStackInSlot(i);

            if (foundStack.getItem() == stack.getItem()) {
                var ammo = ammoHolder.onConsume().apply(stack, amount);
                for (var stack : ammo) {
                    entity.spawnAtLocation(stack);
                }

                var shrinkAmount = (int)Math.ceil((double) amount / (double)ammoHolder.getValue(stack));
                inventory.extractItem(i, shrinkAmount, false);
                return;
            }
        }
    }
}
