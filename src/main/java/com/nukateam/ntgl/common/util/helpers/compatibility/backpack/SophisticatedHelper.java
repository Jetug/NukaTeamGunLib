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
import net.p3pp3rf1y.sophisticatedbackpacks.api.CapabilityBackpackWrapper;
import net.p3pp3rf1y.sophisticatedbackpacks.backpack.BackpackItem;
import net.p3pp3rf1y.sophisticatedbackpacks.backpack.wrapper.IBackpackWrapper;

import javax.annotation.Nullable;

@SuppressWarnings({"UnstableApiUsage", "removal"})
public class SophisticatedHelper {
    public static IAmmoContext findAmmo(Player player, AmmoHolder id) {
        var inventory = getBackpackInventory(player);

        if (inventory == null)
            return AmmoContext.NONE;

        for (int i = 0; i < inventory.getSlots(); i++) {
            var stack = inventory.getStackInSlot(i);
            if (InventoryUtil.isAmmo(stack, id)) {
                return new ItemHandlerAmmoContext(stack, inventory);
            }
        }

        return AmmoContext.NONE;
    }

    public static IAmmoContext findMagazine(Player player, AmmoHolder id) {
        var inventory = getBackpackInventory(player);
        if (inventory == null) {
            return AmmoContext.NONE;
        }

        ItemStack ammo = null;

        for (int i = 0; i < inventory.getSlots(); i++) {
            var stack = inventory.getStackInSlot(i);
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
    private static ItemStack getBackpackItem(Player player) {
        if(Ntgl.curiosLoaded){
            var backpackItem = CuriosHelper.getItem(player, stack -> stack.getItem() instanceof BackpackItem);
            if(backpackItem != null) return backpackItem;
        }

        var chestStack = player.getItemBySlot(EquipmentSlot.CHEST);
        if(chestStack.getItem() instanceof BackpackItem) {
            return chestStack;
        }
        else return null;
    }

    public static IBackpackWrapper getBackpackWrapper(ItemStack stack) {
        return stack.getCapability(CapabilityBackpackWrapper.getCapabilityInstance())
                .resolve()
                .orElse(null);
    }

    @Nullable
    private static IItemHandler getBackpackInventory(Player player) {
        var backpackStack = getBackpackItem(player);

        if (backpackStack != null) {
            var wrapper = getBackpackWrapper(backpackStack);
            return wrapper != null ? wrapper.getInventoryHandler() : null;
        }
        return null;
    }

}