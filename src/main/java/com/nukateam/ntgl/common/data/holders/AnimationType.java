package com.nukateam.ntgl.common.data.holders;

import com.google.gson.JsonParseException;
import com.nukateam.ntgl.Ntgl;
import net.minecraft.resources.ResourceLocation;

import java.util.HashMap;
import java.util.Map;

public class AnimationType extends ResourceHolder {
    public static final AnimationType FIRE = new AnimationType("fire");
    public static final AnimationType RELOAD = new AnimationType("reload");
    public static final AnimationType MELEE = new AnimationType("melee");

    private static final Map<ResourceLocation, AnimationType> typeMap = new HashMap<>();

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
        typeMap.putIfAbsent(mode.getId(), mode);
    }

    public static AnimationType getType(ResourceLocation id) {
//        return typeMap.getOrDefault(id, FIRE);

        var type = typeMap.get(id);
        if(type == null){
            throw new JsonParseException("Animation type \"" + id.toString() + "\" doesn't exists");
        }
        return type;
    }

    public static AnimationType getType(String id) {
        return getType(ResourceLocation.tryParse(id));
    }
}
