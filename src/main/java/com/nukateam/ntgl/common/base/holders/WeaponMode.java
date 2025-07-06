package com.nukateam.ntgl.common.base.holders;

import com.nukateam.ntgl.Ntgl;
import net.minecraft.resources.ResourceLocation;

import java.util.HashMap;
import java.util.Map;

public class WeaponMode extends ResourceHolder {
    public static final WeaponMode GUN = new WeaponMode(ResourceLocation.tryBuild(Ntgl.MOD_ID, "gun"));
    public static final WeaponMode MELEE = new WeaponMode(ResourceLocation.tryBuild(Ntgl.MOD_ID, "melee"));

    private static final Map<ResourceLocation, WeaponMode> loadingTypeMap = new HashMap<>();

    static {
        registerType(GUN);
        registerType(MELEE);
    }

    public WeaponMode(ResourceLocation id) {
        super(id);
    }

    public static void registerType(WeaponMode mode) {
        loadingTypeMap.putIfAbsent(mode.getId(), mode);
    }

    public static WeaponMode getType(ResourceLocation id) {
        return loadingTypeMap.getOrDefault(id, GUN);
    }

    public static WeaponMode getType(String id) {
        return getType(ResourceLocation.tryParse(id));
    }
}
