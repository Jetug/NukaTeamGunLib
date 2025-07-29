package com.nukateam.ntgl.common.util.world;

import com.google.common.collect.Sets;
import com.mojang.datafixers.util.Pair;
import com.nukateam.ntgl.common.data.config.ExplosionConfig;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.item.PrimedTnt;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.ProtectionEnchantment;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.ExplosionDamageCalculator;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseFireBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.ForgeEventFactory;

import javax.annotation.Nullable;
import java.util.List;

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
    private final boolean causesFire;
    private final RandomSource random = RandomSource.create();
    private final BlockInteraction blockInteraction;

    public ProjectileExplosion(Level world, Entity exploder,
                               @Nullable DamageSource source,
                               @Nullable ExplosionDamageCalculator context, ExplosionConfig projectile,
                               Vec3 pos, float radius, boolean causesFire, BlockInteraction mode) {
        super(world, exploder, source, context, pos.x, pos.y, pos.z, radius, causesFire, mode);
        this.world = world;
        this.causesFire = causesFire;
        this.blockInteraction = mode;
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

    public ProjectileExplosion(Level world, Entity exploder, ExplosionConfig projectile,
                               Vec3 pos, BlockInteraction mode, List<BlockPos> toBlow) {
        this(world, exploder, null, null, projectile, pos, projectile.getRadius(), projectile.isCauseFire(), mode);
        this.getToBlow().addAll(toBlow);
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

        var entities = this.world.getEntities(null, new AABB(minX, minY, minZ, maxX, maxY, maxZ));

//        ForgeEventFactory.onExplosionDetonate(this.world, this, entities, diameter);

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
            var knockback = (1.0D - strength) * blockDensity * this.knockback;
//            float finalDamage = (int)((knockback * knockback + knockback) / 2.0D * 7.0D * diameter + 1.0D);
            float finalDamage = this.damage;

            if(this.damageDecreaseWithDistance){
                finalDamage *= 1.0D - strength;
            }

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

    @Override
    public void finalizeExplosion(boolean pSpawnParticles) {
        if (this.world.isClientSide) {
            this.world.playLocalSound(this.x, this.y, this.z,
                    SoundEvents.GENERIC_EXPLODE, SoundSource.BLOCKS, 4.0F,
                    (1.0F + (this.world.random.nextFloat() - this.world.random.nextFloat()) * 0.2F) * 0.7F,
                    false);
        }

        var interactsWithBlocks = this.interactsWithBlocks();
        var toBlow = (ObjectArrayList<BlockPos>)getToBlow();

        if (pSpawnParticles) {
            if (!(this.radius < 2.0F) && interactsWithBlocks) {
                this.world.addParticle(ParticleTypes.EXPLOSION_EMITTER, this.x, this.y, this.z, 1.0D, 0.0D, 0.0D);
            } else {
                this.world.addParticle(ParticleTypes.EXPLOSION, this.x, this.y, this.z, 1.0D, 0.0D, 0.0D);
            }
        }

        if (interactsWithBlocks) {
            var blockDrops = new ObjectArrayList<Pair<ItemStack, BlockPos>>();
            var isPlayer = this.getIndirectSourceEntity() instanceof Player;
            Util.shuffle(toBlow, this.world.random);

            for(BlockPos blockpos : toBlow) {
                var blockState = this.world.getBlockState(blockpos);

                if (!blockState.isAir()) {
                    var immutableBLockPos = blockpos.immutable();
                    this.world.getProfiler().push("explosion_blocks");
                    if (blockState.canDropFromExplosion(this.world, blockpos, this)) {
                        if (this.world instanceof ServerLevel serverLevel) {
                            var blockEntity = blockState.hasBlockEntity() ? this.world.getBlockEntity(blockpos) : null;
                            var builder = (new LootParams.Builder(serverLevel))
                                    .withParameter(LootContextParams.ORIGIN, Vec3.atCenterOf(blockpos))
                                    .withParameter(LootContextParams.TOOL, ItemStack.EMPTY)
                                    .withOptionalParameter(LootContextParams.BLOCK_ENTITY, blockEntity)
                                    .withOptionalParameter(LootContextParams.THIS_ENTITY, this.exploder);

                            if (this.blockInteraction == Explosion.BlockInteraction.DESTROY_WITH_DECAY) {
                                builder.withParameter(LootContextParams.EXPLOSION_RADIUS, this.radius);
                            }

                            blockState.spawnAfterBreak(serverLevel, blockpos, ItemStack.EMPTY, isPlayer);
                            blockState.getDrops(builder).forEach((p_46074_) -> {
                                addBlockDrops(blockDrops, p_46074_, immutableBLockPos);
                            });
                        }
                    }

                    blockState.onBlockExploded(this.world, blockpos, this);
                    this.world.getProfiler().pop();
                }
            }

            for(Pair<ItemStack, BlockPos> pair : blockDrops) {
                Block.popResource(this.world, pair.getSecond(), pair.getFirst());
            }
        }

        if (this.causesFire) {
            for(BlockPos blockpos2 : toBlow) {
                if (this.random.nextInt(3) == 0 && this.world.getBlockState(blockpos2).isAir() && this.world.getBlockState(blockpos2.below()).isSolidRender(this.world, blockpos2.below())) {
                    this.world.setBlockAndUpdate(blockpos2, BaseFireBlock.getState(this.world, blockpos2));
                }
            }
        }

    }

    private static void addBlockDrops(ObjectArrayList<Pair<ItemStack, BlockPos>> pDropPositionArray,
                                      ItemStack pStack, BlockPos pPos) {
        int i = pDropPositionArray.size();

        for(int j = 0; j < i; ++j) {
            Pair<ItemStack, BlockPos> pair = pDropPositionArray.get(j);
            ItemStack itemstack = pair.getFirst();
            if (ItemEntity.areMergable(itemstack, pStack)) {
                ItemStack itemstack1 = ItemEntity.merge(itemstack, pStack, 16);
                pDropPositionArray.set(j, Pair.of(itemstack1, pair.getSecond()));
                if (pStack.isEmpty()) {
                    return;
                }
            }
        }

        pDropPositionArray.add(Pair.of(pStack, pPos));
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
