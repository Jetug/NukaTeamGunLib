package com.nukateam.ntgl.common.data.holders;

import com.nukateam.ntgl.Ntgl;
import net.minecraft.resources.ResourceLocation;

import java.util.HashMap;
import java.util.Map;

public class WeaponMode extends ResourceHolder {
    public static final WeaponMode PRIMARY     = new WeaponMode(ResourceLocation.tryBuild(Ntgl.MOD_ID, "primary"));
    public static final WeaponMode SECONDARY   = new WeaponMode(ResourceLocation.tryBuild(Ntgl.MOD_ID, "secondary"));
    public static final WeaponMode ADDITIONAL  = new WeaponMode(ResourceLocation.tryBuild(Ntgl.MOD_ID, "additional"));
    public static final WeaponMode ALTERNATIVE = new WeaponMode(ResourceLocation.tryBuild(Ntgl.MOD_ID, "alternative"));

    private static final Map<ResourceLocation, WeaponMode> loadingTypeMap = new HashMap<>();

    static {
        registerType(PRIMARY    );
        registerType(SECONDARY  );
        registerType(ADDITIONAL);
        registerType(ALTERNATIVE);
    }

    public WeaponMode(ResourceLocation id) {
        super(id);
    }

    public static void registerType(WeaponMode mode) {
        loadingTypeMap.putIfAbsent(mode.getId(), mode);
    }

    public static WeaponMode getType(ResourceLocation id) {
        return loadingTypeMap.getOrDefault(id, PRIMARY);
    }

    public static WeaponMode getType(String id) {
        return getType(ResourceLocation.tryParse(id));
    }
}
