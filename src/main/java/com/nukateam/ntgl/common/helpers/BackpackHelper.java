package com.nukateam.ntgl.common.helpers;

import com.nukateam.ntgl.common.base.AmmoContext;
import com.nukateam.ntgl.common.base.gun.Gun;
import com.mrcrayfish.backpacked.Backpacked;
import com.mrcrayfish.backpacked.core.ModEnchantments;
import com.mrcrayfish.backpacked.inventory.BackpackInventory;
import com.mrcrayfish.backpacked.inventory.BackpackedInventoryAccess;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentHelper;

/**
 * Author: MrCrayfish
 */
public class BackpackHelper {
    public static AmmoContext findAmmo(Player player, ResourceLocation id) {
        ItemStack backpack = Backpacked.getBackpackStack(player);
        if (backpack.isEmpty())
            return AmmoContext.NONE;

        if (EnchantmentHelper.getItemEnchantmentLevel(ModEnchantments.MARKSMAN.get(), backpack) <= 0)
            return AmmoContext.NONE;

        BackpackInventory inventory = ((BackpackedInventoryAccess) player).getBackpackedInventory();
        if (inventory == null)
            return AmmoContext.NONE;

        for (int i = 0; i < inventory.getContainerSize(); i++) {
            ItemStack stack = inventory.getItem(i);
            if (Gun.isAmmo(stack, id)) {
                return new AmmoContext(stack, inventory);
            }
        }

        return AmmoContext.NONE;
    }

    public static AmmoContext findMagazine(Player player, ResourceLocation id) {
        ItemStack backpack = Backpacked.getBackpackStack(player);
        if (backpack.isEmpty())
            return AmmoContext.NONE;

        if (EnchantmentHelper.getItemEnchantmentLevel(ModEnchantments.MARKSMAN.get(), backpack) <= 0)
            return AmmoContext.NONE;

        var inventory = ((BackpackedInventoryAccess) player).getBackpackedInventory();

        if (inventory == null)
            return AmmoContext.NONE;

        ItemStack ammo = null;

        for (int i = 0; i < inventory.getContainerSize(); i++) {
            var stack = inventory.getItem(i);
            if (Gun.isAmmo(stack, id)) {
                if(stack.getDamageValue() == 0)
                    return new AmmoContext(stack, inventory);
                if (ammo == null || (stack.getDamageValue() < ammo.getDamageValue() && ammo.getDamageValue() < ammo.getMaxDamage()))
                    ammo = stack;
            }
        }


        return ammo == null ? AmmoContext.NONE : new AmmoContext(ammo, inventory);
    }
}
