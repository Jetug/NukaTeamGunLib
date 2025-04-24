package com.nukateam.ntgl.common.util.helpers;

import com.nukateam.ntgl.common.base.AmmoContext;
import com.nukateam.ntgl.common.data.config.gun.Gun;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.items.IItemHandler;
import net.p3pp3rf1y.sophisticatedbackpacks.backpack.BackpackItem;
import net.p3pp3rf1y.sophisticatedbackpacks.backpack.wrapper.IBackpackWrapper;
import top.theillusivec4.curios.api.CuriosApi;

import javax.annotation.Nullable;

@SuppressWarnings("ALL")
public class SophisticatedBackpackHelper {
    @Nullable
    private static ItemStack getBackpackWrapper(Player player) {
        var backpackCurio =
                CuriosApi.getCuriosHelper().findFirstCurio(player, stack -> stack.getItem() instanceof BackpackItem);

        var chestStack = player.getItemBySlot(EquipmentSlot.CHEST);

        if(backpackCurio.isPresent())
            return backpackCurio.get().stack();
        else if(chestStack.getItem() instanceof BackpackItem) {
            return chestStack;
        }
        else return null;
    }

    public static AmmoContext findAmmo(Player player, ResourceLocation id) {
        var backpackWrapper = getBackpackWrapper(player);

        var item = (BackpackItem)backpackWrapper.getItem();
        item.getBackpackWrapper()
        if (backpackWrapper == null) {
            return AmmoContext.NONE;
        }

        var inventory = backpackWrapper.getInventoryHandler();

        for (int i = 0; i < inventory.getSlots(); i++) {
            ItemStack stack = inventory.getStackInSlot(i);
            if (Gun.isAmmo(stack, id)) {
                return new AmmoContext(stack, inventory);
            }
        }

        return AmmoContext.NONE;
    }

    public static AmmoContext findMagazine(Player player, ResourceLocation id) {
        IBackpackWrapper backpackWrapper = getBackpackWrapper(player);
        if (backpackWrapper == null) {
            return AmmoContext.NONE;
        }

        IItemHandler inventory = backpackWrapper.getInventoryHandler();
        ItemStack ammo = null;

        for (int i = 0; i < inventory.getSlots(); i++) {
            ItemStack stack = inventory.getStackInSlot(i);
            if (Gun.isAmmo(stack, id)) {
                if (stack.getDamageValue() == 0) {
                    return new AmmoContext(stack, inventory);
                }
                if (ammo == null || (stack.getDamageValue() < ammo.getDamageValue() && ammo.getDamageValue() < ammo.getMaxDamage())) {
                    ammo = stack;
                }
            }
        }

        return ammo == null ? AmmoContext.NONE : new AmmoContext(ammo, inventory);
    }
}