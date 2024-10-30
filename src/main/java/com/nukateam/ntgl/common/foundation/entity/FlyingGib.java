package com.nukateam.ntgl.common.foundation.entity;


import com.nukateam.ntgl.ClientProxy;
import com.nukateam.ntgl.common.foundation.init.ModParticleTypes;
import com.nukateam.ntgl.common.foundation.init.Projectiles;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.PowderSnowBlock;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.util.Lazy;

import static com.nukateam.ntgl.common.foundation.entity.projectile.DeathEffect.*;
import static net.minecraft.network.syncher.SynchedEntityData.defineId;
import static net.minecraft.tags.FluidTags.LAVA;

public class FlyingGib extends Entity {
    public static final EntityDataAccessor<Integer> ENTITY = defineId(FlyingGib.class, EntityDataSerializers.INT);
    public static final EntityDataAccessor<Integer> PART = defineId(FlyingGib.class, EntityDataSerializers.INT);
    public static final EntityDataAccessor<Float> SIZE = defineId(FlyingGib.class, EntityDataSerializers.FLOAT);
    public static final EntityDataAccessor<Float> GRAVITY = defineId(FlyingGib.class, EntityDataSerializers.FLOAT);
    public static final EntityDataAccessor<CompoundTag> DATA = defineId(FlyingGib.class, EntityDataSerializers.COMPOUND_TAG);
    public static final int LIFE = 75;
    public static final double BOUNCE = -0.5;
    private final Lazy<LivingEntity> localEntity = Lazy.of(() -> (LivingEntity)Minecraft.getInstance().level.getEntity(getEntityId()));

    private RandomSource rand;
//    private double xDelta = 0;
//    private double yDelta = 0;
//    private double zDelta = 0;

    public int maxTimeToLive = 2000;
    public int timeToLive = 2000;
    public double gravity = 0.029999999329447746D;
    public Vec3 rotationAxis;
    public int hitGroundTTL = 0;
    public float size;
    public GoreData data;

    public FlyingGib(EntityType<FlyingGib> type, Level level) {
        super(type, level);
        this.rand = this.level().getRandom();
        this.rotationAxis = new Vec3(
                rand.nextInt(0, 90),
                rand.nextInt(0, 90),
                rand.nextInt(0, 90));
    }

    public FlyingGib(Level world, LivingEntity entity, GoreData data, Vec3 pos, Vec3 delta,
                     float size, int bodyPart) {
        this(Projectiles.FLYING_GIBS.get(), world);
        this.setPos(pos.x, pos.y, pos.z);
        this.rand = this.level().getRandom();
        this.size = size;
        this.maxTimeToLive = LIFE + rand.nextInt(50);
        this.timeToLive = maxTimeToLive;
        this.rotationAxis = new Vec3(rand.nextDouble(), rand.nextDouble(), rand.nextDouble());
        this.data = data;

        setDeltaMovement(delta);
        setRot(entity.getYRot(), getXRot());

        getEntityData().set(ENTITY, entity.getId());
        getEntityData().set(PART, bodyPart);
        getEntityData().set(SIZE, size);
        getEntityData().set(DATA, data.serializeNBT());
//        this.gravity = data.gravity * (1 + entity.getRandom().nextFloat());
//        this.gravity = data.gravity;
    }

    @Override
    protected void defineSynchedData() {
        entityData.define(ENTITY, -1);
        entityData.define(PART, 0);
        entityData.define(SIZE, 1f);
        entityData.define(GRAVITY, 0f);
        entityData.define(DATA, new CompoundTag());
    }

