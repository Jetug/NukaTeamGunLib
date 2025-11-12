package com.nukateam.ntgl.common.util.helpers.compatibility.backpack;

import com.nukateam.ntgl.common.data.holders.AmmoHolder;
import com.nukateam.ntgl.common.util.helpers.context.AmmoContext;
import com.mrcrayfish.backpacked.inventory.BackpackedInventoryAccess;
import com.nukateam.ntgl.common.util.util.InventoryUtil;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

public class BackpackedHelper {
    public static AmmoContext findAmmo(Player player, AmmoHolder id) {
        var inventory = ((BackpackedInventoryAccess) player).backpacked$GetBackpackInventory();

        if (inventory == null)
            return AmmoContext.NONE;

        for (int i = 0; i < inventory.getContainerSize(); i++) {
            ItemStack stack = inventory.getItem(i);
            if (InventoryUtil.isAmmo(stack, id)) {
                return new AmmoContext(stack, inventory);
            }
        }

        return AmmoContext.NONE;
    }

    public static AmmoContext findMagazine(Player player, AmmoHolder id) {
        var inventory = ((BackpackedInventoryAccess) player).backpacked$GetBackpackInventory();

        if (inventory == null)
            return AmmoContext.NONE;

        ItemStack ammo = null;

        for (int i = 0; i < inventory.getContainerSize(); i++) {
            var stack = inventory.getItem(i);
            if (InventoryUtil.isAmmo(stack, id)) {
                if(stack.getDamageValue() == 0)
                    return new AmmoContext(stack, inventory);
                if (ammo == null || (stack.getDamageValue() < ammo.getDamageValue() && ammo.getDamageValue() < ammo.getMaxDamage()))
                    ammo = stack;
            }
        }

        return ammo == null ? AmmoContext.NONE : new AmmoContext(ammo, inventory);
    }
}
