package com.nukateam.ntgl.common.util.world;

import com.google.common.collect.Sets;
import com.nukateam.ntgl.common.data.config.ExplosionConfig;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.PrimedTnt;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.enchantment.ProtectionEnchantment;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.ExplosionDamageCalculator;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.ForgeEventFactory;

import javax.annotation.Nullable;

/**
 * Author: MrCrayfish
 */
public class ProjectileExplosion extends Explosion {
    private static final ExplosionDamageCalculator DEFAULT_CONTEXT = new ExplosionDamageCalculator();

    private final Level world;
    private final double x;
    private final double y;
    private final double z;
    private final float radius;
    private final Entity exploder;
    private final ExplosionDamageCalculator context;
    private final float damage;
    private final float knockback;
    private final boolean damageDecreaseWithDistance;

    public ProjectileExplosion(Level world, Entity exploder,
                               @Nullable DamageSource source,
                               @Nullable ExplosionDamageCalculator context, ExplosionConfig projectile,
                               Vec3 pos, float radius, boolean causesFire, BlockInteraction mode) {
        super(world, exploder, source, context, pos.x, pos.y, pos.z, radius, causesFire, mode);
        this.world = world;
        this.x = pos.x;
        this.y = pos.y;
        this.z = pos.z;
        this.radius = radius;
        this.exploder = exploder;
        this.context = context == null ? DEFAULT_CONTEXT : context;
        this.damage = projectile.getDamage();
        this.knockback = projectile.getKnockback();
        this.damageDecreaseWithDistance = projectile.isDamageReduceOverDistance();
    }

    @Override
    public void explode() {
        destroyBlocks();

        var diameter = this.radius * 2.0D;
        int minX = Mth.floor(this.x - diameter - 1.0D);
        int maxX = Mth.floor(this.x + diameter + 1.0D);
        int minY = Mth.floor(this.y - diameter - 1.0D);
        int maxY = Mth.floor(this.y + diameter + 1.0D);
        int minZ = Mth.floor(this.z - diameter - 1.0D);
        int maxZ = Mth.floor(this.z + diameter + 1.0D);

        var entities = this.world.getEntities(this.exploder, new AABB(minX, minY, minZ, maxX, maxY, maxZ));

        ForgeEventFactory.onExplosionDetonate(this.world, this, entities, diameter);

        var explosionPos = new Vec3(this.x, this.y, this.z);
        for (var entity : entities) {
            if (entity.ignoreExplosion())
                continue;

            var strength = Math.sqrt(entity.distanceToSqr(explosionPos)) / diameter;
            if (strength > 1.0D)
                continue;

            var deltaX = entity.getX() - this.x;
            var deltaY = (entity instanceof PrimedTnt ? entity.getY() : entity.getEyeY()) - this.y;
            var deltaZ = entity.getZ() - this.z;
            var distanceToExplosion = Math.sqrt(deltaX * deltaX + deltaY * deltaY + deltaZ * deltaZ);

            if (distanceToExplosion != 0.0D) {
                deltaX /= distanceToExplosion;
                deltaY /= distanceToExplosion;
                deltaZ /= distanceToExplosion;
            } else {
                deltaX = 0.0;
                deltaY = 1.0;
                deltaZ = 0.0;
            }

            var blockDensity = (double) getSeenPercent(explosionPos, entity);
            var knockback = (1.0D - strength) * blockDensity;
            float finalDamage = (int)((knockback * knockback + knockback) / 2.0D * 7.0D * diameter + 1.0D);

            entity.hurt(this.getDamageSource(), finalDamage);

            if (entity instanceof LivingEntity)
                knockback = ProtectionEnchantment.getExplosionKnockbackAfterDampener((LivingEntity) entity, knockback);

            entity.setDeltaMovement(entity.getDeltaMovement().add(deltaX * knockback, deltaY * knockback, deltaZ * knockback));

            if (entity instanceof Player player) {
                if (!player.isSpectator() && (!player.isCreative() || !player.getAbilities().flying)) {
                    this.getHitPlayers().put(player, new Vec3(deltaX * knockback, deltaY * knockback, deltaZ * knockback));
                }
            }
        }
    }

    private void destroyBlocks() {
        var set = Sets.<BlockPos>newHashSet();

        for (int x = 0; x < 16; x++) {
            for (int y = 0; y < 16; y++) {
                for (int z = 0; z < 16; z++) {
                    if (x == 0 || x == 15 || y == 0 || y == 15 || z == 0 || z == 15) {
                        var d0 = (double) ((float) x / 15.0F * 2.0F - 1.0F);
                        var d1 = (double) ((float) y / 15.0F * 2.0F - 1.0F);
                        var d2 = (double) ((float) z / 15.0F * 2.0F - 1.0F);
                        var d3 = Math.sqrt(d0 * d0 + d1 * d1 + d2 * d2);
                        d0 = d0 / d3;
                        d1 = d1 / d3;
                        d2 = d2 / d3;
                        var f = this.radius * (0.7F + this.world.random.nextFloat() * 0.6F);
                        var blockX = this.x;
                        var blockY = this.y;
                        var blockZ = this.z;

                        for (; f > 0.0F; f -= 0.225F) {
                            var pos = BlockPos.containing(blockX, blockY, blockZ);
                            var blockState = this.world.getBlockState(pos);
                            var fluidState = this.world.getFluidState(pos);
                            var optional = this.context.getBlockExplosionResistance(this, this.world, pos, blockState, fluidState);

                            if (optional.isPresent()) {
                                f -= (optional.get() + 0.3F) * 0.3F;
                            }

                            if (f > 0.0F && this.context.shouldBlockExplode(this, this.world, pos, blockState, f)) {
                                set.add(pos);
                            }

                            blockX += d0 * (double) 0.3F;
                            blockY += d1 * (double) 0.3F;
                            blockZ += d2 * (double) 0.3F;
                        }
                    }
                }
            }
        }

        this.getToBlow().addAll(set);
    }
}
