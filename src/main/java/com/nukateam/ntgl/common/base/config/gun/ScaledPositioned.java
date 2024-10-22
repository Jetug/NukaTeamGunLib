package com.nukateam.ntgl.common.base.config.gun;

import com.google.gson.JsonObject;
import com.nukateam.ntgl.common.data.annotation.Optional;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;

public class ScaledPositioned extends Positioned {
    @Optional
    protected double scale = 1.0;

    public ScaledPositioned() {
    }

    public ScaledPositioned(CompoundTag tag) {
        this.deserializeNBT(tag);
    }

    @Override
    public CompoundTag serializeNBT() {
        CompoundTag tag = super.serializeNBT();
        tag.putDouble("Scale", this.scale);
        return tag;
    }

    @Override
    public void deserializeNBT(CompoundTag tag) {
        super.deserializeNBT(tag);
        if (tag.contains("Scale", Tag.TAG_ANY_NUMERIC)) {
            this.scale = tag.getDouble("Scale");
        }
    }

    @Override
    public JsonObject toJsonObject() {
        JsonObject object = super.toJsonObject();
        if (this.scale != 1.0) {
            object.addProperty("scale", this.scale);
        }
        return object;
    }

    public double getScale() {
        return this.scale;
    }

    @Override
    public ScaledPositioned copy() {
        ScaledPositioned positioned = new ScaledPositioned();
        positioned.xOffset = this.xOffset;
        positioned.yOffset = this.yOffset;
        positioned.zOffset = this.zOffset;
        positioned.scale = this.scale;
        return positioned;
    }
}
