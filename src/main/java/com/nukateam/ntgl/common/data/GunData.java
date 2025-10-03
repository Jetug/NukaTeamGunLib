package com.nukateam.ntgl.common.data;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;

import javax.annotation.Nullable;

public class GunData {
    @Nullable public final ItemStack gun;
    @Nullable public ItemStack attachment;
    @Nullable public final LivingEntity shooter;

    public GunData(ItemStack gun, LivingEntity shooter) {
        this.gun = gun;
        this.shooter = shooter;
    }
}
