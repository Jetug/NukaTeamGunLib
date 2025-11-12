package com.nukateam.ntgl.common.data.config.weapon;

import com.google.gson.JsonObject;
import com.nukateam.ntgl.common.util.annotation.Optional;
import com.nukateam.ntgl.common.util.util.NbtUtils;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.common.util.INBTSerializable;

public class Zoom implements INBTSerializable<CompoundTag> {
    public static final String FOV_MODIFIER = "FovModifier";
    public static final String OFFSET = "Offset";

    @Optional protected float fovModifier = 1;
    @Optional Vec3 offset = Vec3.ZERO;

    @Override
    public CompoundTag serializeNBT(HolderLookup.Provider provider) {
        var tag = new CompoundTag();
        tag.putFloat(FOV_MODIFIER, this.fovModifier);
        tag.put(OFFSET, NbtUtils.writeVec3(offset));
        return tag;
    }

    @Override
    public void deserializeNBT(HolderLookup.Provider provider, CompoundTag tag) {
        if (tag.contains(FOV_MODIFIER, Tag.TAG_ANY_NUMERIC)) {
            this.fovModifier = tag.getFloat(FOV_MODIFIER);
        }
        if (tag.contains(OFFSET, Tag.TAG_COMPOUND)) {
            this.offset = NbtUtils.readVec3(tag.getCompound(OFFSET));
        }
    }

    public JsonObject toJsonObject() {
        var object = new JsonObject();
        object.addProperty("fovModifier", this.fovModifier);
        return object;
    }

    public static Zoom create(CompoundTag tag){
        var zoom = new Zoom();
        zoom.deserializeNBT(tag);
        return zoom;
    }

    public Zoom copy() {
        var zoom = new Zoom();
        zoom.fovModifier = this.fovModifier;
        zoom.offset = this.offset;

        return zoom;
    }

    public float getFovModifier() {
        return this.fovModifier;
    }

    public Vec3 getOffset() {
        return this.offset;
    }
}
