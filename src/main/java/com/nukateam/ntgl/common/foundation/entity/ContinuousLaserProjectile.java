package com.nukateam.ntgl.common.foundation.entity;

import com.nukateam.ntgl.common.base.gun.Gun;
import com.nukateam.ntgl.common.foundation.item.GunItem;
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

    public ContinuousLaserProjectile(EntityType<? extends ProjectileEntity> entityType, Level worldIn, LivingEntity shooter, ItemStack weapon, GunItem item, Gun modifiedGun) {
        super(entityType, worldIn, shooter, weapon, item, modifiedGun);
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
            setupDirection(shooter, weapon, (GunItem) weapon.getItem(), modifiedGun);
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