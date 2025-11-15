package com.nukateam.ntgl.common.foundation.entity;

import com.nukateam.ntgl.common.data.WeaponData;
import com.nukateam.ntgl.common.util.util.math.ExtendedEntityRayTraceResult;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
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

public class LaserProjectile extends AbstractBeamProjectile {
    private static final float GROUND_FIRE_CHANCE = 0.1f;
    private static final float ENTITY_FIRE_CHANCE = 0.1f;

    public LaserProjectile(EntityType<? extends ProjectileEntity> entityType, Level worldIn) {
        super(entityType, worldIn);
    }

    public LaserProjectile(EntityType<? extends ProjectileEntity> entityType, Level worldIn,  WeaponData data) {
        super(entityType, worldIn, data);
        trace();
    }

    public float getBlockFireChance(){
        return GROUND_FIRE_CHANCE;
    }

    public float getEntityFireChance(){
        return ENTITY_FIRE_CHANCE;
    }

    @Override
    protected void burnEntity(Entity entity) {
        var burnTime = projectile.getBurnSeconds();
        if (burnTime > 0 && random.nextFloat() <= getEntityFireChance()) {
            entity.igniteForSeconds(burnTime);
        }
    }

    @Override
    protected void onHitBlock(BlockHitResult hitResult, BlockState blockState) {
        var blockPos = hitResult.getBlockPos();
        var face = hitResult.getDirection();

        if(random.nextFloat() <= getBlockFireChance()) {
            if (!CampfireBlock.canLight(blockState) && !CandleBlock.canLight(blockState) && !CandleCakeBlock.canLight(blockState)) {
                var relativeBlockPos = blockPos.relative(face);

                if (BaseFireBlock.canBePlacedAt(level(), relativeBlockPos, face)) {
                    var fireBlockState = BaseFireBlock.getState(level(), relativeBlockPos);
                    level().setBlock(relativeBlockPos, fireBlockState, 11);

                }
            } else {
                level().setBlock(blockPos, blockState.setValue(BlockStateProperties.LIT, Boolean.valueOf(true)), 11);
            }
        }

        super.onHitBlock(hitResult, blockState);
    }
}