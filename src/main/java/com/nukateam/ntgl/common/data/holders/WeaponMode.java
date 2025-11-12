package com.nukateam.ntgl.common.data.holders;

import com.nukateam.ntgl.Ntgl;
import com.nukateam.ntgl.client.input.NtglKeyBinds;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

public class WeaponMode extends ResourceHolder {
    private static final Map<ResourceLocation, WeaponMode> loadingTypeMap = new HashMap<>();

    public static final WeaponMode PRIMARY     = new WeaponMode("primary"    );
    public static final WeaponMode SECONDARY   = new WeaponMode("secondary"  );
    public static final WeaponMode ADDITIONAL  = new WeaponMode("additional" );
    public static final WeaponMode ALTERNATIVE = new WeaponMode("alternative");

    static {
        registerType(PRIMARY    );
        registerType(SECONDARY  );
        registerType(ADDITIONAL );
        registerType(ALTERNATIVE);
    }

    private WeaponMode(String id) {
        super(ResourceLocation.tryBuild(Ntgl.MOD_ID, id));
    }

    public WeaponMode(ResourceLocation id, Supplier<KeyMapping> key) {
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
