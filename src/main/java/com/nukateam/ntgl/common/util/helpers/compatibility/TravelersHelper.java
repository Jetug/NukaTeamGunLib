package com.nukateam.ntgl.common.util.helpers.compatibility;


import com.nukateam.ntgl.common.data.holders.AmmoHolder;
import com.nukateam.ntgl.common.util.helpers.context.AmmoContext;
import com.nukateam.ntgl.common.util.helpers.context.IAmmoContext;
import com.nukateam.ntgl.common.util.helpers.context.ItemHandlerAmmoContext;
import com.nukateam.ntgl.common.util.util.InventoryUtil;
import com.tiviacz.travelersbackpack.capability.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.items.IItemHandler;

import javax.annotation.Nullable;

public class TravelersHelper {
    public static IAmmoContext findAmmo(Player player, AmmoHolder id) {
        var inventory = getBackpackInventory(player);

        if (inventory == null)
            return AmmoContext.NONE;

        for (int i = 0; i < inventory.getSlots(); i++) {
            ItemStack stack = inventory.getStackInSlot(i);
            if (InventoryUtil.isAmmo(stack, id)) {
                return new ItemHandlerAmmoContext(stack, inventory);
            }
        }

        return AmmoContext.NONE;
    }

    public static IAmmoContext findMagazine(Player player, AmmoHolder id) {
        var inventory = getBackpackInventory(player);

        if (inventory == null)
            return AmmoContext.NONE;

        ItemStack ammo = null;

        for (int i = 0; i < inventory.getSlots(); i++) {
            ItemStack stack = inventory.getStackInSlot(i);
            if (InventoryUtil.isAmmo(stack, id)) {
                if (stack.getDamageValue() == 0) {
                    return new ItemHandlerAmmoContext(stack, inventory);
                }
                if (ammo == null || (stack.getDamageValue() < ammo.getDamageValue() && ammo.getDamageValue() < ammo.getMaxDamage())) {
                    ammo = stack;
                }
            }
        }

        return ammo == null ? AmmoContext.NONE : new ItemHandlerAmmoContext(ammo, inventory);
    }

    @Nullable
    private static IItemHandler getBackpackInventory(Player player) {
        if(CapabilityUtils.isWearingBackpack(player)) {
            var wrapper = CapabilityUtils.getBackpackWrapper(player);
            return wrapper.getStorage();
        }

        return null;
    }
}
