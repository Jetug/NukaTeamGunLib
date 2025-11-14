package com.nukateam.chassis_core.common.foundation.item;

import com.nukateam.ntgl.common.foundation.components.NtglComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;

public class StackUtils {
    public static final String DEFAULT = "default";
    public static final String VARIANT = "variant";
    public static final String ATTACHMENTS = "mods";
    private static final String DAMAGE_KEY = "Damage";

    public static String getVariant(ItemStack stack) {
        var tag = NtglComponents.getWeaponTag(stack);
        return tag.contains(VARIANT) ? tag.getString(VARIANT) : DEFAULT;
    }

    public static boolean hasAttachment(ItemStack stack, String mod) {
        var mods = getAttachments(stack);
        return mods.contains(mod);
    }

    public static ArrayList<String> getAttachments(ItemStack stack) {
        var tag = NtglComponents.getWeaponTag(stack);
        if (tag == null || !tag.contains(ATTACHMENTS)) return new ArrayList<>();
        var attachments = tag.getCompound(ATTACHMENTS);
        var values = new ArrayList<String>();

        for (var key : attachments.getAllKeys()) {
            values.add(attachments.getString(key));
        }

        return values;
    }

    public static int getItemDamage(ItemStack stack) {
        var tag = NtglComponents.getWeaponTag(stack);
        if (tag != null) {
            return tag.getInt(DAMAGE_KEY);
        } else return 0;
    }

    public static void setItemDamage(ItemStack stack, int totalDamage) {
        var tag = NtglComponents.getWeaponTag(stack);
        tag.putInt(DAMAGE_KEY, totalDamage);
    }

    public static void damageItem(ItemStack itemStack, int dmg) {
        var resultDamage = getItemDamage(itemStack) + dmg;
        setItemDamage(itemStack, Math.min(resultDamage, itemStack.getMaxDamage()));
    }
}
