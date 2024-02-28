package com.nukateam.gunscore.common.foundation.entity;

import com.nukateam.gunscore.Config;
import com.nukateam.gunscore.common.foundation.init.ModEntities;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

import static com.nukateam.example.common.registery.ModGuns.BASEGRENADE;

public class ThrowableBaseGrenadeEntity extends ThrowableGrenadeEntity{


    public ThrowableBaseGrenadeEntity(EntityType<? extends ThrowableItemEntity> entityType, Level worldIn) {
        super(entityType, worldIn);
    }

    public ThrowableBaseGrenadeEntity(EntityType<? extends ThrowableItemEntity> entityType, Level world, LivingEntity entity) {
        super(entityType, world, entity);
        this.setShouldBounce(true);
        this.setGravityVelocity(0.05F);
        this.setItem(new ItemStack(BASEGRENADE.get()));
        this.setMaxLife(20 * 3);
    }

    public ThrowableBaseGrenadeEntity(Level world, LivingEntity entity, int timeLeft) {
        super(ModEntities.THROWABLE_BASEGRENADE.get(), world, entity);
        this.setShouldBounce(true);
        this.setGravityVelocity(0.05F);
        this.setItem(new ItemStack(BASEGRENADE.get()));
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
        if (this.level.isClientSide) {
            this.level.addParticle(ParticleTypes.SMOKE, true, this.getX(), this.getY() + 0.25, this.getZ(), 0, 0, 0);
        }
    }

    @Override
    public void onDeath() {
        GrenadeEntity.createExplosion(this, Config.COMMON.grenades.explosionRadius.get().floatValue(), true);
    }
}
