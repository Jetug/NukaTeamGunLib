package com.nukateam.ntgl.common.base.utils;

import com.nukateam.ntgl.common.base.gun.Gun;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.common.util.INBTSerializable;
import org.checkerframework.checker.units.qual.K;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class NbtUtils {
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


    public static <K, R extends INBTSerializable, T extends ArrayList<R>> CompoundTag serializeMap(Map<K, T> map){
        var tag = new CompoundTag();

        for (var key: map.keySet()) {
            tag.put(String.valueOf(key), serializeArray(map.get(key)));
        }

        return tag;
    }

    public static Map<ResourceLocation, ArrayList<Gun.Modules.Attachment>> deserializeAttachmentMap(CompoundTag tag){
        var array = new HashMap<ResourceLocation, ArrayList<Gun.Modules.Attachment>>();
        for (var key: tag.getAllKeys()) {
            if(tag.contains(key, Tag.TAG_COMPOUND)) {
                array.put(ResourceLocation.tryParse(key), deserializeArray(tag.getCompound(key)));
            }
        }

        return array;
    }
}
