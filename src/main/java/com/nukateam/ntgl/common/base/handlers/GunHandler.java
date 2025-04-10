package com.nukateam.ntgl.common.base.handlers;

import com.nukateam.ntgl.common.data.config.gun.Gun;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;

public class GunHandler {
    public int getAmmoAfterShoot(ItemStack gun, LivingEntity shooter){
        return Math.max(0, Gun.getAmmo(gun) - 1);
    }
}
