package com.nukateam.ntgl.common.util.helpers;

import com.nukateam.ntgl.common.base.AmmoContext;
import com.nukateam.ntgl.common.data.config.gun.Gun;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.items.IItemHandler;
import net.p3pp3rf1y.sophisticatedbackpacks.api.CapabilityBackpackWrapper;
import net.p3pp3rf1y.sophisticatedbackpacks.backpack.BackpackItem;
import net.p3pp3rf1y.sophisticatedbackpacks.common.gui.BackpackContainer;
import net.p3pp3rf1y.sophisticatedbackpacks.common.gui.BackpackContext;
import net.p3pp3rf1y.sophisticatedbackpacks.util.PlayerInventoryProvider;
import top.theillusivec4.curios.api.CuriosApi;

import javax.annotation.Nullable;
import java.util.concurrent.atomic.AtomicReference;

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

    private static AtomicReference<BackpackContainer> findAndOpenFirstBackpack(ServerPlayer player) {
        var container = new AtomicReference<BackpackContainer>(null);
        PlayerInventoryProvider.get().runOnBackpacks(player, (backpack, inventoryName, identifier, slot) -> {
            BackpackContext.Item backpackContext = new BackpackContext.Item(inventoryName, identifier, slot);
            container.set(new BackpackContainer(-1, player, backpackContext));
            return true;
        });

        return container;
    }

    @Nullable
    private static IItemHandler getBackpackInventory(Player player) {
        var backpackStack = getBackpackItem(player);
        if (backpackStack != null) {
            return backpackStack.getCapability(CapabilityBackpackWrapper.getCapabilityInstance())
                    .resolve()
                    .map(wrapper -> wrapper.getInventoryHandler())
                    .orElse(null);
        }
        return null;
    }

    public static AmmoContext findAmmo(Player player, ResourceLocation id) {
        var inventory = getBackpackInventory(player);
        if (inventory == null)
            return AmmoContext.NONE;


        for (int i = 0; i < inventory.getSlots(); i++) {
            var stack = inventory.getStackInSlot(i);
            if (Gun.isAmmo(stack, id)) {
                return new AmmoContext(stack, null);
            }
        }

        return AmmoContext.NONE;
    }

    public static AmmoContext findMagazine(Player player, ResourceLocation id) {
        var inventory = getBackpackInventory(player);
        if (inventory == null) {
            return AmmoContext.NONE;
        }

        ItemStack ammo = null;

        for (int i = 0; i < inventory.getSlots(); i++) {
            var stack = inventory.getStackInSlot(i);
            if (Gun.isAmmo(stack, id)) {
                if (stack.getDamageValue() == 0) {
                    return new AmmoContext(stack, null);
                }
                if (ammo == null || (stack.getDamageValue() < ammo.getDamageValue() && ammo.getDamageValue() < ammo.getMaxDamage())) {
                    ammo = stack;
                }
            }
        }

        return ammo == null ? AmmoContext.NONE : new AmmoContext(ammo, null);
    }
}