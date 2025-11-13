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
import net.neoforged.neoforge.common.capabilities.ForgeCapabilities;
import net.neoforged.neoforge.common.util.LazyOptional;
import net.neoforged.neoforge.items.IItemHandler;
import net.p3pp3rf1y.sophisticatedbackpacks.backpack.BackpackItem;

import javax.annotation.Nullable;

public class YyzBackpackHelper {
    public static IAmmoContext findAmmo(Player player, AmmoHolder id) {
        IItemHandler inventory = getBackpackInventory(player);

        if (inventory == null)
            return AmmoContext.NONE;

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

        if (inventory == null)
            return AmmoContext.NONE;

        ItemStack ammo = null;
        int ammoSlot = -1;

        for (int i = 0; i < inventory.getSlots(); i++) {
            ItemStack stack = inventory.getStackInSlot(i);
            if (InventoryUtil.isAmmo(stack, id)) {
                if (stack.getDamageValue() == 0) {
                    return new YyzBackpackAmmoContext(stack, inventory, i);
                }
                if (ammo == null || (stack.getDamageValue() < ammo.getDamageValue() && ammo.getDamageValue() < ammo.getMaxDamage())) {
                    ammo = stack;
                    ammoSlot = i;
                }
            }
        }

        return ammo == null ? AmmoContext.NONE : new YyzBackpackAmmoContext(ammo, inventory, ammoSlot);
    }

    @Nullable
    private static IItemHandler getBackpackInventory(Player player) {
        ItemStack chestStack = player.getItemBySlot(EquipmentSlot.CHEST);

        if (isYyzBackpack(chestStack)) {
            LazyOptional<IItemHandler> backpackInventory = chestStack.getCapability(ForgeCapabilities.ITEM_HANDLER);
            return backpackInventory.orElse(null);
        }

        if (Ntgl.curiosLoaded) {
            ItemStack backpackCurio = CuriosHelper.getItem(player, YyzBackpackHelper::isYyzBackpack);
            if (backpackCurio != null) {
                LazyOptional<IItemHandler> backpackInventory = backpackCurio.getCapability(ForgeCapabilities.ITEM_HANDLER);
                return backpackInventory.orElse(null);
            }
        }

        return null;
    }

    private static boolean isYyzBackpack(ItemStack stack) {
        if (stack.isEmpty()) return false;
        return stack.getItem() instanceof BackpackItem;
    }
}