    @Override
    public void tick() {
        super.tick();

        if (this.timeToLive > 0)
            --timeToLive;
        else this.kill();

        handleLavaMovement();

        this.move(MoverType.SELF, getDeltaMovement());

        if (this.onGround()) {
            if (hitGroundTTL == 0) {
                hitGroundTTL = timeToLive;
            }

//            var pos = getBlockPosBelowThatAffectsMyMovement();
//            var friction = this.level().getBlockState(pos).getFriction(this.level(), pos, this) * 0.98F;
//
//            friction = this.onGround() ? friction * 0.91F : 0.91F;
//            this.setDeltaMovement(this.getDeltaMovement().multiply(friction, 0.98D, friction));

            BlockPos blockpos = this.getBlockPosBelowThatAffectsMyMovement();
            float friction = this.level().getBlockState(this.getBlockPosBelowThatAffectsMyMovement()).getFriction(level(), this.getBlockPosBelowThatAffectsMyMovement(), this);
            float newFriction = this.onGround() ? friction * 0.91F : 0.91F;

            Vec3 vec35 = this.handleRelativeFrictionAndCalculateMovement(getDeltaMovement(), friction);
            double d2 = vec35.y;

            if (this.level().isClientSide && !this.level().hasChunkAt(blockpos)) {
                if (this.getY() > (double)this.level().getMinBuildHeight())
                    d2 = -0.1D;
                else d2 = 0.0D;
            }

            this.setDeltaMovement(vec35.x * (double)newFriction, d2 * (double)0.98F, vec35.z * (double)newFriction);
        }

        var motionScale = this.isInWater() ? this.getWaterInertia() : 1f;

        this.setDeltaMovement(getDeltaMovement().scale(motionScale));
        handleGravity();
        particleTick();
    }

    public Vec3 handleRelativeFrictionAndCalculateMovement(Vec3 pDeltaMovement, float pFriction) {
        this.moveRelative(this.getFrictionInfluencedSpeed(pFriction), pDeltaMovement);
//        this.setDeltaMovement(this.handleOnClimbable(this.getDeltaMovement()));
        this.move(MoverType.SELF, this.getDeltaMovement());
        Vec3 vec3 = this.getDeltaMovement();
        if ((this.horizontalCollision) && (this.getFeetBlockState().is(Blocks.POWDER_SNOW) && PowderSnowBlock.canEntityWalkOnPowderSnow(this))) {
            vec3 = new Vec3(vec3.x, 0.2D, vec3.z);
        }

        return vec3;
    }

    private float getFrictionInfluencedSpeed(float pFriction) {
        return this.onGround() ? 1.5F * (0.21600002F / (pFriction * pFriction * pFriction)) : 0.02F;
    }

    @Override
    public void onRemovedFromWorld() {
        goreStats.remove(getEntityId());
        super.onRemovedFromWorld();
    }

    private void particleTick() {
        if (this.level().isClientSide && getData().showBlood) {
            for (int i = 5; i > 0; i--) {
                this.level().addParticle(ModParticleTypes.BLOOD.get(), true,
                        this.getX() - (this.getDeltaMovement().x() / i),
                        this.getY() - (this.getDeltaMovement().y() / i),
                        this.getZ() - (this.getDeltaMovement().z() / i),
                        0, 0, 0);
            }
        }
    }

    private void handleGravity(){
        this.checkSlowFallDistance();

        var gravity = getData().gravity;
        var vec3 = this.getDeltaMovement();
        var f = this.getXRot() * ((float)Math.PI / 180F);
        var d5 = Math.cos(f);

        vec3 = this.getDeltaMovement().add(0.0D, gravity * (-1.0D + d5 * 0.75D), 0.0D);

        this.setDeltaMovement(vec3.multiply(0.99F, 0.95F, 0.99F));
        this.move(MoverType.SELF, this.getDeltaMovement());
    }

    private void handleLavaMovement() {
        if (this.level().getBlockState(ClientProxy.getEntityBlockPos(this)).getFluidState().is(LAVA)) {
//            this.xDelta = 0.20000000298023224D;
//            this.yDelta = (rand.nextFloat() - rand.nextFloat()) * 0.2F;
//            this.zDelta = (rand.nextFloat() - rand.nextFloat()) * 0.2F;
            var rand = this.level().getRandom();
            this.playSound(SoundEvents.GENERIC_BURN, 0.4F, 2.0F + rand.nextFloat() * 0.4F);
        }
    }

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

    public float getGravity() {
        return getEntityData().get(GRAVITY);
    }

    public GoreData getData() {
        if(data == null) {
            data = new GoreData();
            data.deserializeNBT(getEntityData().get(DATA));
        }
        return data;
    }

    public LivingEntity getLocalEntity(){
        return localEntity.get();
    }

    @Override
    public boolean shouldRender(double pX, double pY, double pZ) {
        return true;
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag pCompound) {}

    @Override
    protected void addAdditionalSaveData(CompoundTag pCompound) {}
}
