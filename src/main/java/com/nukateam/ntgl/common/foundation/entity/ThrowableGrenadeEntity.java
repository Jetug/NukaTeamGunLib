package com.nukateam.ntgl.common.foundation.entity;

import com.nukateam.example.common.registery.ModGuns;
import com.nukateam.ntgl.Config;
import com.nukateam.ntgl.common.foundation.init.Projectiles;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public class ThrowableGrenadeEntity extends ThrowableItemEntity {
    public float rotation;
    public float prevRotation;

    public ThrowableGrenadeEntity(EntityType<? extends ThrowableItemEntity> entityType, Level worldIn) {
        super(entityType, worldIn);
    }

    public ThrowableGrenadeEntity(EntityType<? extends ThrowableItemEntity> entityType, Level world, LivingEntity entity) {
        super(entityType, world, entity);
        this.setShouldBounce(true);
        this.setGravityVelocity(0.05F);
        this.setItem(new ItemStack(ModGuns.GRENADE.get()));
        this.setMaxLife(20 * 3);
    }

    public ThrowableGrenadeEntity(Level world, LivingEntity entity, int timeLeft) {
        super(Projectiles.THROWABLE_GRENADE.get(), world, entity);
        this.setShouldBounce(true);
        this.setGravityVelocity(0.05F);
        this.setItem(new ItemStack(ModGuns.GRENADE.get()));
        this.setMaxLife(timeLeft);
    }

    @Override
    protected void defineSynchedData() {
    }

    @Override
    public void tick() {
        super.tick();
        this.prevRotation = this.rotation;
        double speed = this.getDeltaMovement().length();
        if (speed > 0.1) {
            this.rotation += speed * 50;
        }
        if (this.level().isClientSide) {
            this.level().addParticle(ParticleTypes.SMOKE, true, this.getX(), this.getY() + 0.25, this.getZ(), 0, 0, 0);
        }
    }

    @Override
    public void onDeath() {
        GrenadeEntity.createExplosion(this, Config.COMMON.grenades.explosionRadius.get().floatValue(), Config.COMMON.grenades.enableBlockRemoval.get());
    }

    @Override
    public boolean alwaysAccepts() {
        return super.alwaysAccepts();
    }
}
