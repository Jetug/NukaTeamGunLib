package com.nukateam.ntgl.common.foundation.entity;

import com.nukateam.ntgl.common.base.gun.Gun;
import com.nukateam.ntgl.common.foundation.item.GunItem;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.Creeper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import static com.nukateam.ntgl.client.render.renderers.projectiles.TeslaProjectileRenderer.getRandomAngle;
import static com.nukateam.ntgl.common.foundation.init.Projectiles.*;

public class TeslaProjectile extends AbstractBeamProjectile {
    public static final int TTL = 10;
    public static final float CHAIN_RANGE = 8.0f;
    public static final int CHAIN_TARGETS = 4;
    public static final float CHAIN_DAMAGE_FACTOR = 0.75f;
    private static final float CREEPER_POWER_CHANCE = 0.3f;
    protected long seed = 0;

    public float angle = getRandomAngle();

    protected int chainTargets = CHAIN_TARGETS;
    protected Entity prevTarget = null;
    protected EntityType<? extends ProjectileEntity> entityType = null;

    public TeslaProjectile(EntityType<? extends ProjectileEntity> entityType, Level worldIn) {
        super(entityType, worldIn);
        this.seed = worldIn.random.nextLong();
        this.entityType = entityType;
    }

    public TeslaProjectile(EntityType<? extends ProjectileEntity> entityType, Level worldIn, LivingEntity shooter,
                           ItemStack weapon, GunItem item, Gun modifiedGun) {
          this(entityType, worldIn, shooter, weapon, item, modifiedGun, CHAIN_TARGETS);
    }

    public TeslaProjectile(EntityType<? extends ProjectileEntity> entityType, Level worldIn, LivingEntity shooter,
                           ItemStack weapon, GunItem item, Gun modifiedGun, int chainTargets) {
        super(entityType, worldIn, shooter, weapon, item, modifiedGun);
        this.chainTargets = chainTargets;
        this.entityType = entityType;
        trace();
    }

    public static final double D2R = Math.PI / 180.0;
    public static final double R2D = 180.0 / Math.PI;

    public TeslaProjectile(EntityType<? extends ProjectileEntity> entityType, Level worldIn,
                           LivingEntity shooter, Entity source, LivingEntity target,
                           ItemStack weapon, GunItem item, Gun modifiedGun, int chainTargets) {

        super(entityType, worldIn, shooter, weapon, item, modifiedGun);

        maxTicks = (short) this.life;
        this.chainTargets = chainTargets;
        this.prevTarget = target;
        //TODO Align Projectile from Source to Target
        this.setPos(source.getX(),
                source.getY() + source.getEyeHeight() * 0.5f,
                source.getZ());

        Vec3 src = position();
        Vec3 tgt = new Vec3(target.getX(), target.getY() + target.getEyeHeight() * 0.5f, target.getZ());
        Vec3 dir = tgt.subtract(src).normalize();

        this.distance = (float) src.distanceTo(tgt);
        this.laserPitch = (float) (Math.asin(-dir.y) * R2D);
        this.laserYaw = (float) (Math.atan2(dir.x, dir.z) * R2D);

        //System.out.printf("pitch : %.3f,  yaw : %.3f,  distance : %.3f\n", laserPitch, laserYaw, distance);

        this.setXRot(laserPitch);
        this.setYRot(laserYaw);

        this.setDeltaMovement(
                dir.x * projectile.getSpeed(),
                dir.y * projectile.getSpeed(),
                dir.z * projectile.getSpeed());

        trace();

        if (distance <= 0) {
            distance = (float)projectile.getSpeed();
        }
    }

    @Override
    protected void handleBlockBreaking(BlockPos pos, BlockState state) {}

    @Override
    protected void onHitEntity(Entity entity, Vec3 hitVec, Vec3 startVec, Vec3 endVec, boolean headshot) {
        super.onHitEntity(entity, hitVec, startVec, endVec, headshot);

        if (!level().isClientSide) {
            if(entity instanceof Creeper creeper)
                powerCreeper(creeper);

            if (this.chainTargets > 0) {
                var nextTarget = findNextTarget(entity);
                if (nextTarget != null) {
                    var projectile = new TeslaProjectile(
                            TESLA_PROJECTILE.get(), level(), this.shooter, entity, nextTarget, weapon, (GunItem) weapon.getItem(),
                            modifiedGun, chainTargets - 1);
                    level().addFreshEntity(projectile);
                }
            }
        }
    }

    public float getCreeperPowerChance(){
        return CREEPER_POWER_CHANCE;
    }

    private void powerCreeper(Creeper creeper) {
        if(random.nextFloat() <= getCreeperPowerChance()){
            var nbt = creeper.serializeNBT();
            nbt.putBoolean("powered", true);
            creeper.deserializeNBT(nbt);
        }
    }

    private LivingEntity findNextTarget(Entity lastTarget) {
        var list = this.level().getEntities(lastTarget, new AABB(
                        lastTarget.getX() - CHAIN_RANGE,
                        lastTarget.getY() - CHAIN_RANGE,
                        lastTarget.getZ() - CHAIN_RANGE,
                        lastTarget.getX() + CHAIN_RANGE,
                        lastTarget.getY() + CHAIN_RANGE,
                        lastTarget.getZ() + CHAIN_RANGE),
                PROJECTILE_TARGETS);

        for (var entity : list) {
            if (entity instanceof LivingEntity livingEntity) {
                double distance = livingEntity.distanceToSqr(
                        lastTarget.getX(),
                        lastTarget.getY() + lastTarget.getEyeHeight() * 0.5f,
                        lastTarget.getZ());
                distance = Math.sqrt(distance);

                if (distance < CHAIN_RANGE && livingEntity.isAlive() && livingEntity != lastTarget) {
                    if (!livingEntity.equals(shooter) && !livingEntity.equals(prevTarget)) {
                        var from = new Vec3(lastTarget.getX(), lastTarget.getY() + lastTarget.getEyeHeight() * 0.5f, lastTarget.getZ());
                        var to = new Vec3(livingEntity.getX(), livingEntity.getY() + livingEntity.getEyeHeight() * 0.5f, livingEntity.getZ());

                        var context = new ClipContext(from, to, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, this);
                        var raytraceResult = rayTraceBlocks(level(), context, IGNORE_LEAVES);
                        ;//this.level.rayTraceBlocks(vec3d1, vec3d2, false, true, false);

//                        if (raytraceResult == null)
                            return livingEntity;
                    }
                }
            }
        }
        return null;
    }

//    @Override
//    protected TGDamageSource getProjectileDamageSource() {
//        TGDamageSource src = TGDamageSource.causeLightningDamage(this, this.shooter, DeathType.LASER);
//        src.armorPenetration = this.penetration;
//        src.setNoKnockback();
//        src.goreChance=0.5f;
//        return src;
//    }
}