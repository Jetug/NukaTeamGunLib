package com.nukateam.ntgl.common.foundation.entity;

import com.nukateam.ntgl.common.data.config.gun.Gun;
import com.nukateam.ntgl.common.foundation.item.WeaponItem;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

/**
 * Author: MrCrayfish
 */
public class GrenadeEntity extends ProjectileEntity {
    public GrenadeEntity(EntityType<? extends ProjectileEntity> entityType, Level world) {
        super(entityType, world);
    }

    public GrenadeEntity(EntityType<? extends ProjectileEntity> entityType, Level world, LivingEntity shooter, ItemStack weapon, WeaponItem item, Gun modifiedGun) {
        super(entityType, world, shooter, weapon, item, modifiedGun);
    }
}
