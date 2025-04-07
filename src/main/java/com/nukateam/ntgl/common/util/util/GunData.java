package com.nukateam.ntgl.common.util.util;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;

public class GunData {
    public final ItemStack gun;
    public final LivingEntity shooter;

    public GunData(ItemStack gun, LivingEntity shooter) {
        this.gun = gun;
        this.shooter = shooter;
    }
}
