package com.nukateam.ntgl.common.util.helpers;

import com.nukateam.ntgl.common.foundation.init.NtglComponents;
import com.nukateam.ntgl.common.foundation.item.WeaponItem;
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
        if (item instanceof WeaponItem weaponItem) {
            var stack = new ItemStack(weaponItem);
            var tag = NtglComponents.getWeaponTag(stack);
            weaponItem.setDefaultTag(tag);
            NtglComponents.setWeaponTag(stack, tag);
            output.accept(stack);
            return true;
        }
        else return false;
    }
}
