package com.nukateam.ntgl.common.data.config.weapon;

import com.google.gson.JsonObject;
import com.nukateam.ntgl.common.data.holders.AttachmentType;
import com.nukateam.ntgl.common.util.util.NbtUtils;
import com.nukateam.ntgl.common.util.annotation.Optional;
import com.nukateam.ntgl.common.debug.IDebugWidget;
import com.nukateam.ntgl.common.debug.IEditorMenu;
import com.nukateam.ntgl.common.debug.screen.widget.DebugButton;
import com.nukateam.ntgl.common.debug.screen.widget.DebugToggle;
import net.minecraft.client.Minecraft;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.neoforge.common.util.INBTSerializable;
import net.minecraft.core.HolderLookup;
import org.apache.commons.lang3.tuple.Pair;

import javax.annotation.Nullable;
import java.util.*;

public class Modules implements INBTSerializable<CompoundTag> {
    @Optional
    boolean attachmentScreen = true;
    @Optional
    private LinkedHashMap<AttachmentType, ArrayList<Attachment>> attachments = new LinkedHashMap<>();

    public boolean attachmentScreen() {
        return this.attachmentScreen;
    }

    public LinkedHashMap<AttachmentType, ArrayList<Attachment>> getAttachments() {
        return this.attachments;
    }

    @Override
    public CompoundTag serializeNBT(HolderLookup.Provider provider) {
        var tag = new CompoundTag();
        tag.putBoolean("AttachmentScreen", attachmentScreen);

        if (attachments != null && !attachments.isEmpty())
            tag.put("Attachments", NbtUtils.serializeArrayMap(attachments));

        return tag;
    }

    @Override
    public void deserializeNBT(HolderLookup.Provider provider, CompoundTag tag) {
        if (tag.contains("AttachmentScreen", Tag.TAG_BYTE)) {
            this.attachmentScreen = tag.getBoolean("AttachmentScreen");
        }
        if (tag.contains("Attachments", Tag.TAG_COMPOUND)) {
            var nbt = tag.getCompound("Attachments");
            this.attachments = NbtUtils.deserializeAttachmentMap(nbt);
        }
    }

    public JsonObject toJsonObject() {
        return new JsonObject();
    }

    public Modules copy() {
        Modules modules = new Modules();
        modules.attachments = new LinkedHashMap<>(this.attachments);
        return modules;
    }

    public static class Attachment implements INBTSerializable<CompoundTag> {
        public static final String OFFSET = "Offset";

        @Optional @Nullable String name;
        @Optional @Nullable ResourceLocation item;
        @Optional ArrayList<String> hide = new ArrayList<>();
        @Optional ArrayList<String> bones = new ArrayList<>();
        @Optional Vec3 offset = Vec3.ZERO;

        @Override
        public CompoundTag serializeNBT(HolderLookup.Provider provider) {
            var tag = new CompoundTag();

            if (this.name != null) {
                tag.putString("Name", this.name);
            }
            if (this.item != null) {
                tag.putString("Item", this.item.toString());
            }
            if (this.hide != null) {
                tag.put("Hide", NbtUtils.serializeStringArray(this.hide));
            }
            if (this.bones != null) {
                tag.put("Bones", NbtUtils.serializeStringArray(this.bones));
            }
            tag.put(OFFSET, NbtUtils.writeVec3(offset));
            return tag;
        }

        @Override
        public void deserializeNBT(HolderLookup.Provider provider, CompoundTag tag) {
            if (tag.contains("Name", Tag.TAG_STRING)) {
                this.name = tag.getString("Name");
            }
            if (tag.contains("Item", Tag.TAG_STRING)) {
                this.item = ResourceLocation.tryParse(tag.getString("Item"));
            }
            if (tag.contains("Hide", Tag.TAG_COMPOUND)) {
                this.hide = NbtUtils.deserializeStringArray(tag.getCompound("Hide"));
            }
            if (tag.contains("Bones", Tag.TAG_COMPOUND)) {
                this.bones = NbtUtils.deserializeStringArray(tag.getCompound("Bones"));
            }
            if (tag.contains(OFFSET, Tag.TAG_COMPOUND)) {
                this.offset = NbtUtils.readVec3(tag.getCompound(OFFSET));
            }
        }

        public JsonObject toJsonObject() {
            var object = new JsonObject();
            if (this.name != null) {
                object.addProperty("Name", this.name);
            }
            if (this.item != null) {
                object.addProperty("Item", this.item.toString());
            }
            return object;
        }

        public Attachment copy() {
            var attachments = new Attachment();
            if (this.name != null) {
                attachments.name = this.name;
            }
            if (this.item != null) {
                attachments.item = this.item;
            }
            if (this.hide != null) {
                attachments.hide = this.hide;
            }
            if (this.bones != null) {
                attachments.bones = this.bones;
            }
            attachments.offset = this.offset;

            return attachments;
        }

        @Nullable
        public String getName() {
            return this.name;
        }

        @Nullable
        public ResourceLocation getItemId() {
            return this.item;
        }

        public ArrayList<String> getHidden() {
            return this.hide;
        }

        public ArrayList<String> getBones() {
            return this.bones;
        }

        public Vec3 getOffset() {
            return this.offset;
        }
    }
}
