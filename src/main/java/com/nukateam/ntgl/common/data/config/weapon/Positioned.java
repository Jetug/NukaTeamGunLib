package com.nukateam.ntgl.common.data.config.weapon;

import com.google.gson.JsonObject;
import com.nukateam.ntgl.common.util.annotation.Optional;
import com.nukateam.ntgl.common.util.util.NbtUtils;
import com.nukateam.ntgl.common.util.util.SuperBuilder;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.util.INBTSerializable;

public class Positioned implements INBTSerializable<CompoundTag> {
    public static final String OFFSET = "Offset";

    @Optional protected Vec3 offset = Vec3.ZERO;

    @Override
    public CompoundTag serializeNBT() {
        CompoundTag tag = new CompoundTag();
        tag.put(OFFSET, NbtUtils.writeVec3(offset));
        return tag;
    }

    @Override
    public void deserializeNBT(CompoundTag tag) {
        if (tag.contains(OFFSET, Tag.TAG_COMPOUND)) {
            this.offset = NbtUtils.readVec3(tag.getCompound(OFFSET));
        }
    }

    public JsonObject toJsonObject() {
        var object = new JsonObject();
        return object;
    }

    public Vec3 getOffset() {
        return this.offset;
    }


    public Positioned copy() {
        var positioned = new Positioned();
        positioned.offset = this.offset;
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

        public T setOffset(Vec3 offset) {
            this.positioned.offset = offset;
            return this.self();
        }

        @Override
        public Positioned build() {
            return this.positioned.copy();
        }
    }
}
