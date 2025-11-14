package com.nukateam.ntgl.common.data.config.attachment;

import com.google.gson.JsonObject;
import com.nukateam.ntgl.common.data.holders.AttachmentType;
import com.nukateam.ntgl.common.debug.IDebugWidget;
import com.nukateam.ntgl.common.debug.IEditorMenu;
import com.nukateam.ntgl.common.util.annotation.Ignored;
import com.nukateam.ntgl.common.util.annotation.Optional;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.neoforge.common.util.INBTSerializable;
import org.apache.commons.lang3.tuple.Pair;

import java.util.List;
import java.util.function.Supplier;

public class AttachmentConfig implements INBTSerializable<CompoundTag> {
    public static final String ATTACHMENT_TYPE = "AttachmentType";
    public static final String MODIFIERS = "modifiers";

    @Ignored private AttachmentType type;
    @Optional private Modifiers modifiers = new Modifiers();

    @Override
    public CompoundTag serializeNBT(HolderLookup.Provider provider) {
        var tag = new CompoundTag();
        tag.putString(ATTACHMENT_TYPE, this.type.toString());
        tag.put(MODIFIERS, this.modifiers.serializeNBT(provider));
        return tag;
    }

    @Override
    public void deserializeNBT(HolderLookup.Provider provider, CompoundTag tag) {
        if (tag.contains(ATTACHMENT_TYPE, Tag.TAG_STRING)) {
            this.type = AttachmentType.getType(ResourceLocation.tryParse(tag.getString(ATTACHMENT_TYPE)));
        }
        if (tag.contains(MODIFIERS)) {
            this.modifiers.deserializeNBT(provider, tag.getCompound(MODIFIERS));
        }
    }

    public static AttachmentConfig create(ResourceLocation id, CompoundTag tag) {
        var attachment = new AttachmentConfig();
        attachment.deserializeNBT(null, tag);
        return attachment;
    }

    public AttachmentConfig copy() {
        var projectile = new AttachmentConfig();
        projectile.type = this.type;
        projectile.modifiers = this.modifiers.copy();
        return projectile;
    }

    public JsonObject toJsonObject() {
        var object = new JsonObject();
        object.addProperty("type", this.type.toString());
        object.add("modifiers", this.modifiers.toJsonObject());
        return object;
    }

    public AttachmentType getType() {
        return this.type;
    }

    public Modifiers getModifiers() {
        return modifiers;
    }

    public static class Builder {
        private final AttachmentConfig attachment;

        private Builder() {
            this.attachment = new AttachmentConfig();
        }

        private Builder(AttachmentConfig attachment) {
            this.attachment = attachment.copy();
        }

        public static AttachmentConfig.Builder create() {
            return new AttachmentConfig.Builder();
        }

        public static AttachmentConfig.Builder create(AttachmentConfig attachment) {
            return new AttachmentConfig.Builder(attachment);
        }

        public AttachmentConfig.Builder setType(AttachmentType type) {
            this.attachment.type = type;
            return this;
        }

        public AttachmentConfig build() {
            return this.attachment.copy(); //Copy since the builder could be used again
        }
    }
}
