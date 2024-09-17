package com.nukateam.ntgl.common.base.gun;

import com.nukateam.ntgl.Ntgl;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;

import java.util.HashMap;
import java.util.Map;

/**
 * Author: MigaMi
 */
public class FireMode extends BaseType {
    /** A fire mode that shoots once per trigger press*/
    public static final FireMode SEMI_AUTO = new FireMode(new ResourceLocation(Ntgl.MOD_ID, "semi"));

    /** A fire mode that shoots as long as the trigger is held down*/
    public static final FireMode AUTO = new FireMode(new ResourceLocation(Ntgl.MOD_ID, "auto"));

    /** A fire mode that shoots in bursts*/
    public static final FireMode BURST = new FireMode(new ResourceLocation(Ntgl.MOD_ID, "burst"));

    /** The fire mode map.*/
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
        return new ResourceLocation(id.getNamespace(), "textures/hud/" + id.getPath());
    }

    /**
     * Registers a new fire mode. If the id already exists, the fire mode will simply be ignored.
     *
     * @param mode the get of the fire mode
     */
    public static void registerType(FireMode mode) {
        fireModeMap.putIfAbsent(mode.getId(), mode);
    }

    /**
     * Gets the fire mode associated the the id. If the fire mode does not exist, it will default to
     * one handed.
     *
     * @param id the id of the fire mode
     * @return returns an get of the fire mode or SEMI_AUTO if it doesn't exist
     */
    public static FireMode getType(ResourceLocation id) {
        return fireModeMap.getOrDefault(id, SEMI_AUTO);
    }

    /**
     * Gets the fire mode associated the the id. If the fire mode does not exist, it will default to
     * one handed.
     *
     * @param id the id of the fire mode
     * @return returns an get of the fire mode or SEMI_AUTO if it doesn't exist
     */
    public static FireMode getType(String id) {
        return getType(ResourceLocation.tryParse(id));
    }
}
