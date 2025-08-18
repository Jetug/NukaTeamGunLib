package com.nukateam.ntgl.common.util.util;

import com.nukateam.ntgl.common.data.holders.AmmoHolder;
import com.nukateam.ntgl.common.data.GunData;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;

public class FuelUtils {
    public static final String FUEL = "Fuel";

    public static CompoundTag getOrCreateFuelTag(ItemStack stack) {
        var tag = stack.getOrCreateTag();
        if(tag.contains(FUEL, Tag.TAG_COMPOUND)){
            return tag.getCompound(FUEL);
        }
        return new CompoundTag();
    }

    public static boolean hasFuel(GunData data){
        var allFuel = GunModifierHelper.getAllFuel(data);
        for (var fuelType : allFuel) {
            var fuel = getFuel(data.gun, fuelType);
            if (fuel <= 0)
                return false;
        }
        return true;
    }

    public static boolean isFull(GunData data, AmmoHolder ammoHolder) {
        var fuel = getFuel(data.gun, ammoHolder);
        var max = GunModifierHelper.getMaxFuel(data, ammoHolder);
        return fuel >= max;
    }

    public static int getFuel(ItemStack stack, AmmoHolder ammoHolder) {
        var fuelTag = getOrCreateFuelTag(stack);

        if(fuelTag.contains(ammoHolder.toString(), Tag.TAG_INT))
            return fuelTag.getInt(ammoHolder.toString());
        return 0;
    }

    public static void setFuel(ItemStack stack, AmmoHolder ammoHolder, int value) {
        var tag = stack.getOrCreateTag();

        var fuelTag = getOrCreateFuelTag(stack);
        fuelTag.putInt(ammoHolder.toString(), value);

        tag.put(FUEL, fuelTag);
        stack.setTag(tag);
    }

    public static void addFuel(GunData data, AmmoHolder ammoHolder, int value) {
        var oldValue = getFuel(data.gun, ammoHolder);
        var max = GunModifierHelper.getMaxFuel(data, ammoHolder);
        setFuel(data.gun, ammoHolder, Mth.clamp(oldValue + value, 0, max));
    }

    public static float getFuelPercent(ItemStack stack, AmmoHolder ammoHolder, GunData gunData) {
        var fuel = getFuel(stack, ammoHolder);
        var maxFuel = GunModifierHelper.getMaxFuel(gunData, ammoHolder);
        var fuelPercent = (fuel / (float) maxFuel);
        return fuelPercent;
    }
}
