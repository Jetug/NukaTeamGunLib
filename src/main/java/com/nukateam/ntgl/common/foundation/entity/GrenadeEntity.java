package com.nukateam.ntgl.common.foundation.entity;

import com.nukateam.ntgl.common.data.GunData;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;

/**
 * Author: MrCrayfish
 */
public class GrenadeEntity extends ProjectileEntity {
    public GrenadeEntity(EntityType<? extends ProjectileEntity> entityType, Level world) {
        super(entityType, world);
    }

    public GrenadeEntity(EntityType<? extends ProjectileEntity> entityType, Level world, GunData data) {
        super(entityType, world, data);
    }
}
