package com.nukateam.ntgl.common.util.helpers.compatibility;


import com.mrcrayfish.backpacked.inventory.BackpackInventory;
import com.nukateam.ntgl.common.data.holders.AmmoHolder;
import com.nukateam.ntgl.common.util.helpers.context.AmmoContext;
import com.nukateam.ntgl.common.util.helpers.context.IAmmoContext;
import com.nukateam.ntgl.common.util.helpers.context.SophisticatedAmmoContext;
import com.nukateam.ntgl.common.util.helpers.context.TravelersBackpackAmmoContext;
import com.nukateam.ntgl.common.util.util.InventoryUtil;
import com.tiviacz.travelersbackpack.capability.*;
import com.tiviacz.travelersbackpack.inventory.BackpackWrapper;
import com.tiviacz.travelersbackpack.inventory.StorageAccessWrapper;
import com.tiviacz.travelersbackpack.inventory.upgrades.pickup.AutoPickupUpgrade;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemHandlerHelper;

import javax.annotation.Nullable;

public class TravelersHelper {
    public static IAmmoContext findAmmo(Player player, AmmoHolder id) {
        var inventory = getBackpackInventory(player);

        if (inventory == null)
            return TravelersBackpackAmmoContext.NONE;

        for (int i = 0; i < inventory.getSlots(); i++) {
            ItemStack stack = inventory.getStackInSlot(i);
            if (InventoryUtil.isAmmo(stack, id)) {
                return new TravelersBackpackAmmoContext(stack, inventory, i);
            }
        }

        return TravelersBackpackAmmoContext.NONE;
    }

    public static IAmmoContext findMagazine(Player player, AmmoHolder id) {
        var inventory = getBackpackInventory(player);

        if (inventory == null)
            return TravelersBackpackAmmoContext.NONE;

        ItemStack ammo = null;
        int ammoSlot = -1;

        for (int i = 0; i < inventory.getSlots(); i++) {
            ItemStack stack = inventory.getStackInSlot(i);
            if (InventoryUtil.isAmmo(stack, id)) {
                if (stack.getDamageValue() == 0) {
                    return new TravelersBackpackAmmoContext(stack, inventory, i);
                }
                if (ammo == null || (stack.getDamageValue() < ammo.getDamageValue() && ammo.getDamageValue() < ammo.getMaxDamage())) {
                    ammo = stack;
                    ammoSlot = i;
                }
            }
        }

        return ammo == null ? TravelersBackpackAmmoContext.NONE : new TravelersBackpackAmmoContext(ammo, inventory, ammoSlot);
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
