package com.nukateam.ntgl.common.data.config.weapon;

import com.google.gson.JsonObject;
import com.nukateam.ntgl.common.util.annotation.Optional;
import com.nukateam.ntgl.common.util.util.NbtUtils;
import com.nukateam.ntgl.common.util.util.SuperBuilder;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.util.INBTSerializable;

public class Display implements INBTSerializable<CompoundTag> {
    public static final String OFFSET = "Offset";
    public static final String NON_HAND_OFFSET = "NonHandOffset";

    @Optional protected Vec3 offset = Vec3.ZERO;
    @Optional protected Vec3 nonHandOffset = Vec3.ZERO;

    public static Display create(CompoundTag tag){
        var config = new Display();
        config.deserializeNBT(tag);
        return config;
    }

    @Override
    public CompoundTag serializeNBT() {
        var tag = new CompoundTag();
        tag.put(OFFSET, NbtUtils.writeVec3(offset));
        tag.put(NON_HAND_OFFSET, NbtUtils.writeVec3(nonHandOffset));
        return tag;
    }

    @Override
    public void deserializeNBT(CompoundTag tag) {
        if (tag.contains(OFFSET, Tag.TAG_COMPOUND)) {
            this.offset = NbtUtils.readVec3(tag.getCompound(OFFSET));
        }
        if (tag.contains(NON_HAND_OFFSET, Tag.TAG_COMPOUND)) {
            this.nonHandOffset = NbtUtils.readVec3(tag.getCompound(NON_HAND_OFFSET));
        }
    }

    public JsonObject toJsonObject() {
        var object = new JsonObject();
        return object;
    }

    public Vec3 getOffset() {
        return this.offset;
    }

    public Vec3 getNonHandOffset() {
        return this.nonHandOffset;
    }

    public Display copy() {
        var positioned = new Display();
        positioned.offset = this.offset;
        positioned.nonHandOffset = this.nonHandOffset;
        return positioned;
    }

    public static class Builder extends AbstractBuilder<Builder> {
    }

    protected static abstract class AbstractBuilder<T extends AbstractBuilder<T>> extends SuperBuilder<Display, T> {
        private final Display display;

        private AbstractBuilder() {
            this(new Display());
        }

        protected AbstractBuilder(Display display) {
            this.display = display;
        }

        public T setOffset(Vec3 offset) {
            this.display.offset = offset;
            return this.self();
        }

        @Override
        public Display build() {
            return this.display.copy();
        }
    }
}
