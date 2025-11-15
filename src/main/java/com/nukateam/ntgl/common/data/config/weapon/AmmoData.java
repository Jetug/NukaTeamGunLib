package com.nukateam.ntgl.common.data.config.weapon;

import com.google.gson.JsonObject;
import com.nukateam.ntgl.common.debug.IDebugWidget;
import com.nukateam.ntgl.common.debug.IEditorMenu;
import com.nukateam.ntgl.common.util.annotation.Optional;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.neoforge.common.util.INBTSerializable;
import net.minecraft.core.HolderLookup;
import org.apache.commons.lang3.tuple.Pair;

import java.util.List;
import java.util.function.Supplier;

public class AmmoData implements INBTSerializable<CompoundTag> {
    @Optional private AmmoConfig ammo = new AmmoConfig();
    @Optional private ProjectileConfig projectile = new ProjectileConfig();

    @Override
    public CompoundTag serializeNBT(HolderLookup.Provider provider) {
        var tag = new CompoundTag();
        tag.put("ammo", this.ammo.serializeNBT(provider));
        tag.put("projectile", this.projectile.serializeNBT(provider));
        return tag;
    }

    @Override
    public void deserializeNBT(HolderLookup.Provider provider, CompoundTag tag) {
        if (tag.contains("ammo", Tag.TAG_COMPOUND)) {
            this.ammo = AmmoConfig.create(tag.getCompound("ammo"));
        }
        if (tag.contains("projectile", Tag.TAG_COMPOUND)) {
            this.projectile = ProjectileConfig.create(tag.getCompound("projectile"));
        }
    }

    public AmmoData copy() {
        var config = new AmmoData();
        config.ammo = this.ammo;
        config.projectile = this.projectile;

        return config;
    }

    public JsonObject toJsonObject() {
        var object = new JsonObject();
        object.add("ammo", this.ammo.toJsonObject());
        object.add("projectile", this.projectile.toJsonObject());
        return object;
    }

    public AmmoConfig getAmmo() {
        return ammo;
    }

    public ProjectileConfig getProjectile() {
        return projectile;
    }

    public static AmmoData create(CompoundTag tag) {
        var ammo = new AmmoData();
        ammo.deserializeNBT(null,tag);
        return ammo;
    }

    public static class Builder {
        private final AmmoData ammo;

        private Builder() {
            this.ammo = new AmmoData();
        }

        private Builder(AmmoData ammo) {
            this.ammo = ammo.copy();
        }

        public static AmmoData.Builder create() {
            return new AmmoData.Builder();
        }

        public static AmmoData.Builder create(AmmoData ammo) {
            return new AmmoData.Builder(ammo);
        }

        public AmmoData build() {
            return this.ammo.copy();
        }
    }
}
