package com.nukateam.ntgl.common.data.holders;

import net.minecraft.resources.ResourceLocation;

import java.util.HashMap;
import java.util.Map;

public class GrenadeMode extends ResourceHolder {
    public static GrenadeMode SAFE = new GrenadeMode("safe");
    public static GrenadeMode UNSAFE = new GrenadeMode("unsafe");

    private static final Map<ResourceLocation, GrenadeMode> typeMap = new HashMap<>();

    static {
        registerType(SAFE);
        registerType(UNSAFE);
    }

    public GrenadeMode(ResourceLocation id) {
        super(id);
    }

    public GrenadeMode(String name) {
        super(name);
    }

    public static void registerType(GrenadeMode mode) {
        typeMap.putIfAbsent(mode.getId(), mode);
    }

    public static GrenadeMode getType(ResourceLocation id) {
        return typeMap.getOrDefault(id, SAFE);
    }

    public static GrenadeMode getType(String path) {
        var id = ResourceLocation.tryParse(path);
        return getType(id);
    }
}
