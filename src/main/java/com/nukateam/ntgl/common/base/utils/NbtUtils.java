package com.nukateam.ntgl.common.base.utils;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;

import java.util.ArrayList;

public class NbtUtils {
    public static CompoundTag serializeNbt(ArrayList<String> array){
        var tag = new CompoundTag();
        for (var i = 0; i < array.size(); i++){
            tag.putString(String.valueOf(i), array.get(i));
        }
        return tag;
    }

    public static ArrayList<String> deserializeNbt(CompoundTag tag){
        var array = new ArrayList<String>();
        for (var key: tag.getAllKeys()) {
            try{
                array.add(tag.getString(key));
            }
            catch (Exception ignored){}
        }

        return array;
    }
}
