package com.nukateam.ntgl.common.base.utils;

import com.nukateam.ntgl.common.base.gun.Gun;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraftforge.common.util.INBTSerializable;

import java.util.ArrayList;

public class NbtUtils {
    public static <T extends INBTSerializable> CompoundTag serializeNbt(ArrayList<T> array){
        var tag = new CompoundTag();
        for (var i = 0; i < array.size(); i++){
            tag.put(String.valueOf(i), array.get(i).serializeNBT());
        }
        return tag;
    }

    public static ArrayList<Gun.Modules.Attachment> deserializeNbt(CompoundTag tag){
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
}
