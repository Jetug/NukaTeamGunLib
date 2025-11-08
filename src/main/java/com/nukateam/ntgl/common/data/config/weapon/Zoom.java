package com.nukateam.ntgl.common.data.config.weapon;

import com.google.gson.JsonObject;
import com.nukateam.ntgl.common.util.annotation.Optional;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.phys.Vec3;

public class Zoom extends Positioned{
    public static final String FOV_MODIFIER = "FovModifier";
    @Optional protected float fovModifier = 1;

    @Override
    public CompoundTag serializeNBT() {
        super.serializeNBT();
        CompoundTag tag = new CompoundTag();
        tag.putFloat(FOV_MODIFIER, this.fovModifier);
        return tag;
    }

    @Override
    public void deserializeNBT(CompoundTag tag) {
        super.deserializeNBT(tag);
        if (tag.contains(FOV_MODIFIER, Tag.TAG_ANY_NUMERIC)) {
            this.fovModifier = tag.getFloat(FOV_MODIFIER);
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
        super.copy();
        var zoom = new Zoom();
        zoom.fovModifier = this.fovModifier;
        return zoom;
    }

    public float getFovModifier() {
        return this.fovModifier;
    }
}
