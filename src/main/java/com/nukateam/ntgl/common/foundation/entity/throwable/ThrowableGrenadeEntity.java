package com.nukateam.ntgl.common.foundation.entity.throwable;

import com.nukateam.example.common.registery.ModGuns;
import com.nukateam.ntgl.common.data.config.ProjectileConfig;
import com.nukateam.ntgl.common.foundation.init.Projectiles;
import com.nukateam.ntgl.common.foundation.item.interfaces.IThrowable;
import com.nukateam.ntgl.common.util.world.ExplosionUtils;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public class ThrowableGrenadeEntity<T extends Item & IThrowable> extends ThrowableItemEntity<T> {
    public ThrowableGrenadeEntity(EntityType<? extends ThrowableItemEntity> entityType, Level worldIn) {
        super(entityType, worldIn);
    }

    public ThrowableGrenadeEntity(EntityType<? extends ThrowableItemEntity> entityType, Level world, LivingEntity thrower, T item, int timeLeft) {
        super(entityType, world, thrower, item);
        this.setShouldBounce(true);
        this.setGravityVelocity(0.05F);
        this.setItem(new ItemStack(item));
        this.setMaxLife(timeLeft);
    }

    public ThrowableGrenadeEntity(Level world, LivingEntity entity, T item, int timeLeft) {
        this(Projectiles.THROWABLE_GRENADE.get(), world, entity, item, timeLeft);
        this.setItem(new ItemStack(item));
    }

    @Override
    protected void defineSynchedData() {}

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
        explode();
    }

    public void explode() {
        ExplosionUtils.createExplosion(this, projectile.getExplosion(), position());
    }

    @Override
    public boolean alwaysAccepts() {
        return super.alwaysAccepts();
    }
}
