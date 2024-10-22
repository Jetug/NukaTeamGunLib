package com.nukateam.ntgl.common.base.holders;

import com.nukateam.ntgl.Ntgl;
import net.minecraft.resources.ResourceLocation;

import java.util.HashMap;
import java.util.Map;

public class FireMode extends ResourceHolder {
    /** A fire mode that shoots once per trigger press*/
    public static final FireMode SEMI_AUTO = new FireMode(new ResourceLocation(Ntgl.MOD_ID, "semi"));

    /** A fire mode that shoots as long as the trigger is held down*/
    public static final FireMode AUTO = new FireMode(new ResourceLocation(Ntgl.MOD_ID, "auto"));

    /** A fire mode that shoots in bursts*/
    public static final FireMode BURST = new FireMode(new ResourceLocation(Ntgl.MOD_ID, "burst"));

    private static final Map<ResourceLocation, FireMode> fireModeMap = new HashMap<>();

    static {
        /* Registers the standard fire modes when the class is loaded */
        registerType(SEMI_AUTO);
        registerType(AUTO);
        registerType(BURST);
    }

    public FireMode(ResourceLocation id) {
        super(id);
    }

    public ResourceLocation getIcon() {
        return new ResourceLocation(id.getNamespace(), "textures/hud/fire_mode/" + id.getPath() + ".png");
    }

    public static void registerType(FireMode mode) {
        fireModeMap.putIfAbsent(mode.getId(), mode);
    }

    public static FireMode getType(ResourceLocation id) {
        return fireModeMap.getOrDefault(id, SEMI_AUTO);
    }

    public static FireMode getType(String id) {
        return getType(ResourceLocation.tryParse(id));
    }
}
