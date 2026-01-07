package com.nukateam.ntgl.common.util.helpers;

import com.nukateam.ntgl.common.foundation.item.WeaponItem;
import com.nukateam.ntgl.common.foundation.item.interfaces.IWeapon;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

public class RegistrationHelper {
    public static void registerGunOrDefault(CreativeModeTab.Output output, Item item) {
        registerGunOrDefault(output, item, () -> output.accept(item));
    }

    public static void registerGunOrDefault(CreativeModeTab.Output output, Item item, Runnable def) {
        if(!registerGun(output, item))
            def.run();
    }

    public static boolean registerGun(CreativeModeTab.Output output, Item item) {
        if (item instanceof IWeapon weaponItem) {
            var stack = new ItemStack(item);
            weaponItem.setDefaultTag(stack);
            output.accept(stack);
            return true;
        }
        else return false;
    }
}
