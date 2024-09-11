package com.nukateam.ntgl.common.base.gun;

import com.nukateam.ntgl.Ntgl;
import net.minecraft.resources.ResourceLocation;

import java.util.HashMap;
import java.util.Map;


public class AttachmentType {
    public static AttachmentType SCOPE        = new AttachmentType("scope");
    public static AttachmentType BARREL       = new AttachmentType("barrel");
    public static AttachmentType STOCK        = new AttachmentType("stock");
    public static AttachmentType GRIP         = new AttachmentType("grip");
    public static AttachmentType UNDER_BARREL = new AttachmentType("under_barrel");
    public static AttachmentType MAGAZINE     = new AttachmentType("magazine");

    private static final Map<ResourceLocation, AttachmentType> typeMap = new HashMap<>();

    static {
        registerType(SCOPE       );
        registerType(BARREL      );
        registerType(STOCK       );
        registerType(GRIP       );
        registerType(UNDER_BARREL);
        registerType(MAGAZINE    );
    }

    /**
     * Registers a new fire mode. If the id already exists, the fire mode will simply be ignored.
     *
     * @param mode the get of the fire mode
     */
    public static void registerType(AttachmentType mode) {
        typeMap.putIfAbsent(mode.getId(), mode);
    }

    public static AttachmentType getType(ResourceLocation id) {
        return typeMap.getOrDefault(id, SCOPE);
    }

    private final ResourceLocation id;

    public AttachmentType(ResourceLocation id) {
        this.id = id;
    }

    public AttachmentType(String name) {
        this.id = new ResourceLocation(Ntgl.MOD_ID, name);
    }

    public ResourceLocation getId() {
        return this.id;
    }

}
