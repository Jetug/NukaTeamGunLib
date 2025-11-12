package com.nukateam.ntgl.common.data.holders;

import net.minecraft.resources.ResourceLocation;

import java.util.HashMap;
import java.util.Map;

public class ProjectileType extends ResourceHolder {
    public static ProjectileType BULLET = new ProjectileType("bullet");
    public static ProjectileType GRENADE = new ProjectileType("grenade");
    public static ProjectileType STUN_GRENADE = new ProjectileType("stun_grenade");
    public static ProjectileType MISSILE = new ProjectileType("missile");
    public static ProjectileType LASER   = new ProjectileType("laser"  );
    public static ProjectileType TESLA   = new ProjectileType("tesla"  );
    public static ProjectileType FIRE    = new ProjectileType("fire"  );
    public static ProjectileType CONTINUOUS_LASER = new ProjectileType("continuous_laser");
    public static ProjectileType ARROW_LIKE = new ProjectileType("arrow_like");

    private static final Map<ResourceLocation, ProjectileType> typeMap = new HashMap<>();

    static {
        registerType(BULLET);
        registerType(GRENADE);
        registerType(STUN_GRENADE);
        registerType(MISSILE);
        registerType(LASER);
        registerType(TESLA);
        registerType(FIRE );
        registerType(CONTINUOUS_LASER);
        registerType(ARROW_LIKE);
    }

    public ProjectileType(ResourceLocation id) {
        super(id);
    }

    public ProjectileType(String name) {
        super(name);
    }

    public static void registerType(ProjectileType mode) {
        typeMap.putIfAbsent(mode.getId(), mode);
    }

    public static ProjectileType getType(ResourceLocation id) {
        return typeMap.getOrDefault(id, BULLET);
    }

    public static ProjectileType getType(String path) {
        var id = ResourceLocation.tryParse(path);
        return getType(id);
    }
}
