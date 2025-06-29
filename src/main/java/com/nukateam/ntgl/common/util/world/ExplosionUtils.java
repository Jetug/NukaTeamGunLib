package com.nukateam.ntgl.common.util.world;

import com.nukateam.ntgl.Config;
import com.nukateam.ntgl.common.data.config.ExplosionConfig;
import com.nukateam.ntgl.common.foundation.entity.ProjectileEntity;
import com.nukateam.ntgl.common.util.interfaces.IExplosionDamageable;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.protocol.game.ClientboundExplodePacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.ForgeEventFactory;

import javax.annotation.Nullable;

public class ExplosionUtils {
    /**
     * Creates a projectile explosion for the specified entity.
     *
     * @param entity The entity to explode
     */
    public void createExplosion(Entity entity, ExplosionConfig config) {
        var world = entity.level();
        if (world.isClientSide())
            return;

        var source = entity instanceof ProjectileEntity projectileEntity ? entity.damageSources().explosion(entity, projectileEntity.getShooter()) : null;
        var mode = config.isDestroyBlocks() && Config.COMMON.gameplay.griefing.enableBlockRemovalOnExplosions.get() ?
                Explosion.BlockInteraction.DESTROY :
                Explosion.BlockInteraction.KEEP;

        var explosion = new ProjectileExplosion(world,
                entity, source, null,
                config, entity.position(),
                config.getRadius(), config.isCauseFire(), mode);

        if (net.minecraftforge.event.ForgeEventFactory.onExplosionStart(world, explosion))
            return;

        // Do explosion logic
        explosion.explode();
        explosion.finalizeExplosion(true);

        // Send event to blocks that are exploded (none if mode is none)
        explosion.getToBlow().forEach(pos ->
        {
            if (world.getBlockState(pos).getBlock() instanceof IExplosionDamageable) {
                ((IExplosionDamageable) world.getBlockState(pos).getBlock()).onProjectileExploded(world, world.getBlockState(pos), pos, entity);
            }
        });

        // Clears the affected blocks if mode is none
        if (!explosion.interactsWithBlocks()) {
            explosion.clearToBlow();
        }

        for (ServerPlayer player : ((ServerLevel) world).players()) {
            if (player.distanceToSqr(entity.getX(), entity.getY(), entity.getZ()) < 4096) {
                player.connection.send(new ClientboundExplodePacket(entity.getX(), entity.getY(), entity.getZ(), config.getRadius(), explosion.getToBlow(), explosion.getHitPlayers().get(player)));
            }
        }
    }

    public static void createCustomExplosion(
            Level level,
            Vec3 pos,
            float radius,
            float damage,
            boolean damageDecreaseWithDistance,
            boolean canBreakBlocks,
            @Nullable Entity sourceEntity
    ) {
        var x = pos.x;
        var y = pos.y;
        var z = pos.z;

        var explosionArea = new AABB(
                x - radius, y - radius, z - radius,
                x + radius, y + radius, z + radius
        );

        var entities = level.getEntitiesOfClass(Entity.class, explosionArea);
        for (var entity : entities) {
            double distanceSqr = entity.distanceToSqr(x, y, z);
            if (distanceSqr < radius * radius) {
                double distance = Math.sqrt(distanceSqr);
                float calculatedDamage = damage;

                if (damageDecreaseWithDistance) {
                    calculatedDamage *= (1 - (float) (distance / radius));
                }

                entity.hurt(level.damageSources().explosion(null), calculatedDamage);
            }
        }

        if (canBreakBlocks && Config.COMMON.gameplay.griefing.enableBlockRemovalOnExplosions.get()) {
            Explosion explosion = new Explosion(
                    level,
                    sourceEntity,
                    null,
                    null,
                    x, y, z,
                    radius,
                    false,
                    Explosion.BlockInteraction.DESTROY
            );

            if (!ForgeEventFactory.onExplosionStart(level, explosion)) {
                explosion.explode();
                explosion.finalizeExplosion(true);
            }
        } else {
            playExplosionEffects(level, x, y, z);
        }
    }

    private static void playExplosionEffects(Level level, double x, double y, double z) {
        if (level instanceof ServerLevel serverLevel) {
            serverLevel.playSound(
                    null,
                    x, y, z,
                    SoundEvents.GENERIC_EXPLODE,
                    SoundSource.BLOCKS,
                    4.0F,
                    (1.0F + (level.random.nextFloat() - level.random.nextFloat()) * 0.2F) * 0.7F
            );
        }

        if (level.isClientSide) {
            level.addParticle(ParticleTypes.EXPLOSION, x, y, z, 1.0D, 0.0D, 0.0D);
        }

        RandomSource rand = level.random;
        for (int i = 0; i < 8; i++) {
            level.addParticle(
                    ParticleTypes.SMOKE,
                    x, y, z,
                    rand.nextGaussian() * 0.05,
                    rand.nextGaussian() * 0.05,
                    rand.nextGaussian() * 0.05
            );
        }
    }
}
