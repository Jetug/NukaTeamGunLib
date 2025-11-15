package com.nukateam.ntgl.common.data.config.weapon;

import com.google.common.base.Preconditions;
import com.google.gson.JsonObject;
import com.nukateam.ntgl.common.debug.IDebugWidget;
import com.nukateam.ntgl.common.debug.IEditorMenu;
import com.nukateam.ntgl.common.util.annotation.Optional;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.neoforge.common.util.INBTSerializable;
import net.minecraft.core.HolderLookup;
import org.apache.commons.lang3.tuple.Pair;

import java.util.List;
import java.util.function.Supplier;

public class Fuel implements INBTSerializable<CompoundTag> {
    @Optional
    private int max = 100;
    private boolean mandatory = true;
    private int amountPerUse = 0;
    private AmmoConfig ammo = new AmmoConfig();

    @Override
    public CompoundTag serializeNBT(HolderLookup.Provider provider) {
        var tag = new CompoundTag();
        tag.putInt("Max", this.max);
        tag.putInt("amountPerUse", this.amountPerUse);
        tag.putBoolean("mandatory", this.mandatory);
        tag.put("ammo", this.ammo.serializeNBT(provider));
        return tag;
    }

    @Override
    public void deserializeNBT(HolderLookup.Provider provider, CompoundTag tag) {
        if (tag.contains("Max", Tag.TAG_ANY_NUMERIC)) {
            this.max = tag.getInt("Max");
        }
        if (tag.contains("amountPerUse", Tag.TAG_ANY_NUMERIC)) {
            this.amountPerUse = tag.getInt("amountPerUse");
        }
        if (tag.contains("Max", Tag.TAG_ANY_NUMERIC)) {
            this.mandatory = tag.getBoolean("mandatory");
        }
        if (tag.contains("ammo", Tag.TAG_COMPOUND)) {
            this.ammo = AmmoConfig.create(tag.getCompound("ammo"));
        }
    }

    public JsonObject toJsonObject() {
        Preconditions.checkArgument(this.max > 0, "Max capacity must be more than zero");
        var object = new JsonObject();
        object.addProperty("max", this.max);
        object.addProperty("amountPerUse", this.amountPerUse);
        object.addProperty("mandatory", this.mandatory);
        object.add("ammo", this.ammo.toJsonObject());
        return object;
    }

    public Fuel copy() {
        var projectile = new Fuel();
        projectile.max = this.max;
        projectile.amountPerUse = this.amountPerUse;
        projectile.mandatory = this.mandatory;
        projectile.ammo = this.ammo;

        return projectile;
    }

    public AmmoConfig getAmmo() {
        return ammo;
    }

    public int getMax() {
        return this.max;
    }

    public boolean isMandatory() {
        return mandatory;
    }

    public int getAmountPerUse() {
        return amountPerUse;
    }

    public static Fuel create(CompoundTag tag) {
        var ammo = new Fuel();
        ammo.deserializeNBT(null,tag);
        return ammo;
    }

    public static class Builder {
        private final Fuel ammo;

        private Builder() {
            this.ammo = new Fuel();
        }

        private Builder(Fuel ammo) {
            this.ammo = ammo.copy();
        }

        public static Fuel.Builder create() {
            return new Fuel.Builder();
        }

        public static Fuel.Builder create(Fuel ammo) {
            return new Fuel.Builder(ammo);
        }

        public Fuel build() {
            return this.ammo.copy(); //Copy since the builder could be used again
        }

        public Fuel.Builder setProjectileLife(ResourceLocation id, int life) {
            this.ammo.max = life;
            return this;
        }
    }
}
