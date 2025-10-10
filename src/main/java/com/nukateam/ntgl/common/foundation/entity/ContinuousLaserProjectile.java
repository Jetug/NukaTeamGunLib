package com.nukateam.ntgl.common.foundation.entity;

import com.nukateam.ntgl.common.data.GunData;
import com.nukateam.ntgl.common.data.config.gun.Gun;
import com.nukateam.ntgl.common.foundation.item.WeaponItem;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public class ContinuousLaserProjectile extends LaserProjectile {
    private static final float GROUND_FIRE_CHANCE = 0.01f;
    private static final float ENTITY_FIRE_CHANCE = 0.1f;

    public ContinuousLaserProjectile(EntityType<? extends ProjectileEntity> entityType, Level worldIn) {
        super(entityType, worldIn);
    }

    public ContinuousLaserProjectile(EntityType<? extends ProjectileEntity> entityType, Level worldIn,  GunData data) {
        super(entityType, worldIn, data);
    }

    @Override
    public void tick() {
        super.tick();
        if(isServerSide) {
            if (shooter != null && shooter.isDeadOrDying()) {
                this.remove(RemovalReason.KILLED);
            }
            if (!isRemoved())
                trace();
        }
    }

    @Override
    public void trace() {
        if(shooter != null && isServerSide) {
            setupDirection(shooter, weapon, (WeaponItem) weapon.getItem());
            setPos(shooter.getEyePosition());
        }
        super.trace();
    }

    public float getBlockFireChance(){
        return GROUND_FIRE_CHANCE;
    }

    public float getEntityFireChance(){
        return ENTITY_FIRE_CHANCE;
    }
}