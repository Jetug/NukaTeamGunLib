package com.nukateam.ntgl.common.util.util;

import com.nukateam.ntgl.common.data.GunData;
import com.nukateam.ntgl.common.foundation.crafting.WorkbenchIngredient;
import com.nukateam.ntgl.common.util.helpers.compatibility.BackpackHelper;
import com.nukateam.ntgl.common.util.helpers.context.AmmoContext;
import com.nukateam.ntgl.common.util.helpers.context.IAmmoContext;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.Container;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.registries.ForgeRegistries;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

/**
 * Author: MrCrayfish
 */
public class InventoryUtil {
    public static int getItemStackAmount(Player player, ItemStack find) {
        int count = 0;
        for (ItemStack stack : player.getInventory().items) {
            if (!stack.isEmpty() && areItemStacksEqualIgnoreCount(stack, find)) {
                count += stack.getCount();
            }
        }
        return count;
    }

    private static boolean areItemStacksEqualIgnoreCount(ItemStack source, ItemStack target) {
        if (source.getItem() != target.getItem()) {
            return false;
        } else if (source.getDamageValue() != target.getDamageValue()) {
            return false;
        } else if (source.getTag() == null && target.getTag() != null) {
            return false;
        } else {
            return (source.getTag() == null || source.getTag().equals(target.getTag())) && source.areCapsCompatible(target);
        }
    }

    public static boolean hasWorkstationIngredient(Player player, WorkbenchIngredient find) {
        int count = 0;
        for (ItemStack stack : player.getInventory().items) {
            if (!stack.isEmpty() && find.test(stack)) {
                count += stack.getCount();
            }
        }
        return find.getCount() <= count;
    }

    public static boolean removeWorkstationIngredient(Player player, WorkbenchIngredient find) {
        int amount = find.getCount();
        for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
            ItemStack stack = player.getInventory().getItem(i);
            if (!stack.isEmpty() && find.test(stack)) {
                if (amount - stack.getCount() < 0) {
                    stack.shrink(amount);
                    return true;
                } else {
                    amount -= stack.getCount();
                    player.getInventory().items.set(i, ItemStack.EMPTY);
                    if (amount == 0) return true;
                }
            }
        }
        return false;
    }

    public static IAmmoContext findPlayerAmmo(Player player, ResourceLocation id) {
        if (player.isCreative())
            return getCreativeAmmoContext(id);

        var context = findAmmo(player.getInventory(), id);
        if (!context.equals(AmmoContext.NONE))
            return context;

        return BackpackHelper.findAmmo(player, id);
    }

    public static AmmoContext findAmmo(Container inventory, ResourceLocation id){
        for (int i = 0; i < inventory.getContainerSize(); ++i) {
            var stack = inventory.getItem(i);
            if (isAmmo(stack, id)) {
                return new AmmoContext(stack, inventory);
            }
        }

        return AmmoContext.NONE;
    }

    public static IAmmoContext findPlayerMagazine(Player player, ResourceLocation id) {
        if (player.isCreative()) {
            return getCreativeAmmoContext(id);
        }

        var context = findMagazine(player.getInventory(), id);
        if (!context.equals(AmmoContext.NONE))
            return context;

        return BackpackHelper.findMagazine(player, id);
    }

    public static AmmoContext findMagazine(Container inventory, ResourceLocation id){
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

    public static boolean isAmmo(ItemStack stack, ResourceLocation id) {
        return stack != null && Objects.equals(ForgeRegistries.ITEMS.getKey(stack.getItem()), id);
    }

    /**
     * @return True if the second stack contains more ammo then the first
     */
    private static boolean hasMoreAmmo(ItemStack first, ItemStack second) {
        return second.getDamageValue() < first.getDamageValue() && first.getDamageValue() < first.getMaxDamage();
    }

    @NotNull
    public static AmmoContext getCreativeAmmoContext(ResourceLocation id) {
        var item = ForgeRegistries.ITEMS.getValue(id);
        var ammo = item != null ? new ItemStack(item, Integer.MAX_VALUE) : ItemStack.EMPTY;
        return new AmmoContext(ammo, null);
    }

    public static IAmmoContext findAmmo(LivingEntity entity, ItemStack weapon) {
        var data = new GunData(weapon, entity);
        var id = GunStateHelper.getAmmoId(data);

        if (entity instanceof Player player) {
            var context = findPlayerAmmo(player, id);

            if(context == AmmoContext.NONE){
                var set = GunModifierHelper.getAmmoItems(data);
                for (var value: set) {
                    if(!value.equals(id) && GunStateHelper.getAmmoCount(weapon) == 0){
                        id = value;
                        context = findPlayerAmmo(player, id);
                        if(context != AmmoContext.NONE) {
                            GunStateHelper.setCurrentAmmo(data, id);
                            return context;
                        }
                    }
                }
            }

            return context;
        }
        return getCreativeAmmoContext(id);
    }

    public static IAmmoContext findMagazine(LivingEntity entity, ItemStack weapon) {
        var data = new GunData(weapon, entity);
        var id = GunStateHelper.getAmmoId(data);

        if (entity instanceof Player player) {
            var context = findPlayerMagazine(player, id);

            if(context == AmmoContext.NONE){
                var set = GunModifierHelper.getAmmoItems(data);
                for (var value: set) {
                    if(!value.equals(id) && GunStateHelper.getAmmoCount(weapon) == 0){
                        id = value;
                        context = findPlayerMagazine(player, id);
                        if(context != AmmoContext.NONE) {
                            GunStateHelper.setCurrentAmmo(data, id);
                            return context;
                        }
                    }
                }
            }

            return context;
        }

        return getCreativeAmmoContext(id);
    }
}
