package com.nukateam.ntgl.common.base.gun;

import com.nukateam.ntgl.Ntgl;
import net.minecraft.resources.ResourceLocation;

import java.util.HashMap;
import java.util.Map;


public class AmmoType extends BaseType {
    public static AmmoType ROUND_10MM   = new AmmoType("10mm");
    public static AmmoType GRENADE   = new AmmoType("10mm");
    public static AmmoType CAL45        = new AmmoType("45cal");

    private static final Map<ResourceLocation, AmmoType> typeMap = new HashMap<>();
    
    static {
        registerType(ROUND_10MM);
        registerType(CAL45     );
    }

    public AmmoType(ResourceLocation id) {
        super(id);
    }

    public AmmoType(String name) {
        super(name);
    }

    public static void registerType(AmmoType mode) {
        typeMap.putIfAbsent(mode.getId(), mode);
    }

    public static AmmoType getType(ResourceLocation id) {
        return typeMap.getOrDefault(id, ROUND_10MM);
    }

    public static AmmoType getType(String path) {
        var id = ResourceLocation.tryParse(path);
        return getType(id);
    }
}
