package com.nukateam.ntgl.common.base.utils;

import com.nukateam.ntgl.common.base.config.Ammo;
import com.nukateam.ntgl.common.base.gun.AttachmentType;
import com.nukateam.ntgl.common.base.gun.FireMode;
import com.nukateam.ntgl.common.base.config.Gun;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.common.util.INBTSerializable;

import java.util.*;

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

    public static Set<FireMode> deserializeFireMode(CompoundTag tag){
        var array = new HashSet<FireMode>();

        for (var key: tag.getAllKeys()) {
            if(tag.contains(key, Tag.TAG_STRING)) {
                array.add(FireMode.getType(ResourceLocation.tryParse(tag.getString(key))));
            }
        }

        return array;
    }
    public static Set<ResourceLocation> deserializeAmmoSet(CompoundTag tag){
        var array = new HashSet<ResourceLocation>();

        for (var key: tag.getAllKeys()) {
            if(tag.contains(key, Tag.TAG_STRING)) {
                array.add(ResourceLocation.tryParse(tag.getString(key)));
            }
        }

        return array;
    }

    public static <T extends INBTSerializable> CompoundTag serializeArray(ArrayList<T> array){
        var tag = new CompoundTag();
        for (var i = 0; i < array.size(); i++){
            tag.put(String.valueOf(i), array.get(i).serializeNBT());
        }
        return tag;
    }

    public static ArrayList<Gun.Modules.Attachment> deserializeArray(CompoundTag tag){
        var array = new ArrayList<Gun.Modules.Attachment>();
        for (var key: tag.getAllKeys()) {
            if(tag.contains(key, Tag.TAG_COMPOUND)) {
                var val = new Gun.Modules.Attachment();
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

    public static Map<String, String> deserializeStringMap(CompoundTag tag){
        var map = new HashMap<String, String>();

        for (var key: tag.getAllKeys()) {
            if(tag.contains(key, Tag.TAG_STRING)) {
                map.put(key, tag.getString(key));
            }
        }

        return map;
    }

    public static Map<String, ResourceLocation> deserializeRLMap(CompoundTag tag){
        var map = new HashMap<String, ResourceLocation>();

        for (var key: tag.getAllKeys()) {
            if(tag.contains(key, Tag.TAG_STRING)) {
                map.put(key, ResourceLocation.tryParse(tag.getString(key)));
            }
        }

        return map;
    }

    public static Map<ResourceLocation, Ammo> deserializeProjectileMap(CompoundTag tag){
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

    public static <K, R extends INBTSerializable, T extends ArrayList<R>> CompoundTag serializeArrayMap(Map<K, T> map){
        var tag = new CompoundTag();

        for (var key: map.keySet()) {
            tag.put(key.toString(), serializeArray(map.get(key)));
        }

        return tag;
    }

    public static Map<AttachmentType, ArrayList<Gun.Modules.Attachment>> deserializeAttachmentMap(CompoundTag tag){
        var array = new HashMap<AttachmentType, ArrayList<Gun.Modules.Attachment>>();

        for (var key: tag.getAllKeys()) {
            if(tag.contains(key, Tag.TAG_COMPOUND)) {
                array.put(AttachmentType.getType(key), deserializeArray(tag.getCompound(key)));
            }
        }

        return array;
    }
}
