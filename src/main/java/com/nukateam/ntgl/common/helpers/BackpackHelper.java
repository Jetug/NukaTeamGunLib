package com.nukateam.ntgl.common.helpers;

import com.nukateam.ntgl.common.base.AmmoContext;
import com.nukateam.ntgl.common.base.gun.Gun;
import com.mrcrayfish.backpacked.Backpacked;
import com.mrcrayfish.backpacked.core.ModEnchantments;
import com.mrcrayfish.backpacked.inventory.BackpackInventory;
import com.mrcrayfish.backpacked.inventory.BackpackedInventoryAccess;
import com.nukateam.ntgl.common.data.util.GunModifierHelper;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentHelper;

/**
 * Author: MrCrayfish
 */
public class BackpackHelper {
    public static AmmoContext findAmmo(Player player, ItemStack weapon) {
        var inventory = ((BackpackedInventoryAccess) player).getBackpackedInventory();
        if (inventory == null) return AmmoContext.NONE;
        return Gun.findAmmo(inventory, weapon);
    }

    public static AmmoContext findMagazine(Player player, ItemStack weapon) {
        var inventory = ((BackpackedInventoryAccess) player).getBackpackedInventory();
        if (inventory == null) return AmmoContext.NONE;
        return Gun.findMagazine(player.getInventory(), weapon);
    }
}
