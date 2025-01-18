package com.nukateam.ntgl.common.base.holders;

import com.nukateam.ntgl.Ntgl;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
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
    public static AttachmentType MUZZLE       = new AttachmentType("muzzle");

    private static final Map<ResourceLocation, AttachmentType> typeMap = new HashMap<>();
    
    static {
        registerType(SCOPE       );
        registerType(BARREL      );
        registerType(STOCK       );
        registerType(GRIP        );
        registerType(UNDER_BARREL);
        registerType(MAGAZINE    );
        registerType(MUZZLE      );
    }

    private final ResourceLocation id;

    public AttachmentType(ResourceLocation id) {
        this.id = id;
    }

    private AttachmentType(String name) {
        this.id = new ResourceLocation(Ntgl.MOD_ID, name);
    }

    public static void registerType(AttachmentType mode) {
        typeMap.putIfAbsent(mode.getId(), mode);
    }

    public static AttachmentType getType(ResourceLocation id) {
        return typeMap.getOrDefault(id, SCOPE);
    }

    public static AttachmentType getType(String path) {
        var id = ResourceLocation.tryParse(path);
        return getType(id);
    }

    public ResourceLocation getId() {
        return this.id;
    }

    public ResourceLocation getIcon() {
        return new ResourceLocation(getId().getNamespace(), "textures/gui/icons/" + getId().getPath() + ".png");
    }

    public String getTranslationKey(){
        return "slot."+getId().getNamespace()+".attachment." + getId().getPath();
    }

    public MutableComponent getTranslationComponent(){
        return Component.translatable(this.getTranslationKey());
    }

    public boolean equals(ResourceLocation obj) {
        return this == getType(obj);
    }

    @Override
    public String toString() {
        return id.toString();
    }
}
