package com.nukateam.ntgl.common.data.holders;

import com.nukateam.ntgl.Ntgl;
import net.minecraft.resources.ResourceLocation;

import java.util.HashMap;
import java.util.Map;

public class AttackMode extends ResourceHolder {
    public static final AttackMode PRIMARY     = new AttackMode(ResourceLocation.tryBuild(Ntgl.MOD_ID, "primary"));
    public static final AttackMode SECONDARY   = new AttackMode(ResourceLocation.tryBuild(Ntgl.MOD_ID, "secondary"));
    public static final AttackMode ADDITIONAL  = new AttackMode(ResourceLocation.tryBuild(Ntgl.MOD_ID, "additional"));
    public static final AttackMode ALTERNATIVE = new AttackMode(ResourceLocation.tryBuild(Ntgl.MOD_ID, "alternative"));

    private static final Map<ResourceLocation, AttackMode> loadingTypeMap = new HashMap<>();

    static {
        registerType(PRIMARY    );
        registerType(SECONDARY  );
        registerType(ADDITIONAL);
        registerType(ALTERNATIVE);
    }

    public AttackMode(ResourceLocation id) {
        super(id);
    }

    public static void registerType(AttackMode mode) {
        loadingTypeMap.putIfAbsent(mode.getId(), mode);
    }

    public static AttackMode getType(ResourceLocation id) {
        return loadingTypeMap.getOrDefault(id, PRIMARY);
    }

    public static AttackMode getType(String id) {
        return getType(ResourceLocation.tryParse(id));
    }
}
