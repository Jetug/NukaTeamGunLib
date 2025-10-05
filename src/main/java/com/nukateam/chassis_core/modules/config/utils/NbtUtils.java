package com.nukateam.chassis_core.modules.config.utils;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;

import java.util.LinkedHashSet;
import java.util.Set;
import java.util.function.Function;

public class NbtUtils {
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


    public static LinkedHashSet<ResourceLocation> deserializeRLSet(CompoundTag tag){
        return deserializeSet(tag, ResourceLocation::tryParse);
    }
}
