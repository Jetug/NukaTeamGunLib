package com.nukateam.chassis_core.common.config;

import com.nukateam.chassis_core.common.data.holders.ChassisPart;
import com.nukateam.chassis_core.modules.config.utils.NbtUtils;
import com.nukateam.ntgl.common.util.annotation.Ignored;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.neoforged.neoforge.common.util.INBTSerializable;
import org.jetbrains.annotations.UnknownNullability;

import java.util.LinkedHashSet;

public class ChassisConfig implements INBTSerializable<CompoundTag>{
    @Ignored
    private LinkedHashSet<ChassisPart> parts;

    @Override
    public CompoundTag serializeNBT(HolderLookup.Provider provider) {
        var tag = new CompoundTag();
        tag.put("parts", NbtUtils.serializeSet(this.parts));
        return tag;
    }

    @Override
    public void deserializeNBT(HolderLookup.Provider provider, CompoundTag tag) {
        if (tag.contains("parts", Tag.TAG_COMPOUND)) {
            this.parts = NbtUtils.deserializeSet(tag.getCompound("parts"), ChassisPart::getType);
        }
    }

    public ChassisConfig copy() {
        var config = new ChassisConfig();
        config.parts = this.parts;
        return config;
    }

    public static ChassisConfig create(CompoundTag tag) {
        var config = new ChassisConfig();
        config.deserializeNBT(null, tag);
        return config;
    }

    public LinkedHashSet<ChassisPart> getParts() {
        return parts;
    }

    public static class Builder {
        private final ChassisConfig config;

        private Builder() {
            this.config = new ChassisConfig();
        }

        private Builder(ChassisConfig projectile) {
            this.config = projectile.copy();
        }

        public static Builder create() {
            return new Builder();
        }

        public static Builder create(ChassisConfig projectile) {
            return new Builder(projectile);
        }

        public ChassisConfig build() {
            return this.config.copy();
        }

        public Builder setParts(LinkedHashSet<ChassisPart> parts) {
            this.config.parts = parts;
            return this;
        }
    }
}
