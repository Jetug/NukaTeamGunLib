package com.nukateam.ntgl.common.util.helpers;

import com.nukateam.ntgl.common.foundation.item.WeaponItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

public class RegistrationHelper {
    public static void registerGunOrDefault(CreativeModeTab.Output output, Item item) {
        if(!registerGun(output, item)) {
            output.accept(item);
        }
    }

    public static boolean registerGun(CreativeModeTab.Output output, Item item) {
        if (item instanceof WeaponItem weaponItem) {
            var stack = new ItemStack(weaponItem);
            weaponItem.setDefaultTag(stack);
            output.accept(stack);
            return true;
        }
        else return false;
    }
}
