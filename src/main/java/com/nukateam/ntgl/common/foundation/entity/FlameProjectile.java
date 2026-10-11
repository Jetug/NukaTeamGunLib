package com.nukateam.ntgl.common.foundation.entity;

import com.nukateam.ntgl.client.helpers.MuzzleMatrixHelper;
import com.nukateam.ntgl.common.data.WeaponData;
import com.nukateam.ntgl.common.util.util.WeaponModifierHelper;
import com.nukateam.ntgl.common.util.util.math.ExtendedEntityRayTraceResult;
import net.minecraft.client.Minecraft;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseFireBlock;
import net.minecraft.world.level.block.CampfireBlock;
import net.minecraft.world.level.block.CandleBlock;
import net.minecraft.world.level.block.CandleCakeBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

import java.util.function.Predicate;

public class FlameProjectile extends ProjectileEntity {
    private static final float GROUND_FIRE_CHANCE = 0.4f;
    private static final float ENTITY_FIRE_CHANCE = 1.0f;
    private static final double FLAME_FLOW_SPEED = 0.8;
    private static final double FLAME_FLOW_TAPER = 0.6;
    private static final double FLAME_CONE_BASE = 0.08;
    private static final double FLAME_CONE_GROWTH = 0.5;
    private static final double FLAME_OUTWARD_BASE = 0.03;
    private static final double FLAME_OUTWARD_GROWTH = 0.1;
    private static final double FLAME_BLOB_FLOW = 0.3;

    public FlameProjectile(EntityType<? extends ProjectileEntity> entityType, Level worldIn) {
        super(entityType, worldIn);
    }

    public FlameProjectile(EntityType<? extends ProjectileEntity> entityType, Level worldIn, WeaponData data) {
        super(entityType, worldIn, data);
        this.modifiedGravity = WeaponModifierHelper.getProjectileGravity(data, -0.01);
    }

    @Override
    public void tick() {
        super.tick();
    }

    @Override
    protected void onProjectileTick() {
        if (!this.level().isClientSide) return;
        if (!(this.level().getEntity(this.getOwnerId()) instanceof LivingEntity living)) return;

        var eye = living.getEyePosition();

        if (this.tickCount == 1) {
            var view = living.getViewVector(1.0F);
            var muzzleWorldPos = MuzzleMatrixHelper.getMuzzleWorldPosForEntity(this.getOwnerId(), Minecraft.getInstance().getTimer().getRealtimeDeltaTicks());
            var start = muzzleWorldPos != null ? muzzleWorldPos : eye.add(view.scale(2.1));

            var range = this.getProjectile().getSpeed() * this.getProjectile().getLife();
            var end = hitClampedEnd(start, eye.add(view.scale(range)), living);

            var flow = end.subtract(start);
            addFlameStream(start, end, flow.lengthSqr() < 1.0E-4 ? view : flow.normalize());
        }

        var shotDir = this.position().subtract(eye);
        if (shotDir.lengthSqr() > 1.0E-4) {
            addFlameBlob(this.position(), shotDir.normalize());
        }
    }

    private Vec3 hitClampedEnd(Vec3 start, Vec3 aimEnd, LivingEntity living) {
        var blockHit = this.level().clip(new ClipContext(start, aimEnd, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, this));
        Vec3 end = blockHit.getType() == HitResult.Type.MISS ? aimEnd : blockHit.getLocation();

        var entityHit = ProjectileUtil.getEntityHitResult(this, start, aimEnd, this.getBoundingBox(),
                (entity) -> entity != this && entity != living && !(entity instanceof FlameProjectile), 0.0D);
        if (entityHit != null && start.distanceToSqr(entityHit.getLocation()) < start.distanceToSqr(end)) {
            end = entityHit.getLocation();
        }

        return end;
    }

    private void addFlameStream(Vec3 start, Vec3 end, Vec3 dir) {
        var span = start.distanceTo(end);
        if (span < 0.05) {
            this.level().addParticle(ParticleTypes.FLAME, true, start.x, start.y, start.z, 0.0, 0.0, 0.0);
            return;
        }

        var count = Math.min(9, Math.max(4, (int) Math.round(span / 1.5) + 1));
        var rand = this.level().random;

        var upRef = Math.abs(dir.y) < 0.9 ? new Vec3(0.0, 1.0, 0.0) : new Vec3(1.0, 0.0, 0.0);
        var u = upRef.cross(dir).normalize();
        var v = dir.cross(u).normalize();

        for (int i = 0; i < count; ++i) {
            var fraction = (double) i / (double) (count - 1);

            var radius = FLAME_CONE_BASE + FLAME_CONE_GROWTH * fraction;
            var radial = radius * Math.sqrt(rand.nextDouble());
            var theta = rand.nextDouble() * Math.PI * 2.0;
            var c = Math.cos(theta);
            var s = Math.sin(theta);
            var outX = u.x * c + v.x * s;
            var outY = u.y * c + v.y * s;
            var outZ = u.z * c + v.z * s;

            var taper = Math.max(0.0, 1.0 - FLAME_FLOW_TAPER * fraction);
            var flow = FLAME_FLOW_SPEED * taper;
            var outward = FLAME_OUTWARD_BASE + FLAME_OUTWARD_GROWTH * fraction;

            this.level().addParticle(ParticleTypes.FLAME, true,
                    start.x + (end.x - start.x) * fraction + outX * radial,
                    start.y + (end.y - start.y) * fraction + outY * radial,
                    start.z + (end.z - start.z) * fraction + outZ * radial,
                    dir.x * flow + outX * outward,
                    dir.y * flow + outY * outward,
                    dir.z * flow + outZ * outward);
        }
        this.level().addParticle(ParticleTypes.FLAME, true, end.x, end.y, end.z, 0.0, 0.0, 0.0);
    }

    private void addFlameBlob(Vec3 pos, Vec3 dir) {
        var vx = dir.x * FLAME_BLOB_FLOW;
        var vy = dir.y * FLAME_BLOB_FLOW;
        var vz = dir.z * FLAME_BLOB_FLOW;
        var rand = this.level().random;
        for (int i = 0; i < 3; ++i) {
            this.level().addParticle(ParticleTypes.FLAME, true,
                    pos.x + (rand.nextDouble() - 0.5) * 0.4,
                    pos.y + (rand.nextDouble() - 0.5) * 0.4,
                    pos.z + (rand.nextDouble() - 0.5) * 0.4,
                    vx, vy, vz);
        }
        if (rand.nextInt(2) == 0) {
            this.level().addParticle(ParticleTypes.SMOKE, true, pos.x, pos.y, pos.z, vx, vy, vz);
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
