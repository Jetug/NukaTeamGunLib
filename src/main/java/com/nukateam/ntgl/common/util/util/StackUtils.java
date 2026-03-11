package com.nukateam.ntgl.common.util.util;

import net.minecraft.world.item.ItemStack;

public class StackUtils {
    public static void setDurability(ItemStack stack, int durability) {
        stack.setDamageValue(stack.getMaxDamage() - durability);
    }

    public static int getDurability(ItemStack itemStack) {
        return itemStack.getMaxDamage() - itemStack.getDamageValue();
    }
}
