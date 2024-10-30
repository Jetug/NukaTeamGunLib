package com.nukateam.ntgl.common.data.interfaces;

import net.minecraft.world.item.ItemStack;

public interface IConfigProvider<T> {
    T getConfig();
}
