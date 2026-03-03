package com.nukateam.ntgl.common.util.helpers.compatibility.backpack;

import com.nukateam.ntgl.Ntgl;
import com.nukateam.ntgl.common.data.holders.AmmoHolder;
import com.nukateam.ntgl.common.util.helpers.compatibility.CuriosHelper;
import com.nukateam.ntgl.common.util.helpers.context.AmmoContext;
import com.nukateam.ntgl.common.util.helpers.context.IAmmoContext;
import com.nukateam.ntgl.common.util.helpers.context.ItemHandlerAmmoContext;
import com.nukateam.ntgl.common.util.util.InventoryUtil;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.items.IItemHandler;
import net.p3pp3rf1y.sophisticatedbackpacks.backpack.BackpackItem;
import net.p3pp3rf1y.sophisticatedbackpacks.backpack.wrapper.BackpackWrapper;
import net.p3pp3rf1y.sophisticatedbackpacks.backpack.wrapper.IBackpackWrapper;

import javax.annotation.Nullable;

public class SophisticatedHelper {

    @Nullable
    private static ItemStack getBackpackItem(Player player) {
        if(Ntgl.curiosLoaded){
            var backpackItem = CuriosHelper.getItem(player, stack -> stack.getItem() instanceof BackpackItem);
            if(backpackItem != null) return backpackItem;
        }

        var chestStack = player.getItemBySlot(EquipmentSlot.CHEST);
        if(chestStack.getItem() instanceof BackpackItem) {
            return chestStack;
        }
        return null;
    }

    @Nullable
    public static IBackpackWrapper getBackpackWrapper(ItemStack stack) {
        if (!(stack.getItem() instanceof BackpackItem)) {
            return null;
        }

        // В 1.21.1 используется статический метод get из BackpackWrapper
        return BackpackWrapper.fromStack(stack);
    }

    @Nullable
    private static IItemHandler getBackpackInventory(Player player) {
        ItemStack backpackStack = getBackpackItem(player);

        if (backpackStack != null) {
            IBackpackWrapper wrapper = getBackpackWrapper(backpackStack);
            return wrapper != null ? wrapper.getInventoryHandler() : null;
        }
        return null;
    }

    public static IAmmoContext findAmmo(Player player, AmmoHolder id) {
        IItemHandler inventory = getBackpackInventory(player);

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
        IItemHandler inventory = getBackpackInventory(player);
        if (inventory == null) {
            return AmmoContext.NONE;
        }

        ItemStack ammo = null;
        int slotIndex = -1;

        for (int i = 0; i < inventory.getSlots(); i++) {
            ItemStack stack = inventory.getStackInSlot(i);
            if (InventoryUtil.isAmmo(stack, id)) {
                if (stack.getDamageValue() == 0) {
                    return new ItemHandlerAmmoContext(stack, inventory);
                }
                if (ammo == null || (stack.getDamageValue() < ammo.getDamageValue() && ammo.getDamageValue() < ammo.getMaxDamage())) {
                    ammo = stack;
                    slotIndex = i;
                }
            }
        }

        return ammo == null ? AmmoContext.NONE : new ItemHandlerAmmoContext(ammo, inventory);
    }
}