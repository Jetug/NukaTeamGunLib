package com.nukateam.ntgl.common.util.util;

import com.nukateam.ntgl.common.data.holders.AmmoHolder;
import com.nukateam.ntgl.common.data.config.weapon.ProjectileConfig;
import com.nukateam.ntgl.common.data.config.weapon.Fuel;
import com.nukateam.ntgl.common.data.config.weapon.Modules;
import com.nukateam.ntgl.common.data.holders.AttachmentType;
import com.nukateam.ntgl.common.data.holders.FireMode;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.common.util.INBTSerializable;
import javax.annotation.Nullable;

import java.util.*;
import java.util.function.BiFunction;
import java.util.function.Function;

public class NbtUtils {
    public static CompoundTag writeVec3(Vec3 vec) {
        var vecTag = new CompoundTag();
        vecTag.putDouble("x", vec.x);
        vecTag.putDouble("y", vec.y);
        vecTag.putDouble("z", vec.z);
        return vecTag;
    }

    public static Vec3 readVec3(@Nullable CompoundTag tag) {
        if(tag != null) {
            return new Vec3(
                    tag.getDouble("x"),
                    tag.getDouble("y"),
                    tag.getDouble("z")
            );
        }
        return Vec3.ZERO;
    }

    public static CompoundTag serializeStringArray(ArrayList<String> array){
        var tag = new CompoundTag();
        for (var i = 0; i < array.size(); i++)
            tag.putString(String.valueOf(i), array.get(i));
        return tag;
    }

    public static ArrayList<String> deserializeStringArray(CompoundTag tag){
        var array = new ArrayList<String>();
        for (var key: tag.getAllKeys()) {
            if(tag.contains(key, Tag.TAG_STRING))
                array.add(tag.getString(key));
        }

        return array;
    }

    public static <T> CompoundTag serializeSet(Set<T> array){
        var tag = new CompoundTag();
        var iterator = array.iterator();
        var i = 0;

        while (iterator.hasNext()){
            tag.putString(String.valueOf(i), iterator.next().toString());
            i++;
        }

        return tag;
    }

    public static <R> LinkedHashSet<R> deserializeSet(CompoundTag tag, Function<String, R> deserializer){
        var set = new LinkedHashSet<R>();

        for (var key: tag.getAllKeys()) {
            if(tag.contains(key, Tag.TAG_STRING)) {
                set.add(deserializer.apply(tag.getString(key)));
            }
        }

        return set;
    }

    public static LinkedHashSet<FireMode> deserializeFireMode(CompoundTag tag){
        return deserializeSet(tag, (FireMode::getType));
    }

    public static LinkedHashSet<ResourceLocation> deserializeResourceLocationSet(CompoundTag tag){
        return deserializeSet(tag, ResourceLocation::tryParse);
    }

    public static <T extends INBTSerializable> CompoundTag serializeArray(ArrayList<T> array){
        var tag = new CompoundTag();
        for (var i = 0; i < array.size(); i++){
            tag.put(String.valueOf(i), array.get(i).serializeNBT());
        }
        return tag;
    }

    public static HashMap<AmmoHolder, Fuel> deserializeFuelMap(CompoundTag tag){
        var map = new HashMap<AmmoHolder, Fuel>();

        for (var key: tag.getAllKeys()) {
            if(tag.contains(key, Tag.TAG_COMPOUND)) {
                var fuel = new Fuel();
                var resource = AmmoHolder.getType(key);
                fuel.deserializeNBT(tag.getCompound(key));
                map.put(resource, fuel);
            }
        }

        return map;
    }

    public static <K, R extends INBTSerializable> CompoundTag serializeMap(Map<K, R> map){
        var tag = new CompoundTag();

        for (var entry : map.entrySet()) {
            tag.put(entry.getKey().toString(), entry.getValue().serializeNBT());
        }

        return tag;
    }

    public static <K, V> HashMap<K, V> deserializeMap(CompoundTag tag,
                                                   Function<String, K> keyDeserializer,
                                                   BiFunction<CompoundTag, String, V> valueDeserializer){
        var map = new HashMap<K, V>();

        for (var nbtKey : tag.getAllKeys()) {
            if(tag.contains(nbtKey)) {
                var key = keyDeserializer.apply(nbtKey);
                var value = valueDeserializer.apply(tag, nbtKey);
                map.put(key, value);
            }
        }

        return map;
    }

    public static <V> LinkedHashMap<ResourceLocation, V> deserializeLinkedMap(CompoundTag tag, Function<CompoundTag, V> deserializer){
        var map = new LinkedHashMap<ResourceLocation, V>();

        for (var nbtKey : tag.getAllKeys()) {
            if(tag.contains(nbtKey, Tag.TAG_COMPOUND)) {
                var resource = ResourceLocation.tryParse(nbtKey);
                var value = deserializer.apply(tag.getCompound(nbtKey));
                map.put(resource, value);
            }
        }

        return map;
    }

    public static HashMap<ResourceLocation, ProjectileConfig> deserializeProjectileMap(CompoundTag tag){
        var map = new HashMap<ResourceLocation, ProjectileConfig>();

        for (var key: tag.getAllKeys()) {
            if(tag.contains(key, Tag.TAG_COMPOUND)) {
                var resource = ResourceLocation.tryParse(key);
                var projectile = ProjectileConfig.create(tag.getCompound(key));
                map.put(resource, projectile);
            }
        }

        return map;
    }

    public static ArrayList<Modules.Attachment> deserializeArray(CompoundTag tag){
        var array = new ArrayList<Modules.Attachment>();
        for (var key: tag.getAllKeys()) {
            if(tag.contains(key, Tag.TAG_COMPOUND)) {
                var val = new Modules.Attachment();
                val.deserializeNBT(tag.getCompound(key));
                array.add(val);
            }
        }

        return array;
    }

    public static <K, R> CompoundTag serializeStringMap(Map<K, R> map){
        var tag = new CompoundTag();

        for (var entry : map.entrySet()) {
            tag.putString(entry.getKey().toString(), entry.getValue().toString());
        }

        return tag;
    }

    public static HashMap<String, ResourceLocation> deserializeRLMap(CompoundTag tag){
        var map = new HashMap<String, ResourceLocation>();

        for (var key: tag.getAllKeys()) {
            if(tag.contains(key, Tag.TAG_STRING)) {
                map.put(key, ResourceLocation.tryParse(tag.getString(key)));
            }
        }

        return map;
    }

    public static <K, R extends INBTSerializable, T extends ArrayList<R>> CompoundTag serializeArrayMap(Map<K, T> map){
        var tag = new CompoundTag();

        for (var key: map.keySet()) {
            tag.put(key.toString(), serializeArray(map.get(key)));
        }

        return tag;
    }

    public static LinkedHashMap<AttachmentType, ArrayList<Modules.Attachment>> deserializeAttachmentMap(CompoundTag tag){
        var array = new LinkedHashMap<AttachmentType, ArrayList<Modules.Attachment>>();

        for (var key: tag.getAllKeys()) {
            if(tag.contains(key, Tag.TAG_COMPOUND)) {
                array.put(AttachmentType.getType(key), deserializeArray(tag.getCompound(key)));
            }
        }

        return array;
    }
}
