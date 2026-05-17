package com.nukateam.chassis_core.common.config;

import com.nukateam.chassis_core.common.data.holders.ChassisPart;
import com.nukateam.ntgl.common.util.annotation.Ignored;
import com.nukateam.ntgl.common.util.annotation.Optional;
import com.nukateam.ntgl.common.util.util.NbtUtils;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.common.util.INBTSerializable;
import org.jetbrains.annotations.Nullable;


import java.util.*;

public class EquipmentConfig implements INBTSerializable<CompoundTag>{
    @Ignored
    private ChassisPart part;
    @Optional LinkedHashSet<ResourceLocation> chassis;
    @Optional public ResourceLocation parent;
    @Ignored  public ResourceLocation model;
    @Ignored  public ResourceLocation id;
    @Ignored  public HashMap<String, ResourceLocation> texture;
    @Ignored  public ArrayList<EquipmentAttachment> attachments = new ArrayList<>();
    @Optional public ArrayList<String> hide = new ArrayList<>();
    @Optional public ArrayList<String> mods = new ArrayList<>();


    @Override
    public CompoundTag serializeNBT(HolderLookup.Provider provider) {
        var tag = new CompoundTag();
        tag.putString("part", this.part.toString());
        tag.put("chassis", NbtUtils.serializeSet(this.chassis));
        tag.putString("model", model.toString());
        tag.putString("id", id.toString());
        tag.put("texture", NbtUtils.serializeStringMap(this.texture));
        tag.put("attachments", NbtUtils.serializeArray(this.attachments, provider));
        tag.put("hide", NbtUtils.serializeStringArray(this.hide));
        tag.put("mods", NbtUtils.serializeStringArray(this.mods));

        return tag;
    }

    @Override
    public void deserializeNBT(HolderLookup.Provider provider, CompoundTag tag) {
        if (tag.contains("part", Tag.TAG_STRING)) {
            this.part = ChassisPart.getType(tag.getString("part"));
        }
        if (tag.contains("chassis", Tag.TAG_COMPOUND)) {
            this.chassis = NbtUtils.deserializeResourceLocationSet(tag.getCompound("chassis"));
        }
        if (tag.contains("model", Tag.TAG_STRING)) {
            this.model = ResourceLocation.tryParse(tag.getString("model"));
        }
        if (tag.contains("id", Tag.TAG_STRING)) {
            this.id = ResourceLocation.tryParse(tag.getString("id"));
        }
        if (tag.contains("texture", Tag.TAG_COMPOUND)) {
            this.texture = NbtUtils.deserializeRLMap(tag.getCompound("texture"));
        }
        if (tag.contains("attachments", Tag.TAG_COMPOUND)) {
            this.attachments = NbtUtils.deserializeArray(tag.getCompound("attachments"), EquipmentAttachment::create);
        }
        if (tag.contains("hide", Tag.TAG_COMPOUND)) {
            this.hide = NbtUtils.deserializeStringArrayList(tag.getCompound("hide"));
        }
        if (tag.contains("mods", Tag.TAG_COMPOUND)) {
            this.mods = NbtUtils.deserializeStringArrayList(tag.getCompound("mods"));
        }
    }

    public EquipmentConfig copy() {
        var config = new EquipmentConfig();
        config.part = this.part;
        config.chassis = this.chassis;
        config.model = this.model;
        config.id = this.id;
        return config;
    }

    public static EquipmentConfig create(CompoundTag tag) {
        var config = new EquipmentConfig();
        config.deserializeNBT(null, tag);
        return config;
    }

    public ChassisPart getPart() {
        return part;
    }

    public Set<ResourceLocation> getChassis() {
        return chassis;
    }

    public Collection<String> getAllVariants() {
        return texture.keySet();
    }

    public ResourceLocation getModel() {
        return model;
    }

    public ResourceLocation getId() {
        return id;
    }

    public ResourceLocation getTexture(String tag) {
        return texture.get(tag);
    }

    public ResourceLocation getParent() {
        return parent;
    }

    public HashMap<String, ResourceLocation> getTextures() {
        return texture;
    }

//    public int[] getUv() {
//        return uv;
//    }

    public ArrayList<EquipmentAttachment> getAttachments() {
        return attachments;
    }

    public ArrayList<String> getHide() {
        return hide;
    }

    public ArrayList<String> getMods() {
        return mods;
    }

    @Nullable
    public Collection<String> getArmorBone(String chassisBone) {
        var result = new ArrayList<String>();
        for (var attachment : attachments) {
            if (Objects.equals(attachment.frame, chassisBone))
                result.add(attachment.armor);
        }
        return result;
    }

    public static class Builder {
        private final EquipmentConfig config;

        private Builder() {
            this.config = new EquipmentConfig();
        }

        private Builder(EquipmentConfig projectile) {
            this.config = projectile.copy();
        }

        public static Builder create() {
            return new Builder();
        }

        public static Builder create(EquipmentConfig projectile) {
            return new Builder(projectile);
        }

        public EquipmentConfig build() {
            return this.config.copy();
        }

        public Builder setPart(ChassisPart part) {
            this.config.part = part;
            return this;
        }
    }
}
