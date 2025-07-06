package com.nukateam.ntgl.common.base.holders;

import com.nukateam.ntgl.Ntgl;
import net.minecraft.resources.ResourceLocation;

import java.util.HashMap;
import java.util.Map;

public class MeleeAttack extends ResourceHolder {
    public static final MeleeAttack SINGLE = new MeleeAttack(ResourceLocation.tryBuild(Ntgl.MOD_ID, "gun"));
    public static final MeleeAttack AUTO = new MeleeAttack(ResourceLocation.tryBuild(Ntgl.MOD_ID, "melee"));

    private static final Map<ResourceLocation, MeleeAttack> loadingTypeMap = new HashMap<>();

    static {
        registerType(SINGLE);
        registerType(AUTO);
    }

    public MeleeAttack(ResourceLocation id) {
        super(id);
    }

    public static void registerType(MeleeAttack mode) {
        loadingTypeMap.putIfAbsent(mode.getId(), mode);
    }

    public static MeleeAttack getType(ResourceLocation id) {
        return loadingTypeMap.getOrDefault(id, SINGLE);
    }

    public static MeleeAttack getType(String id) {
        return getType(ResourceLocation.tryParse(id));
    }
}
