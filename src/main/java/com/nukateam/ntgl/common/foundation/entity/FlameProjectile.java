package com.nukateam.ntgl.common.foundation.entity;

import com.nukateam.ntgl.common.base.gun.Ammo;
import com.nukateam.ntgl.common.base.gun.Gun;
import com.nukateam.ntgl.common.foundation.item.GunItem;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseFireBlock;
import net.minecraft.world.level.block.CampfireBlock;
import net.minecraft.world.level.block.CandleBlock;
import net.minecraft.world.level.block.CandleCakeBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.phys.Vec3;

import java.util.function.Predicate;

/**
 * Author: MrCrayfish
 */
public class FlameProjectile extends ProjectileEntity {
    private static final float GROUND_FIRE_CHANCE = 0.4f;
    private static final float ENTITY_FIRE_CHANCE = 1.0f;

    public FlameProjectile(EntityType<? extends ProjectileEntity> entityType, Level worldIn) {
        super(entityType, worldIn);
    }

    public FlameProjectile(EntityType<? extends ProjectileEntity> entityType, Level worldIn, LivingEntity shooter, ItemStack weapon, GunItem item, Gun modifiedGun) {
        super(entityType, worldIn, shooter, weapon, item, modifiedGun);
    }

    public float getBlockFireChance(){
        return GROUND_FIRE_CHANCE;
    }

    public float getEntityFireChance(){
        return ENTITY_FIRE_CHANCE;
    }

    @Override
    protected void onProjectileTick() {
        if (this.level().isClientSide) {
            for (int i = 5; i > 0; i--) {
                this.level().addParticle(ParticleTypes.FLAME, true, this.getX() - (this.getDeltaMovement().x() / i), this.getY() - (this.getDeltaMovement().y() / i), this.getZ() - (this.getDeltaMovement().z() / i), 0, 0, 0);
            }
            if (this.level().random.nextInt(2) == 0) {
                this.level().addParticle(ParticleTypes.SMOKE, true, this.getX(), this.getY(), this.getZ(), 0, 0, 0);
                this.level().addParticle(ParticleTypes.FLAME, true, this.getX(), this.getY(), this.getZ(), 0, 0, 0);
            }
        }
    }

    @Override
    protected Predicate<BlockState> getBlockFilter() {
        return (value) -> false;
    }

    @Override
    protected void onHitEntity(Entity entity, Vec3 hitVec, Vec3 startVec, Vec3 endVec, boolean headshot) {
        super.onHitEntity(entity, hitVec, startVec, endVec, headshot);
        if(random.nextFloat() <= getEntityFireChance())
            entity.setRemainingFireTicks(20);
    }

    @Override
    protected void onHitBlock(BlockState blockstate, BlockPos blockpos, Direction face, double x, double y, double z) {
//        super.onHitBlock(blockstate, blockpos, face, x, y, z);

        if(random.nextFloat() <= getBlockFireChance()) {
            if (!CampfireBlock.canLight(blockstate) && !CandleBlock.canLight(blockstate) && !CandleCakeBlock.canLight(blockstate)) {
                var blockpos1 = blockpos.relative(face);

                if (BaseFireBlock.canBePlacedAt(level(), blockpos1, face)) {
                    var blockstate1 = BaseFireBlock.getState(level(), blockpos1);
                    level().setBlock(blockpos1, blockstate1, 11);

                }
            } else {
                level().setBlock(blockpos, blockstate.setValue(BlockStateProperties.LIT, Boolean.valueOf(true)), 11);
            }
        }
    }

    //
//    @Override
//    public void onExpired() {
//        createExplosion(this, Config.COMMON.missiles.explosionRadius.get().floatValue(), false);
//    }
}
