package com.nukateam.ntgl.common.util.util;

import com.nukateam.ntgl.common.data.holders.ThrowMode;
import com.nukateam.ntgl.common.foundation.item.interfaces.IThrowable;
import net.minecraft.nbt.Tag;
import net.minecraft.world.item.ItemStack;

public class ThrowableStateHelper {

    public static final String THROW_MODE = "ThrowMode";

//    public static ThrowMode getMode(ItemStack stack) {
//        var throwable = (IThrowable)stack.getItem();
//        return throwable.getConfig().getGeneral().getThrowModes();
//    }

    public static void switchThrowMode(ItemStack stack){
        if(stack.getItem() instanceof IThrowable throwable) {
            var modes = throwable.getConfig().getThrowable().getThrowModes();
            var current = getThrowMode(stack);
            var newMode = SetUtils.cycleSet(modes, current);
            setThrowMode(stack, newMode);
        }
    }

    public static ThrowMode getThrowMode(ItemStack stack) {
        var throwable = (IThrowable)stack.getItem();

        var modes = throwable.getConfig().getThrowable().getThrowModes();
        var tag = stack.getOrCreateTag();
        ThrowMode currentMode = null;

        if(tag.contains(THROW_MODE, Tag.TAG_STRING))
            currentMode = ThrowMode.getType(tag.getString(THROW_MODE));

        if (currentMode == null || !modes.contains(currentMode)) {
            setThrowMode(stack, SetUtils.getFirst(modes));
            return SetUtils.getFirst(modes);
        }
        else return currentMode;
    }

    public static void setThrowMode(ItemStack stack, ThrowMode fireMode) {
        var tag = stack.getOrCreateTag();
        tag.putString(THROW_MODE, fireMode.toString());
        stack.setTag(tag);
    }
}
