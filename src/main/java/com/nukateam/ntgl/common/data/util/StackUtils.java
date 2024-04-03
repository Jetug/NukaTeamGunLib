package com.nukateam.ntgl.common.data.util;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;

public class StackUtils {
    public static final String DAMAGE = "Damage";

    public static int getItemDamage(ItemStack itemStack) {
        if (itemStack.hasTag()) {
            CompoundTag nbt = itemStack.getOrCreateTag();
            return nbt.getInt(DAMAGE);
        } else {
            return 0;
        }
    }

    public static void setItemDamage(ItemStack stack, int totalDamage) {
        CompoundTag nbt = stack.getOrCreateTag();
        nbt.putInt(DAMAGE, totalDamage);
    }

    public static void setDurability(ItemStack stack, int durability) {
        CompoundTag nbt = stack.getOrCreateTag();
        nbt.putInt(DAMAGE, stack.getMaxDamage() - durability);
    }

    public static void damageItem(ItemStack itemStack, int dmg) {
        int resultDamage = getItemDamage(itemStack) + dmg;
        setItemDamage(itemStack, Math.min(resultDamage, itemStack.getMaxDamage()));
    }

    public static int getDurability(ItemStack itemStack) {
        return itemStack.getMaxDamage() - itemStack.getDamageValue();
    }
}
