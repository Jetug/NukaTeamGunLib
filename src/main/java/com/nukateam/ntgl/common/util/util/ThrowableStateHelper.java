package com.nukateam.ntgl.common.util.util;

import com.nukateam.ntgl.common.data.holders.ThrowMode;
import com.nukateam.ntgl.common.foundation.item.interfaces.IThrowable;
import net.minecraft.world.item.ItemStack;

public class ThrowableStateHelper {
    public static ThrowMode getMode(ItemStack stack) {
        var throwable = (IThrowable)stack.getItem();
        return throwable.getConfig().getGeneral().getMode();
    }
}
