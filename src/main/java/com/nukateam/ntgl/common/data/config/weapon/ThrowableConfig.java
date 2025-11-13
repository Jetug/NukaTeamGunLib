package com.nukateam.ntgl.common.data.config.weapon;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.nukateam.ntgl.common.data.holders.ThrowMode;
import com.nukateam.ntgl.common.util.util.NbtUtils;
import com.nukateam.ntgl.common.debug.IDebugWidget;
import com.nukateam.ntgl.common.debug.IEditorMenu;
import com.nukateam.ntgl.common.util.annotation.Ignored;
import com.nukateam.ntgl.common.util.annotation.Optional;
import com.nukateam.ntgl.common.util.util.GunJsonUtil;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.neoforge.common.util.INBTSerializable;
import net.neoforged.neoforge.fml.DistExecutor;
import org.apache.commons.lang3.tuple.Pair;

import java.util.*;
import java.util.function.Supplier;

public class ThrowableConfig implements INBTSerializable<CompoundTag>, IEditorMenu {
    public static final String PREPARE_TIME = "prepareTime";
    public static final String THROW_TIME = "throwTime";
    public static final String AMMO_DATA = "AmmoData";

    @Optional LinkedHashSet<ThrowMode> mode = new LinkedHashSet<>(List.of(ThrowMode.SAFE));
    private int prepareTime = 0;
    private int throwTime = 1;
    protected AmmoData ammoData = new AmmoData();

    @Override
    public Component getEditorLabel() {
        return Component.literal("Gun");
    }

    @Override
    public void getEditorWidgets(List<Pair<Component, Supplier<IDebugWidget>>> widgets) {
        DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> {});
    }

    @Override
    public CompoundTag serializeNBT(HolderLookup.Provider provider) {
        var tag = new CompoundTag();
        tag.put("mode", NbtUtils.serializeSet(this.mode));
        tag.putInt(PREPARE_TIME, prepareTime);
        tag.putInt(THROW_TIME, throwTime);
        tag.put(AMMO_DATA, ammoData.serializeNBT());
        return tag;
    }

    @Override
    public void deserializeNBT(HolderLookup.Provider provider, CompoundTag tag) {
        if (tag.contains("mode", Tag.TAG_COMPOUND)) {
            this.mode = NbtUtils.deserializeSet(tag.getCompound("mode"), ThrowMode::getType);
        }
        if (tag.contains(PREPARE_TIME, Tag.TAG_INT)) {
            this.prepareTime = tag.getInt(PREPARE_TIME);
        }
        if (tag.contains(THROW_TIME, Tag.TAG_INT)) {
            this.throwTime = tag.getInt(THROW_TIME);
        }
        if (tag.contains(AMMO_DATA, Tag.TAG_COMPOUND)) {
            this.ammoData = AmmoData.create(tag.getCompound(AMMO_DATA));
        }
    }

    public JsonObject toJsonObject() {
        var gson = new Gson();
        var object = new JsonObject();
        object.addProperty("mode", this.mode.toString());
        object.addProperty("prepareTime", this.prepareTime);
        object.addProperty("throwTime", this.throwTime);
        object.add("projectile", this.ammoData.toJsonObject());
        return object;
    }

    public ThrowableConfig copy() {
        var config = new ThrowableConfig();
        config.mode = (LinkedHashSet<ThrowMode>)mode.clone();
        config.prepareTime = prepareTime;
        config.throwTime = throwTime;
        config.ammoData = ammoData;
        return config;
    }

    public ProjectileConfig getProjectile() {
        return ammoData.getProjectile();
    }

    public AmmoConfig getAmmo(){
        return ammoData.getAmmo();
    }

    private HashMap<String, ResourceLocation> deserializeSounds(CompoundTag tag){
        var result = new HashMap<String, ResourceLocation>();
        for (var key: tag.getAllKeys()) {
            if(tag.contains(key, Tag.TAG_STRING)) {
                result.put(key, createSound(tag, key));
            }
        }
        return result;
    }

    private ResourceLocation createSound(CompoundTag tag, String key) {
        var sound = tag.getString(key);
        return sound.isEmpty() ? null : ResourceLocation.tryParse(sound);
    }

    public int getPrepareTime() {
        return prepareTime;
    }

    public int getThrowTime() {
        return throwTime;
    }

    public LinkedHashSet<ThrowMode> getThrowModes() {
        return mode;
    }

    public static ThrowableConfig create(ResourceLocation id, CompoundTag tag) {
        var gun = new ThrowableConfig();
        gun.deserializeNBT(tag);
        return gun;
    }

    public void onCreated(String id){}

    public static class Builder {
        private final ThrowableConfig gun;

        private Builder() {
            this.gun = new ThrowableConfig();
        }

        private Builder(ThrowableConfig gun) {
            this.gun = gun.copy();
        }

        public static Builder create() {
            return new Builder();
        }

        public static Builder create(ThrowableConfig gun) {
            return new Builder(gun);
        }

        public ThrowableConfig build() {
            return this.gun.copy(); //Copy since the builder could be used again
        }

    }
}
