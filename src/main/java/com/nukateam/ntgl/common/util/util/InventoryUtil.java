package com.nukateam.ntgl.common.util.util;

import com.nukateam.ntgl.common.data.WeaponData;
import com.nukateam.ntgl.common.data.holders.AmmoHolder;
import com.nukateam.ntgl.common.foundation.components.NtglComponents;
import com.nukateam.ntgl.common.util.helpers.compatibility.backpack.BackpackHelper;
import com.nukateam.ntgl.common.util.helpers.context.AmmoContext;
import com.nukateam.ntgl.common.util.helpers.context.IAmmoContext;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.Container;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

/**
 * Author: MrCrayfish
 */
public class InventoryUtil {
    public static IAmmoContext findPlayerAmmo(Player player, AmmoHolder id) {
        var context = findAmmo(player.getInventory(), id);
        if (!context.equals(AmmoContext.NONE))
            return context;

        return BackpackHelper.findAmmo(player, id);
    }

    public static AmmoContext findAmmo(Container inventory, AmmoHolder id){
        for (int i = 0; i < inventory.getContainerSize(); ++i) {
            var stack = inventory.getItem(i);
            if (isAmmo(stack, id)) {
                return new AmmoContext(stack, inventory);
            }
        }

        return AmmoContext.NONE;
    }

    public static IAmmoContext findPlayerMagazine(Player player, AmmoHolder id) {
        var context = findMagazine(player.getInventory(), id);
        if (!context.equals(AmmoContext.NONE))
            return context;

        return BackpackHelper.findMagazine(player, id);
    }

    public static AmmoContext findMagazine(Container inventory, AmmoHolder id){
        ItemStack ammoStack = null;

        for (int i = 0; i < inventory.getContainerSize(); ++i) {
            var foundStack = inventory.getItem(i);

            if (isAmmo(foundStack, id)) {
                if (foundStack.getDamageValue() == 0)
                    return new AmmoContext(foundStack, inventory);

                if (ammoStack == null || hasMoreAmmo(ammoStack, foundStack))
                    ammoStack = foundStack;
            }
        }

        if (ammoStack != null)
            return new AmmoContext(ammoStack, inventory);

        return AmmoContext.NONE;
    }

    public static boolean isAmmo(ItemStack stack, AmmoHolder id) {
        return stack != null && id.isAcceptable(stack);
    }

    /**
     * @return True if the second stack contains more ammo then the first
     */
    private static boolean hasMoreAmmo(ItemStack first, ItemStack second) {
        return second.getDamageValue() < first.getDamageValue() && first.getDamageValue() < first.getMaxDamage();
    }

    @NotNull
    public static AmmoContext getCreativeAmmoContext(ResourceLocation id) {
        var item = BuiltInRegistries.ITEM.get(id);
        var ammo = item != null ? new ItemStack(item, Integer.MAX_VALUE) : ItemStack.EMPTY;
        return new AmmoContext(ammo, null);
    }

    public static IAmmoContext findAmmo(AmmoHolder ammoHandler, WeaponData data) {
        if (data.wielder instanceof Player player) {
            var context = findPlayerAmmo(player, ammoHandler);

            if(context == AmmoContext.NONE){
                var set = WeaponModifierHelper.getAmmoItems(data);
                for (var value: set) {
                    if(!value.equals(ammoHandler.getId()) && WeaponStateHelper.getAmmoCount(data) == 0){
                        ammoHandler = value;
                        context = findPlayerAmmo(player, ammoHandler);
                        if(context != AmmoContext.NONE) {
                            WeaponStateHelper.setCurrentAmmo(data, ammoHandler.getId());
                            return context;
                        }
                    }
                }
            }
            return context;
        }
        return AmmoContext.NONE;
    }

    public static IAmmoContext findAmmo(LivingEntity entity, ItemStack weapon) {
        var data = new WeaponData(weapon, entity);
        var ammoHandler = WeaponStateHelper.getCurrentAmmo(data);

        return findAmmo(ammoHandler, data);
    }

    public static IAmmoContext findMagazine(LivingEntity entity, ItemStack weapon) {
        var data = new WeaponData(weapon, entity);
        var ammoHandler = WeaponStateHelper.getCurrentAmmo(data);

        if (entity instanceof Player player) {
            var context = findPlayerMagazine(player, ammoHandler);

            if(context == AmmoContext.NONE){
                var set = WeaponModifierHelper.getAmmoItems(data);
                for (var value: set) {
                    if(!value.equals(ammoHandler) && WeaponStateHelper.getAmmoCount(data) == 0){
                        ammoHandler = value;
                        context = findPlayerMagazine(player, ammoHandler);
                        if(context != AmmoContext.NONE) {
                            WeaponStateHelper.setCurrentAmmo(data, ammoHandler.getId());
                            return context;
                        }
                    }
                }
            }

            return context;
        }

        return AmmoContext.NONE;
    }

    public static boolean hasAmmo(LivingEntity entity, ItemStack weapon) {
        if(entity instanceof Player player && !player.isCreative()) {
            return !findAmmo(player, weapon).stack().isEmpty();
        }
        return true;
    }
}
