package com.nukateam.chassis_core.common.foundation.item;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;

public class StackUtils {
    public static final String DEFAULT = "default";
    public static final String VARIANT = "variant";
    public static final String ATTACHMENTS = "mods";

    public static String getVariant(ItemStack stack) {
        var tag = stack.getOrCreateTag();
        return tag.contains(VARIANT) ? tag.getString(VARIANT) : DEFAULT;
    }

    public static void setVariant(ItemStack stack, String variant) {
        var tag = stack.getOrCreateTag();
        tag.putString(VARIANT, variant);
        stack.setTag(tag);
    }

//    public static void addAttachment(ItemStack stack, String variant){
//        var mods = getAttachments(stack);
//        mods.add(variant);
//        setAttachment(stack, mods);
//    }

    public static boolean hasAttachment(ItemStack stack, String mod) {
        var mods = getAttachments(stack);
        return mods.contains(mod);
    }

    public static ArrayList<String> getAttachments(ItemStack stack) {
        var tag = stack.getOrCreateTag();
        if (!tag.contains(ATTACHMENTS)) return new ArrayList<>();
        var attachments = tag.getCompound(ATTACHMENTS);
        var values = new ArrayList<String>();

        for (var key : attachments.getAllKeys()) {
            values.add(attachments.getString(key));
        }

        return values;
    }

    public static String getAttachment(ItemStack stack, String slot) {
        var tag = stack.getOrCreateTag();
        if (!tag.contains(ATTACHMENTS) || !tag.getCompound(ATTACHMENTS).contains(slot))
            return null;

        return tag.getCompound(ATTACHMENTS).getString(slot);
    }

//    public static void setAttachment(ItemStack stack, Collection<String> variants){
//        var tag = stack.getOrCreateTag();
//        var listTag = new ListTag();
//
//        for(var str : variants){
//            var stringTag = StringTag.valueOf(str);
//            listTag.add(stringTag);
//        }
//
//        tag.put(ATTACHMENTS, listTag);
//        stack.setTag(tag);
//    }

    public static void setAttachment(ItemStack stack, String slot, String attachment) {
        var stackTag = stack.getOrCreateTag();
        var listTag = new CompoundTag();
        listTag.putString(slot, attachment);
        stackTag.put(ATTACHMENTS, listTag);
        stack.setTag(stackTag);
    }

//    public static void setAttachment(ItemStack stack, String variant){
//        var tag = stack.getOrCreateTag();
//
//        var listTag = new ListTag();
//        var stringTag = StringTag.valueOf(variant);
//        listTag.add(stringTag);
//
//        tag.put(ATTACHMENTS, listTag);
//        stack.setTag(tag);
//    }

//    private static ArrayList<String> getAsArray(ListTag listTag){
//        var res = new ArrayList<String>();
//        for(var item : listTag){
//            var strTag = (StringTag)item;
//            res.add(strTag.getAsString());
//        }
//        return res;
//    }
}
