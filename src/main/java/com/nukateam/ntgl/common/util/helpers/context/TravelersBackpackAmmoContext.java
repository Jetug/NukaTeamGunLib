package com.nukateam.ntgl.common.util.helpers.context;

import com.nukateam.ntgl.common.data.holders.AmmoHolder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.items.IItemHandler;

public class TravelersBackpackAmmoContext implements IAmmoContext {
    public static final IAmmoContext NONE = new TravelersBackpackAmmoContext(ItemStack.EMPTY, null, -1);

    private final ItemStack stack;
    private final IItemHandler inventory;
    private final int slot;

    public TravelersBackpackAmmoContext(ItemStack stack, IItemHandler inventory, int slot) {
        this.stack = stack;
        this.inventory = inventory;
        this.slot = slot;
    }

    @Override
    public ItemStack stack() {
        return stack;
    }

    @Override
    public void shrink(int amount, AmmoHolder ammoHolder, LivingEntity entity) {
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