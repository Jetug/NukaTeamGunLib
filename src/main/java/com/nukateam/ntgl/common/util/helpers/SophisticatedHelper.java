package com.nukateam.ntgl.common.util.helpers;

import com.mojang.datafixers.util.Pair;
import com.nukateam.ntgl.common.base.AmmoContext;
import com.nukateam.ntgl.common.base.IAmmoContext;
import com.nukateam.ntgl.common.base.SophisticatedAmmoContext;
import com.nukateam.ntgl.common.data.config.gun.Gun;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.IItemHandlerModifiable;
import net.p3pp3rf1y.sophisticatedbackpacks.api.CapabilityBackpackWrapper;
import net.p3pp3rf1y.sophisticatedbackpacks.backpack.BackpackItem;
import net.p3pp3rf1y.sophisticatedbackpacks.backpack.wrapper.IBackpackWrapper;
import net.p3pp3rf1y.sophisticatedbackpacks.common.gui.BackpackContainer;
import net.p3pp3rf1y.sophisticatedbackpacks.common.gui.BackpackContext;
import net.p3pp3rf1y.sophisticatedbackpacks.util.PlayerInventoryProvider;
import top.theillusivec4.curios.api.CuriosApi;

import javax.annotation.Nullable;
import java.util.concurrent.atomic.AtomicReference;

@SuppressWarnings({"UnstableApiUsage", "removal"})
public class SophisticatedHelper {
    @Nullable
    private static ItemStack getBackpackItem(Player player) {
        var backpackCurio = CuriosApi.getCuriosHelper().findFirstCurio(player, stack -> stack.getItem() instanceof BackpackItem);
        var chestStack = player.getItemBySlot(EquipmentSlot.CHEST);

        if(backpackCurio.isPresent())
            return backpackCurio.get().stack();
        else if(chestStack.getItem() instanceof BackpackItem) {
            return chestStack;
        }
        else return null;
    }

    public static IBackpackWrapper getBackpackWrapper(ItemStack stack) {
        return stack.getCapability(CapabilityBackpackWrapper.getCapabilityInstance())
                .resolve()
                .orElse(null);
    }

//    private static ItemStack findAndOpenFirstBackpack(Player player) {
//        var stack = new AtomicReference<ItemStack>(null);
//
//        PlayerInventoryProvider.get().runOnBackpacks(player, (backpack, inventoryName, identifier, slot) -> {
//            stack.set(backpack);
//            return true;
//        });
//
//        return stack.get();
//    }

    @Nullable
    private static IItemHandler getBackpackInventory(Player player) {
        var backpackStack = getBackpackItem(player);

        if (backpackStack != null) {
            var wrapper = getBackpackWrapper(backpackStack);
            return wrapper != null ? wrapper.getInventoryHandler() : null;
        }
        return null;
    }

    public static IAmmoContext findAmmo(Player player, ResourceLocation id) {
        var inventory = getBackpackInventory(player);

        if (inventory == null)
            return SophisticatedAmmoContext.NONE;


        for (int i = 0; i < inventory.getSlots(); i++) {
            var stack = inventory.getStackInSlot(i);
            if (Gun.isAmmo(stack, id)) {
                return new SophisticatedAmmoContext(stack, inventory);
            }
        }

        return SophisticatedAmmoContext.NONE;
    }

    public static IAmmoContext findMagazine(Player player, ResourceLocation id) {
        var inventory = getBackpackInventory(player);
        if (inventory == null) {
            return SophisticatedAmmoContext.NONE;
        }

        ItemStack ammo = null;

        for (int i = 0; i < inventory.getSlots(); i++) {
            var stack = inventory.getStackInSlot(i);
            if (Gun.isAmmo(stack, id)) {
                if (stack.getDamageValue() == 0) {
                    return new SophisticatedAmmoContext(stack, inventory);
                }
                if (ammo == null || (stack.getDamageValue() < ammo.getDamageValue() && ammo.getDamageValue() < ammo.getMaxDamage())) {
                    ammo = stack;
                }
            }
        }

        return ammo == null ? SophisticatedAmmoContext.NONE : new SophisticatedAmmoContext(ammo, inventory);
    }
}