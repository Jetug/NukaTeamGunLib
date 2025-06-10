package com.nukateam.ntgl.common.util.util;

import com.nukateam.ntgl.common.base.holders.AmmoType;
import com.nukateam.ntgl.common.base.holders.FireMode;
import com.nukateam.ntgl.common.data.config.Ammo;
import com.nukateam.ntgl.common.foundation.item.interfaces.IAmmo;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Set;

import static net.minecraftforge.registries.ForgeRegistries.ITEMS;

public class GunStateHelper {
    public static final String AMMO_TAG = "Ammo";
    public static final String FIRE_MODE = "FireMode";

    public static void switchAmmo(GunData data){
        var ammoItems = GunModifierHelper.getAmmoItems(data);
        var current = getAmmoId(data);
        var newAmmo = cycleSet(ammoItems, current);

        setCurrentAmmo(data, newAmmo);
    }

    public static void setCurrentAmmo(GunData data, ResourceLocation ammo) {
        var tag = data.gun.getOrCreateTag();
        tag.putString(AMMO_TAG, ammo.toString());
        data.gun.setTag(tag);
    }

    public static ResourceLocation getAmmoId(GunData data) {
        var tag = data.gun.getOrCreateTag();
        var ammoItems = GunModifierHelper.getAmmoItems(data);
        var currentAmmo = getAmmoId(tag);

        if (currentAmmo == null || ammoItems.contains(currentAmmo)) {
            return getFirst(ammoItems);
        }
        else return currentAmmo;
    }

    private static @Nullable ResourceLocation getAmmoId(CompoundTag tag) {
        if(tag.contains(AMMO_TAG, Tag.TAG_STRING))
            return ResourceLocation.tryParse(tag.getString(AMMO_TAG));
        else return null;
    }

    public static boolean isCurrentAmmo(GunData gunData, Item item) {
        return getAmmoId(gunData).equals(ITEMS.getKey(item));
    }

    public static Item getAmmoItem(GunData data) {
        return ITEMS.getValue(getAmmoId(data));
    }

    public static AmmoType getAmmoType(GunData data) {
        var ammo = getAmmoConfig(data);
        return ammo.getType();
    }

    public static Ammo getAmmoConfig(GunData data) {
        var gun = GunModifierHelper.getGun(data.gun);
        var ammoId = getAmmoId(data);

        if(gun.hasAmmo(ammoId)) {
            return gun.getAmmo(ammoId);
        }
        else if(getAmmoItem(data) instanceof IAmmo ammo) {
            return ammo.getConfig();
        }
        else return GunModifierHelper.AMMO;
    }

    //FIRE MODE______________________________________
    public static void switchFireMode(GunData data){
        var fireModes = GunModifierHelper.getFireModes(data);
        var current = getFireMode(data);
        var newFireMode = cycleSet(fireModes, current);
        setFireMode(data, newFireMode);
    }

    public static void setFireMode(GunData data, FireMode fireMode) {
        var tag = data.gun.getOrCreateTag();
        tag.putString(FIRE_MODE, fireMode.toString());
        data.gun.setTag(tag);
    }

    public static FireMode getFireMode(GunData data) {
        var fireModes = GunModifierHelper.getFireModes(data);
        var currentFireMode = getFireMode(data.gun);

        if (currentFireMode == null || !fireModes.contains(currentFireMode)) {
            setFireMode(data,getFirst(fireModes));
            return getFirst(fireModes);
        }
        else return currentFireMode;
    }

    @Nullable
    private static FireMode getFireMode(ItemStack gun) {
        var tag = gun.getOrCreateTag();
        if(tag.contains(FIRE_MODE, Tag.TAG_STRING))
            return FireMode.getType(tag.getString(FIRE_MODE));
        else return null;
    }

    private static <T> T cycleSet(Set<T> set, T value) {
        var buff = new ArrayList<>(set.stream().toList());
        var i = buff.indexOf(value);

        if(i == set.size() - 1)
            i = 0;
        else i++;

        return buff.get(i);
    }

    public static <T> T getFirst(Set<T> set) {
        return set.iterator().next();
    }
}
