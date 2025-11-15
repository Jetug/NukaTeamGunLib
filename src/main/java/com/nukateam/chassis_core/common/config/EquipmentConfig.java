package com.nukateam.chassis_core.common.config;

import com.nukateam.chassis_core.common.data.holders.ChassisPart;
import com.nukateam.chassis_core.common.data.json.EquipmentAttachment;
import com.nukateam.chassis_core.modules.config.annotation.Ignored;
import com.nukateam.chassis_core.modules.config.annotation.Optional;
import com.nukateam.chassis_core.modules.config.utils.NbtUtils;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.common.util.INBTSerializable;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class EquipmentConfig implements INBTSerializable<CompoundTag>{
    @Ignored private ChassisPart part;
    @Optional LinkedHashSet<ResourceLocation> chassis;
    @Optional public ResourceLocation parent;
    @Ignored public ResourceLocation model;
    @Ignored public HashMap<String, ResourceLocation> texture;
//    @Optional public int[] uv;
    @Optional public String[] hide = new String[0];
    @Ignored public EquipmentAttachment[] attachments = new EquipmentAttachment[0];
    @Optional public String[] mods = new String[0];


    @Override
    public CompoundTag serializeNBT(HolderLookup.Provider provider) {
        var tag = new CompoundTag();
        tag.putString("part", this.part.toString());
        tag.put("chassis", NbtUtils.serializeSet(this.chassis));
        tag.putString("model", model.toString());
        return tag;
    }

    @Override
    public void deserializeNBT(HolderLookup.Provider provider, CompoundTag tag) {
        if (tag.contains("part", Tag.TAG_STRING)) {
            this.part = ChassisPart.getType(tag.getString("part"));
        }
        if (tag.contains("chassis", Tag.TAG_COMPOUND)) {
            this.chassis = NbtUtils.deserializeRLSet(tag.getCompound("chassis"));
        }
        if (tag.contains("model", Tag.TAG_STRING)) {
            this.model = ResourceLocation.tryParse(tag.getString("model"));
        }
    }

    public EquipmentConfig copy() {
        var config = new EquipmentConfig();
        config.part = this.part;
        config.chassis = this.chassis;
        config.model = this.model;
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

    public String[] getHide() {
        return hide;
    }

    public EquipmentAttachment[] getAttachments() {
        return attachments;
    }

    public String[] getMods() {
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
