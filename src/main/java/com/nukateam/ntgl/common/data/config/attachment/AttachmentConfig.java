package com.nukateam.ntgl.common.data.config.attachment;

import com.google.gson.JsonObject;
import com.nukateam.ntgl.common.data.holders.AttachmentType;
import com.nukateam.ntgl.common.debug.IDebugWidget;
import com.nukateam.ntgl.common.debug.IEditorMenu;
import com.nukateam.ntgl.common.util.annotation.Ignored;
import com.nukateam.ntgl.common.util.annotation.Optional;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.neoforge.common.util.INBTSerializable;
import net.neoforged.neoforge.fml.DistExecutor;
import org.apache.commons.lang3.tuple.Pair;

import java.util.List;
import java.util.function.Supplier;

public class AttachmentConfig implements INBTSerializable<CompoundTag>, IEditorMenu {
    public static final String ATTACHMENT_TYPE = "AttachmentType";
    public static final String MODIFIERS = "modifiers";

    @Ignored private AttachmentType type;
    @Optional private Modifiers modifiers = new Modifiers();

    @Override
    public CompoundTag serializeNBT(HolderLookup.Provider provider) {
        var tag = new CompoundTag();
        tag.putString(ATTACHMENT_TYPE, this.type.toString());
        tag.put(MODIFIERS, this.modifiers.serializeNBT());
        return tag;
    }

    @Override
    public void deserializeNBT(HolderLookup.Provider provider, CompoundTag tag) {
        if (tag.contains(ATTACHMENT_TYPE, Tag.TAG_STRING)) {
            this.type = AttachmentType.getType(ResourceLocation.tryParse(tag.getString(ATTACHMENT_TYPE)));
        }
        if (tag.contains(MODIFIERS)) {
            this.modifiers.deserializeNBT(tag.getCompound(MODIFIERS));
        }
    }

    public static AttachmentConfig create(ResourceLocation id, CompoundTag tag) {
        var attachment = new AttachmentConfig();
        attachment.deserializeNBT(tag);
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

    @Override
    public Component getEditorLabel() {
        return Component.literal("Attachment");
    }

    @Override
    public void getEditorWidgets(List<Pair<Component, Supplier<IDebugWidget>>> widgets) {
        DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> {
//            ItemStack heldItem = Objects.requireNonNull(Minecraft.getInstance().player).getMainHandItem();
//            ItemStack scope = Projectile.getScopeStack(heldItem);
//            if (scope.getItem() instanceof ScopeItem scopeItem) {
//                widgets.add(Pair.of(scope.getItem().getName(scope), () -> new DebugButton(Component.literal("Edit"), btn -> {
//                    Minecraft.getInstance().setScreen(createEditorScreen(Debug.getScope(scopeItem)));
//                })));
//            }

//            widgets.add(Pair.of(this.modules.getEditorLabel(), () -> new DebugButton(Component.literal(">"), btn -> {
//                Minecraft.getInstance().setScreen(createEditorScreen(this.modules));
//            })));
        });
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
