package com.nukateam.ntgl.common.util.util;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;

public class GunData {
    public final ItemStack stack;
    public final LivingEntity shooter;

    public GunData(ItemStack stack, LivingEntity shooter) {
        this.stack = stack;
        this.shooter = shooter;
    }
}
