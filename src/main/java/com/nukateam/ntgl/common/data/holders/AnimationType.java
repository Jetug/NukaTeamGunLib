package com.nukateam.ntgl.common.data.holders;

import com.nukateam.ntgl.Ntgl;
import net.minecraft.resources.ResourceLocation;

import java.util.HashMap;
import java.util.Map;

public class AnimationType extends ResourceHolder {
    public static final AnimationType FIRE = new AnimationType("fire");
    public static final AnimationType RELOAD = new AnimationType("reload");
    public static final AnimationType MELEE = new AnimationType("melee");

    private static final Map<ResourceLocation, AnimationType> loadingTypeMap = new HashMap<>();

    public static void register(){
        registerType(FIRE);
        registerType(RELOAD);
        registerType(MELEE);
    }

    public AnimationType(ResourceLocation id) {
        super(id);
    }

    private AnimationType(String name) {
        super(ResourceLocation.tryBuild(Ntgl.MOD_ID, name));
    }

    public static void registerType(AnimationType mode) {
        loadingTypeMap.putIfAbsent(mode.getId(), mode);
    }

    public static AnimationType getType(ResourceLocation id) {
        return loadingTypeMap.getOrDefault(id, FIRE);
    }

    public static AnimationType getType(String id) {
        return getType(ResourceLocation.tryParse(id));
    }
}
