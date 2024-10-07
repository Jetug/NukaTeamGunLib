package com.nukateam.ntgl.common.foundation.entity;

import com.nukateam.ntgl.common.base.config.Gun;
import com.nukateam.ntgl.common.foundation.item.GunItem;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
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

public class LaserProjectile extends AbstractBeamProjectile {
    private static final float GROUND_FIRE_CHANCE = 0.1f;
    private static final float ENTITY_FIRE_CHANCE = 0.1f;

    public LaserProjectile(EntityType<? extends ProjectileEntity> entityType, Level worldIn) {
        super(entityType, worldIn);
    }

    public LaserProjectile(EntityType<? extends ProjectileEntity> entityType, Level worldIn, LivingEntity shooter, ItemStack weapon, GunItem item, Gun modifiedGun) {
        super(entityType, worldIn, shooter, weapon, item, modifiedGun);
        trace();
    }

    public float getBlockFireChance(){
        return GROUND_FIRE_CHANCE;
    }

    public float getEntityFireChance(){
        return ENTITY_FIRE_CHANCE;
    }

    @Override
    protected void onHitEntity(Entity entity, Vec3 hitVec, Vec3 startVec, Vec3 endVec, boolean headshot) {
        super.onHitEntity(entity, hitVec, startVec, endVec, headshot);
        if(random.nextFloat() <= getEntityFireChance())
            entity.setRemainingFireTicks(20);
    }

    @Override
    protected void onHitBlock(BlockState blockState, BlockPos blockPos, Direction face, double x, double y, double z) {
        super.onHitBlock(blockState, blockPos, face, x, y, z);

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
    }
}