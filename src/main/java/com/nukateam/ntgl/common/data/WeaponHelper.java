package com.nukateam.ntgl.common.data;

import com.nukateam.ntgl.common.foundation.item.interfaces.IWeapon;
import net.minecraft.world.item.Item;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.ArrayList;

public class WeaponHelper {
    private static ArrayList<Item> weaponItems = new ArrayList<>();

    public static ArrayList<Item> getWeaponItems() {
        if(!weaponItems.isEmpty()) return weaponItems;

        var foundItems = new ArrayList<Item>();

        ForgeRegistries.ITEMS.forEach(item ->{
            if (item instanceof IWeapon){
                foundItems.add(item);
            }
        });

        weaponItems = foundItems;
        return foundItems;
    }
}
