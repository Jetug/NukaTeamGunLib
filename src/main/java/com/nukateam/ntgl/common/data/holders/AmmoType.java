package com.nukateam.ntgl.common.data.holders;

import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class AmmoType extends ResourceHolder {
    public static AmmoType STANDARD = new AmmoType("standard"             );

    private static final Map<ResourceLocation, AmmoType> typeMap = new HashMap<>();
    
    static {
        registerType(STANDARD);
    }

    public AmmoType(ResourceLocation id) {
        super(id);
    }

    public AmmoType(String name) {
        super(name);
    }

    public ResourceLocation getIcon() {
        return ResourceLocation.tryBuild(id.getNamespace(), "textures/hud/ammo_type/" + id.getPath() + ".png");
    }

    public static void registerType(AmmoType mode) {
        typeMap.putIfAbsent(mode.getId(), mode);
    }

    public static AmmoType getType(ResourceLocation id) {
        return typeMap.getOrDefault(id, createDefault(id));
    }

    public static AmmoType getType(String path) {
        var id = ResourceLocation.tryParse(path);
        return getType(id);
    }

    private static AmmoType createDefault(ResourceLocation id){
        var type = new AmmoType(id);
        registerType(type);
        return type;
    }
}
