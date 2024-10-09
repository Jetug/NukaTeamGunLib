package com.nukateam.ntgl.common.foundation.entity;


import com.nukateam.ntgl.ClientProxy;
import com.nukateam.ntgl.common.foundation.entity.projectile.DeathEffect;
import com.nukateam.ntgl.common.foundation.init.Projectiles;
import dev.kosmx.playerAnim.core.util.Vec3d;
import net.minecraft.client.Minecraft;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.projectile.Arrow;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import static com.nukateam.ntgl.ClientProxy.getEntityBlockPos;
import static net.minecraft.network.syncher.SynchedEntityData.defineId;
import static net.minecraft.tags.FluidTags.LAVA;

public class FlyingGibs extends Entity {
    public static final EntityDataAccessor<Integer> ENTITY = defineId(FlyingGibs.class, EntityDataSerializers.INT);
    public static final EntityDataAccessor<Integer> PART = defineId(FlyingGibs.class, EntityDataSerializers.INT);
    public static final EntityDataAccessor<Float> SIZE = defineId(FlyingGibs.class, EntityDataSerializers.FLOAT);
    public static final int LIFE = 7500;

    private RandomSource rand;
    public int maxTimeToLive = 2000;
    public int timeToLive = 2000;
    public double gravity = 0.029999999329447746D;

    private double xo = 0;
    private double yo = 0;
    private double zo = 0;

    public Vec3 rotationAxis;
    public int hitGroundTTL = 0;

    //the exploding entity
    public float size;

    public DeathEffect.GoreData data;

//    public TGParticleSystem trail_system;

    //public EntityDT entityDT;

    public FlyingGibs(EntityType<FlyingGibs> type, Level level) {
        super(type, level);
        this.rand = this.level().getRandom();
        this.rotationAxis = new Vec3(rand.nextDouble(), rand.nextDouble(), rand.nextDouble());
    }

    public FlyingGibs(Level world, LivingEntity entity, DeathEffect.GoreData data, Vec3 pos, Vec3 delta, float size, int bodyPart) {
        this(Projectiles.FLYING_GIBS.get(), world);
        this.setPos(pos.x, pos.y, pos.z);
//        this.setDeltaMovement(xo, yo, zo);
        this.rand = this.level().getRandom();

        setDeltaMovement(delta);

        this.xo = delta.x;
        this.yo = delta.y;
        this.zo = delta.z;

        this.size = size;
        this.maxTimeToLive = LIFE + rand.nextInt(50);
        this.timeToLive = maxTimeToLive;
        //this.entityDT = entityDT;
        this.rotationAxis = new Vec3(rand.nextDouble(), rand.nextDouble(), rand.nextDouble());
        this.data = data;

        setRot(entity.getYRot(), getXRot());

        var entityId = entity.getId();

        getEntityData().set(ENTITY, entityId);
        getEntityData().set(PART, bodyPart);
        getEntityData().set(SIZE, size);

//        trail_system = new TGParticleSystem(this, data.type_trail);
//        ClientProxy.particleManager.addEffect(trail_system);
    }

    @Override
    protected void defineSynchedData() {
        entityData.define(ENTITY, -1);
        entityData.define(PART, 0);
        entityData.define(SIZE, 1f);
    }

    @Override
    public void tick() {
        super.tick();
        if(!level().isClientSide) return;
        var rand = this.level().getRandom();

        if (this.timeToLive > 0)
            --timeToLive;
        else this.kill();

//        this.xOld = this.getX();
//        this.yOld = this.getY();
//        this.zOld = this.getZ();

        this.yo -= gravity;

        if (this.level().getBlockState(ClientProxy.getEntityBlockPos(this)).getFluidState().is(LAVA)) {
            this.xo = 0.20000000298023224D;
            this.yo = (rand.nextFloat() - rand.nextFloat()) * 0.2F;
            this.zo = (rand.nextFloat() - rand.nextFloat()) * 0.2F;

            this.playSound(SoundEvents.GENERIC_BURN, 0.4F, 2.0F + rand.nextFloat() * 0.4F);
        }


        this.move(MoverType.SELF, getDeltaMovement());
        float f = 0.98F;

        if (this.onGround()) {
            //System.out.println("onGround.");
            if (hitGroundTTL == 0) {
                hitGroundTTL = timeToLive;
//                trail_system.setExpired();
            }

            f = (float) (this.level().getBlockState(getEntityBlockPos(this))
                    .getBlock().getFriction() * 0.98);

        }

        this.xo *= f;
        this.yo *= 0.9800000190734863D;
        this.zo *= f;

        if (this.onGround()) {
            this.yo *= -0.8999999761581421D;
        }

        var motionScale = this.isInWater() ? this.getWaterInertia() : 1f;

        this.setDeltaMovement(new Vec3(this.xo, this.yo, this.zo).scale(motionScale));
    }

//
//
//    /**
//     * Returns if this entity is in water and will end up adding the waters velocity to the entity
//     */
//    public boolean handleWaterMovement() {
//        return this.level().handleMaterialAcceleration(this.getEntityBoundingBox(), Material.WATER, this);
//        ArrowRenderer
//    }

    protected float getWaterInertia() {
        return 0.6F;
    }

    public int getEntityId() {
        return getEntityData().get(ENTITY);
    }

    public int getPartId() {
        return getEntityData().get(PART);
    }

    public float getSize() {
        return getEntityData().get(SIZE);
    }

    public LivingEntity getLocalEntity(){
        return (LivingEntity)Minecraft.getInstance().level.getEntity(getEntityId());
    }

    @Override
    public boolean shouldRender(double pX, double pY, double pZ) {
        return true;
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag pCompound) {

    }

    @Override
    protected void addAdditionalSaveData(CompoundTag pCompound) {

    }
}
