package com.nukateam.ntgl.common.util.util;

import com.nukateam.ntgl.common.base.holders.AmmoType;
import com.nukateam.ntgl.common.base.holders.FireMode;
import com.nukateam.ntgl.common.data.config.Ammo;
import com.nukateam.ntgl.common.data.constants.Tags;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Set;

import static net.minecraftforge.registries.ForgeRegistries.ITEMS;

public class GunStateHelper {
    public static final String AMMO_TAG = "Projectile";
    public static final String FIRE_MODE = "FireMode";

    public static int getAmmoCount(ItemStack gunStack) {
        var tag = gunStack.getOrCreateTag();
        return tag.getInt(Tags.AMMO_COUNT);
    }

    //PROJECTILE
    public static void switchAmmo(GunData data){
        var ammoItems = GunModifierHelper.getAmmoItems(data);
        var current = getAmmoId(data);
        var newAmmo = cycleSet(ammoItems, current);

        setCurrentAmmo(data, newAmmo);
    }

    public static ResourceKey<DamageType> getDamageType(GunData data){
        var ammo = getAmmoConfig(data);
        return ammo.getDamageType();
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

        if (currentAmmo == null) {
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

    public static @NotNull Ammo getAmmoConfig(GunData data) {
        var ammoId = getAmmoId(data);
        return GunModifierHelper.getAmmoConfig(ammoId, data);
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
