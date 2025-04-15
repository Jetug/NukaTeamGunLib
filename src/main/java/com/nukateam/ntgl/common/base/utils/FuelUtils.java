package com.nukateam.ntgl.common.base.utils;

import com.nukateam.ntgl.common.base.holders.FuelType;
import com.nukateam.ntgl.common.util.util.GunData;
import com.nukateam.ntgl.common.util.util.GunModifierHelper;
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
        var allFuel = GunModifierHelper.getFuelTypes(data);
        for (var fuelType : allFuel) {
            var fuel = getFuel(data.gun, fuelType);
            if (fuel <= 0)
                return false;
        }
        return true;
    }

    public static boolean isFull(GunData data, FuelType fuelType) {
        var fuel = getFuel(data.gun, fuelType);
        var max = GunModifierHelper.getMaxFuel(data, fuelType);
        return fuel >= max;
    }

    public static int getFuel(ItemStack stack, FuelType fuelType) {
        var fuelTag = getOrCreateFuelTag(stack);

        if(fuelTag.contains(fuelType.toString(), Tag.TAG_INT))
            return fuelTag.getInt(fuelType.toString());
        return 0;
    }

    public static void setFuel(ItemStack stack, FuelType fuelType, int value) {
        var tag = stack.getOrCreateTag();

        var fuelTag = getOrCreateFuelTag(stack);
        fuelTag.putInt(fuelType.toString(), value);

        tag.put(FUEL, fuelTag);
        stack.setTag(tag);
    }

    public static void addFuel(GunData data, FuelType fuelType, int value) {
        var oldValue = getFuel(data.gun, fuelType);
        var max = GunModifierHelper.getMaxFuel(data, fuelType);
        setFuel(data.gun, fuelType, Mth.clamp(oldValue + value, 0, max));
    }

    public static float getFuelPercent(ItemStack stack, FuelType fuelType, GunData gunData) {
        var fuel = getFuel(stack, fuelType);
        var maxFuel = GunModifierHelper.getMaxFuel(gunData, fuelType);
        var fuelPercent = (fuel / (float) maxFuel);
        return fuelPercent;
    }
}
