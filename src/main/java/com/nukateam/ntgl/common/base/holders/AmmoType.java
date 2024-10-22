package com.nukateam.ntgl.common.base.holders;

import net.minecraft.resources.ResourceLocation;

import java.util.HashMap;
import java.util.Map;

public class AmmoType extends ResourceHolder {
    public static AmmoType STANDARD             = new AmmoType("standard"             );
    public static AmmoType PIERCING             = new AmmoType("piercing"             );
    public static AmmoType INCENDIARY           = new AmmoType("incendiary"           );
    public static AmmoType LASER                = new AmmoType("laser"                );
    public static AmmoType TESLA                = new AmmoType("tesla"                );
    public static AmmoType ENERGETIC            = new AmmoType("energetic"            );
    public static AmmoType EXPLOSIVE            = new AmmoType("explosive"            );
    public static AmmoType EXPLOSIVE_INCENDIARY = new AmmoType("explosive_incendiary" );
    public static AmmoType FIRE                 = new AmmoType("fire"                 );
    public static AmmoType SLUG                 = new AmmoType("slug"                 );
    public static AmmoType BUCKSHOT             = new AmmoType("buckshot"             );

    private static final Map<ResourceLocation, AmmoType> typeMap = new HashMap<>();
    
    static {
        registerType(STANDARD   );
        registerType(PIERCING   );
        registerType(INCENDIARY );
        registerType(LASER      );
        registerType(TESLA      );
        registerType(ENERGETIC  );
        registerType(EXPLOSIVE  );
        registerType(EXPLOSIVE_INCENDIARY  );
        registerType(FIRE       );
    }

    public AmmoType(ResourceLocation id) {
        super(id);
    }

    public AmmoType(String name) {
        super(name);
    }
    public ResourceLocation getIcon() {
        return new ResourceLocation(id.getNamespace(), "textures/hud/ammo_type/" + id.getPath() + ".png");
    }

    public static void registerType(AmmoType mode) {
        typeMap.putIfAbsent(mode.getId(), mode);
    }

    public static AmmoType getType(ResourceLocation id) {
        return typeMap.getOrDefault(id, STANDARD);
    }

    public static AmmoType getType(String path) {
        var id = ResourceLocation.tryParse(path);
        return getType(id);
    }
}
