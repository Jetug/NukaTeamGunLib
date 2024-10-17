package com.nukateam.ntgl.common.base.config.gun;

import com.google.gson.JsonObject;
import com.nukateam.ntgl.common.data.annotation.Optional;
import com.nukateam.ntgl.common.data.util.SuperBuilder;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraftforge.common.util.INBTSerializable;

public class Positioned implements INBTSerializable<CompoundTag> {
    @Optional
    protected double xOffset;
    @Optional
    protected double yOffset;
    @Optional
    protected double zOffset;

    @Override
    public CompoundTag serializeNBT() {
        CompoundTag tag = new CompoundTag();
        tag.putDouble("XOffset", this.xOffset);
        tag.putDouble("YOffset", this.yOffset);
        tag.putDouble("ZOffset", this.zOffset);
        return tag;
    }

    @Override
    public void deserializeNBT(CompoundTag tag) {
        if (tag.contains("XOffset", Tag.TAG_ANY_NUMERIC)) {
            this.xOffset = tag.getDouble("XOffset");
        }
        if (tag.contains("YOffset", Tag.TAG_ANY_NUMERIC)) {
            this.yOffset = tag.getDouble("YOffset");
        }
        if (tag.contains("ZOffset", Tag.TAG_ANY_NUMERIC)) {
            this.zOffset = tag.getDouble("ZOffset");
        }
    }

    public JsonObject toJsonObject() {
        JsonObject object = new JsonObject();
        if (this.xOffset != 0) {
            object.addProperty("xOffset", this.xOffset);
        }
        if (this.yOffset != 0) {
            object.addProperty("yOffset", this.yOffset);
        }
        if (this.zOffset != 0) {
            object.addProperty("zOffset", this.zOffset);
        }
        return object;
    }

    public double getXOffset() {
        return this.xOffset;
    }

    public double getYOffset() {
        return this.yOffset;
    }

    public double getZOffset() {
        return this.zOffset;
    }

    public Positioned copy() {
        Positioned positioned = new Positioned();
        positioned.xOffset = this.xOffset;
        positioned.yOffset = this.yOffset;
        positioned.zOffset = this.zOffset;
        return positioned;
    }

    public static class Builder extends AbstractBuilder<Builder> {
    }

    protected static abstract class AbstractBuilder<T extends AbstractBuilder<T>> extends SuperBuilder<Positioned, T> {
        private final Positioned positioned;

        private AbstractBuilder() {
            this(new Positioned());
        }

        protected AbstractBuilder(Positioned positioned) {
            this.positioned = positioned;
        }

        public T setOffset(double xOffset, double yOffset, double zOffset) {
            this.positioned.xOffset = xOffset;
            this.positioned.yOffset = yOffset;
            this.positioned.zOffset = zOffset;
            return this.self();
        }

        public T setXOffset(double xOffset) {
            this.positioned.xOffset = xOffset;
            return this.self();
        }

        public T setYOffset(double yOffset) {
            this.positioned.yOffset = yOffset;
            return this.self();
        }

        public T setZOffset(double zOffset) {
            this.positioned.zOffset = zOffset;
            return this.self();
        }

        @Override
        public Positioned build() {
            return this.positioned.copy();
        }
    }
}
