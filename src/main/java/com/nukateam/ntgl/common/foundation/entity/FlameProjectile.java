package com.nukateam.ntgl.common.foundation.entity;

import com.nukateam.ntgl.client.helpers.MuzzleMatrixHelper;
import com.nukateam.ntgl.common.data.WeaponData;
import com.nukateam.ntgl.common.util.util.math.ExtendedEntityRayTraceResult;
import net.minecraft.client.Minecraft;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseFireBlock;
import net.minecraft.world.level.block.CampfireBlock;
import net.minecraft.world.level.block.CandleBlock;
import net.minecraft.world.level.block.CandleCakeBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;

import java.util.function.Predicate;

public class FlameProjectile extends ProjectileEntity {
    private static final float GROUND_FIRE_CHANCE = 0.4f;
    private static final float ENTITY_FIRE_CHANCE = 1.0f;

    public FlameProjectile(EntityType<? extends ProjectileEntity> entityType, Level worldIn) {
        super(entityType, worldIn);
    }

    public FlameProjectile(EntityType<? extends ProjectileEntity> entityType, Level worldIn, WeaponData data) {
        super(entityType, worldIn, data);
    }

    @Override
    public void tick() {
        super.tick();
    }

    @Override
    protected void onProjectileTick() {
        if (this.level().isClientSide) {
            int shooterId = this.getOwnerId();

            double startX = this.xOld;
            double startY = this.yOld;
            double startZ = this.zOld;

            double offsetX = 0;
            double offsetY = 0;
            double offsetZ = 0;

            if (this.tickCount < 10) {
                Vec3 muzzleWorldPos = MuzzleMatrixHelper.getMuzzleWorldPosForEntity(shooterId, Minecraft.getInstance().getFrameTime());

                if (muzzleWorldPos != null) {
                    double blend = 1.0 - ((double) this.tickCount / 10.0);
                    if (blend < 0) blend = 0;

                    startX = this.xOld + (muzzleWorldPos.x() - this.xOld) * blend;
                    startY = this.yOld + (muzzleWorldPos.y() - this.yOld) * blend;
                    startZ = this.zOld + (muzzleWorldPos.z() - this.zOld) * blend;

                    double endBlend = 1.0 - ((double) (this.tickCount + 1) / 10.0);
                    if (endBlend < 0) endBlend = 0;
                    offsetX = (muzzleWorldPos.x() - this.getX()) * endBlend;
                    offsetY = (muzzleWorldPos.y() - this.getY()) * endBlend;
                    offsetZ = (muzzleWorldPos.z() - this.getZ()) * endBlend;
                }
            }

            double endX = this.getX() + offsetX;
            double endY = this.getY() + offsetY;
            double endZ = this.getZ() + offsetZ;

            for (int i = 1; i <= 5; ++i) {
                double fraction = (double) i / 5.0;
                double px = startX + (endX - startX) * fraction;
                double py = startY + (endY - startY) * fraction;
                double pz = startZ + (endZ - startZ) * fraction;
                this.level().addParticle(ParticleTypes.FLAME, true, px, py, pz, 0.0, 0.0, 0.0);
            }
            if (this.level().random.nextInt(2) == 0) {
                this.level().addParticle(ParticleTypes.SMOKE, true, endX, endY, endZ, 0.0, 0.0, 0.0);
                this.level().addParticle(ParticleTypes.FLAME, true, endX, endY, endZ, 0.0, 0.0, 0.0);
            }
        }
    }

    @Override
    protected Predicate<BlockState> getBlockFilter() {
        return (value) -> false;
    }

    @Override
    protected boolean removeOnHit(HitTarget hitTarget) {
        return true;
    }

    @Override
    protected void onHitBlock(BlockHitResult hitResult, BlockState blockState) {
        var blockPos = hitResult.getBlockPos();
        var face = hitResult.getDirection();
        if(random.nextFloat() <= getBlockFireChance()) {
            if (!CampfireBlock.canLight(blockState) && !CandleBlock.canLight(blockState) && !CandleCakeBlock.canLight(blockState)) {
                var relative = blockPos.relative(face);

                if (BaseFireBlock.canBePlacedAt(level(), relative, face)) {
                    var blockstate1 = BaseFireBlock.getState(level(), relative);
                    level().setBlock(relative, blockstate1, 11);

                }
            } else {
                level().setBlock(blockPos, blockState.setValue(BlockStateProperties.LIT, Boolean.valueOf(true)), 11);
            }
        }

        super.onHitBlock(hitResult, blockState);
    }

    protected float getBlockFireChance(){
        return GROUND_FIRE_CHANCE;
    }

    protected float getEntityFireChance(){
        return ENTITY_FIRE_CHANCE;
    }
}
