package com.nukateam.ntgl.common.data.config;

import com.google.common.base.Preconditions;
import com.google.gson.JsonObject;
import com.nukateam.ntgl.common.debug.IDebugWidget;
import com.nukateam.ntgl.common.debug.IEditorMenu;
import com.nukateam.ntgl.common.util.annotation.Optional;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.util.INBTSerializable;
import net.minecraftforge.fml.DistExecutor;
import org.apache.commons.lang3.tuple.Pair;

import java.util.List;
import java.util.function.Supplier;

public class Fuel implements INBTSerializable<CompoundTag>, IEditorMenu {
    public static final String TYPE = "Type";
    @Optional
    private int max = 100;
    private boolean isMandatory = true;
    private AmmoConfig ammo = new AmmoConfig();

    @Override
    public CompoundTag serializeNBT() {
        var tag = new CompoundTag();
        tag.putInt("Max", this.max);
        tag.putBoolean("isMandatory", this.isMandatory);
        tag.put("ammo", this.ammo.serializeNBT());
        return tag;
    }

    @Override
    public void deserializeNBT(CompoundTag tag) {
        if (tag.contains("Max", Tag.TAG_ANY_NUMERIC)) {
            this.max = tag.getInt("Max");
        }
        if (tag.contains("Max", Tag.TAG_ANY_NUMERIC)) {
            this.isMandatory = tag.getBoolean("isMandatory");
        }
        if (tag.contains("ammo", Tag.TAG_COMPOUND)) {
            this.ammo = AmmoConfig.create(tag.getCompound("ammo"));
        }
    }

    public JsonObject toJsonObject() {
        Preconditions.checkArgument(this.max > 0, "Max capacity must be more than zero");
        var object = new JsonObject();
        object.addProperty("max", this.max);
        object.addProperty("isMandatory", this.isMandatory);
        object.add("ammo", this.ammo.toJsonObject());
        return object;
    }

    public Fuel copy() {
        var projectile = new Fuel();
        projectile.max = this.max;
        projectile.isMandatory = this.isMandatory;
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
        return isMandatory;
    }

    public static Fuel create(CompoundTag tag) {
        var ammo = new Fuel();
        ammo.deserializeNBT(tag);
        return ammo;
    }

    @Override
    public Component getEditorLabel() {
        return Component.literal("Fuel");
    }

    @Override
    public void getEditorWidgets(List<Pair<Component, Supplier<IDebugWidget>>> widgets) {
        DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> {});
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
