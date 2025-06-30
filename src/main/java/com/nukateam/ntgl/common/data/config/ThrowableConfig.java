package com.nukateam.ntgl.common.data.config;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.nukateam.ntgl.common.base.utils.NbtUtils;
import com.nukateam.ntgl.common.debug.IDebugWidget;
import com.nukateam.ntgl.common.debug.IEditorMenu;
import com.nukateam.ntgl.common.util.annotation.Ignored;
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
    protected Projectile projectile = new Projectile();
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
        tag.put("Projectile", projectile.serializeNBT());
        tag.put("Sounds", NbtUtils.serializeStringMap(this.sounds));
        tag.put("Textures", NbtUtils.serializeStringMap(this.textures));
        return tag;
    }

    @Override
    public void deserializeNBT(CompoundTag tag) {
        if (tag.contains("Projectile", Tag.TAG_COMPOUND)) {
            this.projectile = Projectile.create(tag.getCompound("Projectile"));
        }
        if (tag.contains("Sounds", Tag.TAG_COMPOUND)) {
            this.sounds = deserializeSounds(tag.getCompound("Sounds"));
        }
        if (tag.contains("Textures", Tag.TAG_COMPOUND)) {
            this.textures = NbtUtils.deserializeRLMap(tag.getCompound("Textures"));
        }
    }

    public JsonObject toJsonObject() {
        var gson = new Gson();
        var object = new JsonObject();
        object.add("projectile", this.projectile.toJsonObject());
        GunJsonUtil.addObjectIfNotEmpty(object,"sounds", gson.toJsonTree(sounds).getAsJsonObject());
        GunJsonUtil.addObjectIfNotEmpty(object,"textures", gson.toJsonTree(textures).getAsJsonObject());
        return object;
    }

    public ThrowableConfig copy() {
        var gun = new ThrowableConfig();
        gun.projectile = projectile;
        gun.sounds = (HashMap<String, ResourceLocation>) this.sounds.clone();
        gun.textures = (HashMap<String, ResourceLocation>) this.textures.clone();
        return gun;
    }

    public Projectile getProjectile() {
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
