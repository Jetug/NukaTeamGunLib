package com.nukateam.ntgl.common.data.holders;

import net.minecraft.resources.ResourceLocation;

import java.util.HashMap;
import java.util.Map;

public class ProjectileVariant extends ResourceHolder {
    public static ProjectileVariant STANDARD = new ProjectileVariant("standard");

    private static final Map<ResourceLocation, ProjectileVariant> typeMap = new HashMap<>();

    static {
        registerType(STANDARD);
    }

    public ProjectileVariant(ResourceLocation id) {
        super(id);
    }

    public ProjectileVariant(String name) {
        super(name);
    }

    public ResourceLocation getIcon() {
        return ResourceLocation.tryBuild(id.getNamespace(), "textures/projectile/" + id.getPath() + ".png");
    }

    public static void registerType(ProjectileVariant mode) {
        typeMap.putIfAbsent(mode.getId(), mode);
    }

    public static ProjectileVariant getType(ResourceLocation id) {
        return typeMap.getOrDefault(id, createDefault(id));
    }

    public static ProjectileVariant getType(String path) {
        var id = ResourceLocation.tryParse(path);
        return getType(id);
    }

    private static ProjectileVariant createDefault(ResourceLocation id){
        var type = new ProjectileVariant(id);
        registerType(type);
        return type;
    }
}
