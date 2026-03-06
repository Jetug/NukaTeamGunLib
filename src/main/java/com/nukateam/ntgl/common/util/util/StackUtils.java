package com.nukateam.ntgl.common.util.util;

import com.nukateam.ntgl.common.foundation.init.NtglComponents;
import net.minecraft.world.item.ItemStack;

public class StackUtils {
    public static final String DAMAGE = "Damage";

    public static int getItemDamage(ItemStack stack) {
        var tag = NtglComponents.getWeaponTag(stack);
        if (tag != null) {
            return tag.getInt(DAMAGE);
        } else return 0;
    }

    public static void setItemDamage(ItemStack stack, int totalDamage) {
        var tag = NtglComponents.getWeaponTag(stack);
        tag.putInt(DAMAGE, totalDamage);
    }

    public static void setDurability(ItemStack stack, int durability) {
        var tag = NtglComponents.getWeaponTag(stack);
        tag.putInt(DAMAGE, stack.getMaxDamage() - durability);
    }

    public static void damageItem(ItemStack itemStack, int dmg) {
        var resultDamage = getItemDamage(itemStack) + dmg;
        setItemDamage(itemStack, Math.min(resultDamage, itemStack.getMaxDamage()));
    }

    public static int getDurability(ItemStack itemStack) {
        return itemStack.getMaxDamage() - itemStack.getDamageValue();
    }
}
