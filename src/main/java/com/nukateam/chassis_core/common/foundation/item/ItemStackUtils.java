package com.nukateam.chassis_core.common.foundation.item;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;

public abstract class ItemStackUtils {
    private static final String DAMAGE_KEY = "Damage";

    public static int getItemDamage(ItemStack itemStack) {
        if (itemStack.hasTag()) {
            CompoundTag nbt = itemStack.getOrCreateTag();
            return nbt.getInt(DAMAGE_KEY);
        } else return 0;
    }

    public static void setItemDamage(ItemStack head, int totalDamage) {
        CompoundTag nbt = head.getOrCreateTag();
        nbt.putInt(DAMAGE_KEY, totalDamage);
    }

    public static void damageItem(ItemStack itemStack, int dmg) {
        var resultDamage = getItemDamage(itemStack) + dmg;
        setItemDamage(itemStack, Math.min(resultDamage, itemStack.getMaxDamage()));
    }
}
