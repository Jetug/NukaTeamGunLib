package com.nukateam.ntgl.common.base.holders;

import com.nukateam.ntgl.Ntgl;
import net.minecraft.resources.ResourceLocation;

import java.util.HashMap;
import java.util.Map;

public class MeleeMode extends ResourceHolder {
    public static final MeleeMode SINGLE = new MeleeMode(ResourceLocation.tryBuild(Ntgl.MOD_ID, "gun"));
    public static final MeleeMode AUTO = new MeleeMode(ResourceLocation.tryBuild(Ntgl.MOD_ID, "melee"));

    private static final Map<ResourceLocation, MeleeMode> loadingTypeMap = new HashMap<>();

    static {
        registerType(SINGLE);
        registerType(AUTO);
    }

    public MeleeMode(ResourceLocation id) {
        super(id);
    }

    public static void registerType(MeleeMode mode) {
        loadingTypeMap.putIfAbsent(mode.getId(), mode);
    }

    public static MeleeMode getType(ResourceLocation id) {
        return loadingTypeMap.getOrDefault(id, SINGLE);
    }

    public static MeleeMode getType(String id) {
        return getType(ResourceLocation.tryParse(id));
    }
}
