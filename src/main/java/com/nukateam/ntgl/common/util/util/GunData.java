package com.nukateam.ntgl.common.util.util;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;

public class GunData {
    public ItemStack stack;
    public LivingEntity shooter;

    public GunData(ItemStack stack, LivingEntity shooter) {
        this.stack = stack;
        this.shooter = shooter;
    }
}
