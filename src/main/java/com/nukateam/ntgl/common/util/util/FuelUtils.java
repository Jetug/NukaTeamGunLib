package com.nukateam.ntgl.common.util.util;

import com.nukateam.ntgl.common.data.holders.AmmoHolder;
import com.nukateam.ntgl.common.data.WeaponData;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
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

    public static boolean hasFuel(WeaponData data){
        return hasFuel(data, true);
    }

    public static boolean hasFuel(WeaponData data, boolean requareAll){
        var allFuel = WeaponModifierHelper.getAllFuel(data);
        for (var fuelType : allFuel) {
            var isMandatory = WeaponModifierHelper.isFuelMandatory(fuelType.getId(), data);

            if(isMandatory || requareAll) {
                if(!hasFuel(fuelType.getId(), data))
                    return false;
            }
        }
        return true;
    }

    public static boolean hasFuel(ResourceLocation id, WeaponData data){
        var amount = WeaponModifierHelper.getFuelAmountPerUse(id, data);
        var fuel = getFuel(data.weapon, AmmoHolder.getType(id));
        return fuel >= amount;
    }

    public static boolean isFull(WeaponData data, AmmoHolder ammoHolder) {
        var fuel = getFuel(data.weapon, ammoHolder);
        var max = WeaponModifierHelper.getMaxFuel(ammoHolder.getId(), data);
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

    public static void addFuel(WeaponData data, AmmoHolder ammoHolder, int value) {
        var oldValue = getFuel(data.weapon, ammoHolder);
        var max = WeaponModifierHelper.getMaxFuel(ammoHolder.getId(), data);
        setFuel(data.weapon, ammoHolder, Mth.clamp(oldValue + value, 0, max));
    }

    public static void consumeFuel(AmmoHolder ammoHolder, WeaponData data) {
        var oldValue = getFuel(data.weapon, ammoHolder);
        var max = WeaponModifierHelper.getMaxFuel(ammoHolder.getId(), data);
        var value = WeaponModifierHelper.getFuelAmountPerUse(ammoHolder.getId(), data);
        setFuel(data.weapon, ammoHolder, Mth.clamp(oldValue - value, 0, max));
    }

    public static float getFuelPercent(ItemStack stack, AmmoHolder ammoHolder, WeaponData data) {
        var fuel = getFuel(stack, ammoHolder);
        var maxFuel = WeaponModifierHelper.getMaxFuel(ammoHolder.getId(), data);
        var fuelPercent = (fuel / (float) maxFuel);
        return fuelPercent;
    }
}
