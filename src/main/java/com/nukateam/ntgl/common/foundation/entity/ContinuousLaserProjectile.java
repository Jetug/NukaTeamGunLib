package com.nukateam.ntgl.common.foundation.entity;

import com.nukateam.ntgl.common.base.gun.Gun;
import com.nukateam.ntgl.common.data.util.math.ExtendedEntityRayTraceResult;
import com.nukateam.ntgl.common.foundation.item.GunItem;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseFireBlock;
import net.minecraft.world.level.block.CampfireBlock;
import net.minecraft.world.level.block.CandleBlock;
import net.minecraft.world.level.block.CandleCakeBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

public class ContinuousLaserProjectile extends LaserProjectile {
    private static final float GROUND_FIRE_CHANCE = 0.01f;
    private static final float ENTITY_FIRE_CHANCE = 0.1f;

    public ContinuousLaserProjectile(EntityType<? extends ProjectileEntity> entityType, Level worldIn) {
        super(entityType, worldIn);
    }

    public ContinuousLaserProjectile(EntityType<? extends ProjectileEntity> entityType, Level worldIn, LivingEntity shooter, ItemStack weapon, GunItem item, Gun modifiedGun) {
        super(entityType, worldIn, shooter, weapon, item, modifiedGun);
    }

    public void trace() {
        if (shooter == null)
            return;

        setupDirection(shooter, weapon, (GunItem)weapon.getItem(), modifiedGun);

        var startVec = new Vec3(this.getX(), this.getY(), this.getZ());
        var endVec = startVec.add(this.getDeltaMovement());

        HitResult raytraceresult = rayTraceBlocks(this.level(), new ClipContext(startVec, endVec, ClipContext.Block.COLLIDER,
                ClipContext.Fluid.NONE, this), IGNORE_LEAVES);

        if (raytraceresult.getType() != HitResult.Type.MISS) {
            endVec = raytraceresult.getLocation();
        }


        var entityResult = this.findEntityOnPath(shooter, startVec, endVec);

        if (entityResult != null) {
            raytraceresult = new ExtendedEntityRayTraceResult(entityResult);
            if (((EntityHitResult)raytraceresult).getEntity() instanceof Player player) {
                if (this.shooter instanceof Player && !((Player) this.shooter).canHarmPlayer(player)) {
                    raytraceresult = null;
                }
            }
        }

        if (raytraceresult != null) {
            this.onHit(raytraceresult, startVec, endVec);
            var hitVec = raytraceresult.getLocation();
            distance = (float) startVec.distanceTo(hitVec);
        }

        laserPitch = this.getXRot();
        laserYaw = this.getYRot();
        if (distance <= 0) {
            distance = (float) this.projectile.getSpeed();
        }

        this.startVec  	= startVec;
        this.endVec  	= endVec  ;

        this.entityData.set(START_X, (float)startVec.x);
        this.entityData.set(START_Y, (float)startVec.y);
        this.entityData.set(START_Z, (float)startVec.z);

        this.entityData.set(END_X  , (float)endVec.x);
        this.entityData.set(END_Y  , (float)endVec.y);
        this.entityData.set(END_Z  , (float)endVec.z);

        this.entityData.set(DISTANCE  , distance);
    }

    @Override
    public void tick() {
        super.tick();
        if (!isRemoved() && !this.level().isClientSide()) {
            trace();
        }
    }

    public float getBlockFireChance(){
        return GROUND_FIRE_CHANCE;
    }

    public float getEntityFireChance(){
        return ENTITY_FIRE_CHANCE;
    }
}