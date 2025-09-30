package com.nukateam.ntgl.common.data.holders;

import com.nukateam.ntgl.Ntgl;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

import java.util.HashMap;
import java.util.Map;

public class FireMode extends ResourceHolder {
    /** A fire mode that shoots once per trigger press*/
    public static final FireMode SEMI_AUTO = new FireMode(ResourceLocation.tryBuild(Ntgl.MOD_ID, "semi"));

    /** A fire mode that shoots as long as the trigger is held down*/
    public static final FireMode AUTO = new FireMode(ResourceLocation.tryBuild(Ntgl.MOD_ID, "auto"));

    /** A fire mode that shoots in bursts*/
    public static final FireMode BURST = new FireMode(ResourceLocation.tryBuild(Ntgl.MOD_ID, "burst"));

    public static final FireMode MULTI = new FireMode(ResourceLocation.tryBuild(Ntgl.MOD_ID, "multi"));

    private static final Map<ResourceLocation, FireMode> fireModeMap = new HashMap<>();

    static {
        registerType(SEMI_AUTO);
        registerType(AUTO);
        registerType(BURST);
        registerType(MULTI);
    }

    public Component getDisplayName(){
        return Component.translatable("fire_mode." + getId().getNamespace() + "." + getId().getPath());
    }

    public FireMode(ResourceLocation id) {
        super(id);
    }

    public ResourceLocation getIcon() {
        return ResourceLocation.tryBuild(id.getNamespace(), "textures/hud/fire_mode/" + id.getPath() + ".png");
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
