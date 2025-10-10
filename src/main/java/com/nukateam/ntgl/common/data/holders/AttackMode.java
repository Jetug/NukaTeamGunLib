package com.nukateam.ntgl.common.data.holders;

import com.nukateam.ntgl.Ntgl;
import net.minecraft.resources.ResourceLocation;

import java.util.HashMap;
import java.util.Map;

public class AttackMode extends ResourceHolder {
    public static final AttackMode PRIMARY      = new AttackMode(ResourceLocation.tryBuild(Ntgl.MOD_ID, "none"));
    public static final AttackMode SECONDARY    = new AttackMode(ResourceLocation.tryBuild(Ntgl.MOD_ID, "gun"));
    public static final AttackMode ATTACK       = new AttackMode(ResourceLocation.tryBuild(Ntgl.MOD_ID, "melee"));
    public static final AttackMode ALTERNATIVE  = new AttackMode(ResourceLocation.tryBuild(Ntgl.MOD_ID, "throwable"));

    private static final Map<ResourceLocation, AttackMode> loadingTypeMap = new HashMap<>();

    static {
        registerType(PRIMARY    );
        registerType(SECONDARY  );
        registerType(ATTACK     );
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
