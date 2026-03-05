package com.nukateam.ntgl.common.util.helpers.compatibility.backpack;

import com.nukateam.ntgl.Ntgl;
import com.nukateam.ntgl.common.data.holders.AmmoHolder;
import com.nukateam.ntgl.common.util.helpers.compatibility.CuriosHelper;
import com.nukateam.ntgl.common.util.helpers.context.AmmoContext;
import com.nukateam.ntgl.common.util.helpers.context.IAmmoContext;
import com.nukateam.ntgl.common.util.helpers.context.YyzBackpackAmmoContext;
import com.nukateam.ntgl.common.util.util.InventoryUtil;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.items.IItemHandler;


import javax.annotation.Nullable;
import java.util.function.Predicate;

public class YyzBackpackHelper {

    private static boolean isSophisticatedBackpack(ItemStack stack) {
        return false;
    }

    private static boolean isTravelersBackpack(ItemStack stack) {
        return false;
    }

    private static boolean isYyzBackpack(ItemStack stack) {
        if (stack.isEmpty()) return false;
        return false;
    }


    @Nullable
    private static IItemHandler getBackpackInventory(Player player) {
        ItemStack backpackStack = getBackpackItem(player);

        if (!backpackStack.isEmpty()) {

            IItemHandler handler = backpackStack.getCapability(Capabilities.ItemHandler.ITEM);
            if (handler != null) {
                return handler;
            }
        }
        return null;
    }

    @Nullable
    private static ItemStack getBackpackItem(Player player) {
        ItemStack chestStack = player.getItemBySlot(EquipmentSlot.CHEST);
        if (isSupportedBackpack(chestStack)) {
            return chestStack;
        }

        if (Ntgl.curiosLoaded) {
            ItemStack curiosStack = CuriosHelper.getItem(player, YyzBackpackHelper::isYyzBackpack);
            if (!curiosStack.isEmpty()) {
                return curiosStack;
            }
        }

        return ItemStack.EMPTY;
    }

    private static boolean isSupportedBackpack(ItemStack stack) {
        if (stack.isEmpty()) return false;
        return isSophisticatedBackpack(stack) || isTravelersBackpack(stack) || isYyzBackpack(stack); // Добавляйте новые через ||
    }

    public static IAmmoContext findAmmo(Player player, AmmoHolder id) {
        IItemHandler inventory = getBackpackInventory(player);
        if (inventory == null) return AmmoContext.NONE;

        for (int i = 0; i < inventory.getSlots(); i++) {
            ItemStack stack = inventory.getStackInSlot(i);
            if (InventoryUtil.isAmmo(stack, id)) {
                return new YyzBackpackAmmoContext(stack, inventory, i);
            }
        }
        return AmmoContext.NONE;
    }

    public static IAmmoContext findMagazine(Player player, AmmoHolder id) {
        IItemHandler inventory = getBackpackInventory(player);
        if (inventory == null) return AmmoContext.NONE;

        ItemStack bestAmmo = null;
        int bestSlot = -1;

        for (int i = 0; i < inventory.getSlots(); i++) {
            ItemStack stack = inventory.getStackInSlot(i);
            if (!InventoryUtil.isAmmo(stack, id)) continue;

            if (stack.getDamageValue() == 0) {
                return new YyzBackpackAmmoContext(stack, inventory, i);
            }

            if (bestAmmo == null || (stack.getDamageValue() < bestAmmo.getDamageValue())) {
                bestAmmo = stack;
                bestSlot = i;
            }
        }

        return bestAmmo == null ? AmmoContext.NONE : new YyzBackpackAmmoContext(bestAmmo, inventory, bestSlot);
    }
}