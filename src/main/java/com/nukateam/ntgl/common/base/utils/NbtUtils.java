package com.nukateam.ntgl.common.base.utils;

import com.nukateam.ntgl.common.base.holders.ResourceHolder;
import com.nukateam.ntgl.common.base.holders.FuelType;
import com.nukateam.ntgl.common.data.config.Ammo;
import com.nukateam.ntgl.common.data.config.Fuel;
import com.nukateam.ntgl.common.data.config.gun.Modules;
import com.nukateam.ntgl.common.base.holders.AttachmentType;
import com.nukateam.ntgl.common.base.holders.FireMode;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.common.util.INBTSerializable;

import java.util.*;
import java.util.function.Function;

public class NbtUtils {
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
//
//        var array = new HashSet<FireMode>();
//
//        for (var key: tag.getAllKeys()) {
//            if(tag.contains(key, Tag.TAG_STRING)) {
//                array.add(FireMode.getType(tag.getString(key)));
//            }
//        }
//
//        return array;
    }

    public static LinkedHashSet<ResourceLocation> deserializeResourceLocationSet(CompoundTag tag){
        return deserializeSet(tag, ResourceLocation::tryParse);


//        var array = new HashSet<ResourceLocation>();
//
//        for (var key: tag.getAllKeys()) {
//            if(tag.contains(key, Tag.TAG_STRING)) {
//                array.add(ResourceLocation.tryParse(tag.getString(key)));
//            }
//        }
//
//        return array;
    }

    public static <T extends INBTSerializable> CompoundTag serializeArray(ArrayList<T> array){
        var tag = new CompoundTag();
        for (var i = 0; i < array.size(); i++){
            tag.put(String.valueOf(i), array.get(i).serializeNBT());
        }
        return tag;
    }

    public static <T extends ResourceHolder> CompoundTag serializeHolderArray(ArrayList<T> array){
        var tag = new CompoundTag();
        for (var i = 0; i < array.size(); i++){
            tag.putString(String.valueOf(i), array.get(i).toString());
        }
        return tag;
    }

    public static <T extends ResourceHolder> ArrayList<T> deserializeHolderArray(
            CompoundTag tag, Function<ResourceLocation, T> deserializer)
    {
        var array = new ArrayList<T>();
        for (var key: tag.getAllKeys()) {
            if(tag.contains(key, Tag.TAG_STRING)) {
                var value = tag.getString(key);
                var holder = deserializer.apply(ResourceLocation.tryParse(value));
                array.add(holder);
            }
        }

        return array;
    }


//    public static <T extends INBTSerializable> HashMap<ResourceLocation, T> deserializeMap(
//            CompoundTag tag, Function<ResourceLocation, T> deserializer)
//    {
//        var map = new HashMap<ResourceLocation, T>();
//
//        for (var key: tag.getAllKeys()) {
//            if(tag.contains(key, Tag.TAG_COMPOUND)) {
//
//                var result = deserializer.apply(key, tag.getCompound(key))
//
//                var projectile = new Projectile();
//                var resource = ResourceLocation.tryParse(key);
//
//                map.put(resource, result);
//            }
//        }
//
//        return map;
//    }

    public static HashMap<FuelType, Fuel> deserializeFuelMap(CompoundTag tag){
        var map = new HashMap<FuelType, Fuel>();

        for (var key: tag.getAllKeys()) {
            if(tag.contains(key, Tag.TAG_COMPOUND)) {
                var fuel = new Fuel();
                var resource = FuelType.getType(key);
                fuel.deserializeNBT(tag.getCompound(key));
                map.put(resource, fuel);
            }
        }

        return map;
    }

    public static HashMap<ResourceLocation, Ammo> deserializeProjectileMap(CompoundTag tag){
        var map = new HashMap<ResourceLocation, Ammo>();

        for (var key: tag.getAllKeys()) {
            if(tag.contains(key, Tag.TAG_COMPOUND)) {
                var projectile = new Ammo();
                var resource = ResourceLocation.tryParse(key);

                projectile.deserializeNBT(tag.getCompound(key));
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

    public static <K, R extends INBTSerializable> CompoundTag serializeMap(Map<K, R> map){
        var tag = new CompoundTag();

        for (var entry : map.entrySet()) {
            tag.put(entry.getKey().toString(), entry.getValue().serializeNBT());
        }

        return tag;
    }

    public static <K, R> CompoundTag serializeStringMap(Map<K, R> map){
        var tag = new CompoundTag();

        for (var entry : map.entrySet()) {
            tag.putString(entry.getKey().toString(), entry.getValue().toString());
        }

        return tag;
    }

    public static HashMap<String, String> deserializeStringMap(CompoundTag tag){
        var map = new HashMap<String, String>();

        for (var key: tag.getAllKeys()) {
            if(tag.contains(key, Tag.TAG_STRING)) {
                map.put(key, tag.getString(key));
            }
        }

        return map;
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

    public static Map<AttachmentType, ArrayList<Modules.Attachment>> deserializeAttachmentMap(CompoundTag tag){
        var array = new HashMap<AttachmentType, ArrayList<Modules.Attachment>>();

        for (var key: tag.getAllKeys()) {
            if(tag.contains(key, Tag.TAG_COMPOUND)) {
                array.put(AttachmentType.getType(key), deserializeArray(tag.getCompound(key)));
            }
        }

        return array;
    }
}
