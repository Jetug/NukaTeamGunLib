package com.nukateam.ntgl.common.base.config.gun;

import com.google.common.base.Preconditions;
import com.google.gson.JsonObject;
import com.nukateam.ntgl.common.data.annotation.Optional;
import com.nukateam.ntgl.common.data.util.GunJsonUtil;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraftforge.common.util.INBTSerializable;

import javax.annotation.Nullable;

@Deprecated(forRemoval = true)
public class Display implements INBTSerializable<CompoundTag> {
    @Optional
    @Nullable
    protected Flash flash;

    @Nullable
    public Flash getFlash() {
        return this.flash;
    }

    public static class Flash extends Positioned {
        double size = 0.5;

        @Override
        public CompoundTag serializeNBT() {
            CompoundTag tag = super.serializeNBT();
            tag.putDouble("Size", this.size);
            return tag;
        }

        @Override
        public void deserializeNBT(CompoundTag tag) {
            super.deserializeNBT(tag);
            if (tag.contains("Size", Tag.TAG_ANY_NUMERIC)) {
                this.size = tag.getDouble("Size");
            }
        }

        @Override
        public JsonObject toJsonObject() {
            Preconditions.checkArgument(this.size >= 0, "Muzzle flash size must be more than or equal to zero");
            JsonObject object = super.toJsonObject();
            if (this.size != 0.5) {
                object.addProperty("size", this.size);
            }
            return object;
        }

        public Flash copy() {
            Flash flash = new Flash();
            flash.size = this.size;
            flash.xOffset = this.xOffset;
            flash.yOffset = this.yOffset;
            flash.zOffset = this.zOffset;
            return flash;
        }

        /**
         * @return The size/scale of the muzzle flash render
         */
        public double getSize() {
            return this.size;
        }
    }

    @Override
    public CompoundTag serializeNBT() {
        CompoundTag tag = new CompoundTag();
        if (this.flash != null) {
            tag.put("Flash", this.flash.serializeNBT());
        }
        return tag;
    }

    @Override
    public void deserializeNBT(CompoundTag tag) {
        if (tag.contains("Flash", Tag.TAG_COMPOUND)) {
            CompoundTag flashTag = tag.getCompound("Flash");
            if (!flashTag.isEmpty()) {
                Flash flash = new Flash();
                flash.deserializeNBT(tag.getCompound("Flash"));
                this.flash = flash;
            } else {
                this.flash = null;
            }
        }
    }

    public JsonObject toJsonObject() {
        JsonObject object = new JsonObject();
        if (this.flash != null) {
            GunJsonUtil.addObjectIfNotEmpty(object, "flash", this.flash.toJsonObject());
        }
        return object;
    }

    public Display copy() {
        Display display = new Display();
        if (this.flash != null) {
            display.flash = this.flash.copy();
        }
        return display;
    }
}
