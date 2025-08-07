package com.nukateam.ntgl.common.data.config;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.nukateam.ntgl.common.data.holders.ThrowMode;
import com.nukateam.ntgl.common.data.holders.ProjectileType;
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
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.util.INBTSerializable;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.loading.FMLEnvironment;
import org.apache.commons.lang3.tuple.Pair;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;

public class ThrowableConfig implements INBTSerializable<CompoundTag>, IEditorMenu {

    public static final String EQUIP_TIME = "equipTime";
    public static final String PREPARE_TIME = "prepareTime";
    public static final String THROW_TIME = "throwTime";
    public static final String GENERAL = "General";
    public static final String PROJECTILE = "Projectile";
    public static final String SOUNDS = "Sounds";
    public static final String TEXTURES = "Textures";

    public static class General implements INBTSerializable<CompoundTag>{
        @Optional LinkedHashSet<ThrowMode> mode = new LinkedHashSet<>(List.of(ThrowMode.SAFE));
        @Optional private ProjectileType projectile = ProjectileType.BULLET;
        private int equipTime = 0;
        private int prepareTime = 0;
        private int throwTime = 1;

        public static General create(CompoundTag tag) {
            var config = new General();
            config.deserializeNBT(tag);
            return config;
        }

        @Override
        public CompoundTag serializeNBT() {
            var tag = new CompoundTag();
            tag.put("mode", NbtUtils.serializeSet(this.mode));
            tag.putString("projectile", projectile.toString());
            tag.putInt(EQUIP_TIME, equipTime);
            tag.putInt(PREPARE_TIME, prepareTime);
            tag.putInt(THROW_TIME, throwTime);
            return tag;
        }

        @Override
        public void deserializeNBT(CompoundTag tag) {
            if (tag.contains("mode", Tag.TAG_COMPOUND)) {
                this.mode = NbtUtils.deserializeSet(tag.getCompound("mode"), ThrowMode::getType);
            }
            if (tag.contains("projectile", Tag.TAG_STRING)) {
                this.projectile = ProjectileType.getType(tag.getString("projectile"));
            }
            if (tag.contains(EQUIP_TIME, Tag.TAG_INT)) {
                this.equipTime = tag.getInt(EQUIP_TIME);
            }
            if (tag.contains(PREPARE_TIME, Tag.TAG_INT)) {
                this.prepareTime = tag.getInt(PREPARE_TIME);
            }
            if (tag.contains(THROW_TIME, Tag.TAG_INT)) {
                this.throwTime = tag.getInt(THROW_TIME);
            }
        }

        public JsonObject toJsonObject() {
            var object = new JsonObject();
            object.addProperty("mode", this.mode.toString());
            object.addProperty("projectile", this.projectile.toString());
            object.addProperty("equipTime", this.equipTime);
            object.addProperty("prepareTime", this.prepareTime);
            object.addProperty("throwTime", this.throwTime);
            return object;
        }

        public General copy() {
            var gun = new General();
            gun.mode = (LinkedHashSet<ThrowMode>)mode.clone();
            gun.projectile = projectile;
            gun.equipTime = equipTime;
            gun.prepareTime = prepareTime;
            gun.throwTime = throwTime;
            return gun;
        }

        public ProjectileType getProjectileType() {
            return projectile;
        }

        public LinkedHashSet<ThrowMode> getThrowModes() {
            return mode;
        }

        public int getEquipTime() {
            return equipTime;
        }

        public int getPrepareTime() {
            return prepareTime;
        }

        public int getThrowTime() {
            return throwTime;
        }
    }

    protected General general = new General();
    protected ProjectileConfig projectile = new ProjectileConfig();
    protected HashMap<String, ResourceLocation> sounds = new HashMap<>();
    protected HashMap<String, ResourceLocation> textures = new HashMap<>();
    @Ignored
    protected HashMap<String, ResourceLocation> preparedTextures = new HashMap<>();

    @Override
    public Component getEditorLabel() {
        return Component.literal("Gun");
    }

    @Override
    public void getEditorWidgets(List<Pair<Component, Supplier<IDebugWidget>>> widgets) {
        DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> {});
    }

    @Override
    public CompoundTag serializeNBT() {
        var tag = new CompoundTag();
        tag.put(GENERAL, general.serializeNBT());
        tag.put(PROJECTILE, projectile.serializeNBT());
        tag.put(SOUNDS, NbtUtils.serializeStringMap(this.sounds));
        tag.put(TEXTURES, NbtUtils.serializeStringMap(this.textures));
        return tag;
    }

    @Override
    public void deserializeNBT(CompoundTag tag) {
        if (tag.contains(GENERAL, Tag.TAG_COMPOUND)) {
            this.general = General.create(tag.getCompound(GENERAL));
        }
        if (tag.contains(PROJECTILE, Tag.TAG_COMPOUND)) {
            this.projectile = ProjectileConfig.create(tag.getCompound(PROJECTILE));
        }
        if (tag.contains(SOUNDS, Tag.TAG_COMPOUND)) {
            this.sounds = deserializeSounds(tag.getCompound(SOUNDS));
        }
        if (tag.contains(TEXTURES, Tag.TAG_COMPOUND)) {
            this.textures = NbtUtils.deserializeRLMap(tag.getCompound(TEXTURES));
        }
    }

    public JsonObject toJsonObject() {
        var gson = new Gson();
        var object = new JsonObject();
        object.add("projectile", this.projectile.toJsonObject());
        object.add("general", this.general.toJsonObject());
        GunJsonUtil.addObjectIfNotEmpty(object,"sounds", gson.toJsonTree(sounds).getAsJsonObject());
        GunJsonUtil.addObjectIfNotEmpty(object,"textures", gson.toJsonTree(textures).getAsJsonObject());
        return object;
    }

    public ThrowableConfig copy() {
        var gun = new ThrowableConfig();
        gun.general = general;
        gun.projectile = projectile;
        gun.sounds = (HashMap<String, ResourceLocation>) this.sounds.clone();
        gun.textures = (HashMap<String, ResourceLocation>) this.textures.clone();
        return gun;
    }

    public General getGeneral() {
        return general;
    }

    public ProjectileConfig getProjectile() {
        return projectile;
    }

    public HashMap<String, ResourceLocation> getSoundsMap() {
        return sounds;
    }

    public Map<String, ResourceLocation> getTextures() {
        return preparedTextures;
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

    public static ThrowableConfig create(ResourceLocation id, CompoundTag tag) {
        var gun = new ThrowableConfig();
        gun.deserializeNBT(tag);
        prepareTextures(id.getPath(), gun);
        return gun;
    }

    public void onCreated(String id){
        prepareTextures(id, this);
    }

    private static void prepareTextures(String itemId, ThrowableConfig gun) {
        if(FMLEnvironment.dist == Dist.CLIENT) {
            CompletableFuture.runAsync(() -> {
                gun.textures.forEach((variant, path) -> {
//                        var texture = resourceExists(path) ? path : getTexture(itemId, path);
                    var texture = getTexture(itemId, path);
                    gun.preparedTextures.put(variant, texture);
                });
            });
        }
    }

    @NotNull
    private static ResourceLocation getTexture(String itemId, ResourceLocation path) {
        return ResourceLocation.tryBuild(path.getNamespace(), "textures/guns/" + itemId + "/" + path.getPath() + ".png");
    }

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
