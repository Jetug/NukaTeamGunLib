package com.nukateam.ntgl.common.base.gun;

import com.nukateam.ntgl.Ntgl;
import net.minecraft.resources.ResourceLocation;

import java.util.HashMap;
import java.util.Map;

public class LoadingType extends ResourceHolder {
    public static final LoadingType MAGAZINE = new LoadingType(new ResourceLocation(Ntgl.MOD_ID, "magazine"));
    public static final LoadingType PER_CARTRIDGE = new LoadingType(new ResourceLocation(Ntgl.MOD_ID, "per_cartridge"));

    private static final Map<ResourceLocation, LoadingType> loadingTypeMap = new HashMap<>();

    static {
        registerType(MAGAZINE);
        registerType(PER_CARTRIDGE);
    }

    public LoadingType(ResourceLocation id) {
        super(id);
    }

    public static void registerType(LoadingType mode) {
        loadingTypeMap.putIfAbsent(mode.getId(), mode);
    }

    public static LoadingType getType(ResourceLocation id) {
        return loadingTypeMap.getOrDefault(id, MAGAZINE);
    }

    public static LoadingType getType(String id) {
        return getType(ResourceLocation.tryParse(id));
    }
}
