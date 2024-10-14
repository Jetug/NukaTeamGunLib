package com.nukateam.ntgl.common.foundation.entity;


import com.nukateam.ntgl.ClientProxy;
import com.nukateam.ntgl.common.foundation.entity.projectile.DeathEffect;
import com.nukateam.ntgl.common.foundation.init.ModParticleTypes;
import com.nukateam.ntgl.common.foundation.init.Projectiles;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.IronGolemModel;
import net.minecraft.client.model.VillagerModel;
import net.minecraft.client.renderer.entity.VillagerRenderer;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.animal.Chicken;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.util.Lazy;

import static com.nukateam.ntgl.ClientProxy.getEntityBlockPos;
import static net.minecraft.network.syncher.SynchedEntityData.defineId;
import static net.minecraft.tags.FluidTags.LAVA;

public class FlyingGibs extends Entity {
    public static final EntityDataAccessor<Integer> ENTITY = defineId(FlyingGibs.class, EntityDataSerializers.INT);
    public static final EntityDataAccessor<Integer> PART = defineId(FlyingGibs.class, EntityDataSerializers.INT);
    public static final EntityDataAccessor<Float> SIZE = defineId(FlyingGibs.class, EntityDataSerializers.FLOAT);
    public static final int LIFE = 75;
    public static final double BOUNCE = -0.5;

    private RandomSource rand;
    public int maxTimeToLive = 2000;
    public int timeToLive = 2000;
    public double gravity = 0.029999999329447746D;

    private double xDelta = 0;
    private double yDelta = 0;
    private double zDelta = 0;

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
        this.rand = this.level().getRandom();

        setDeltaMovement(delta);

        this.xDelta = delta.x;
        this.yDelta = delta.y;
        this.zDelta = delta.z;

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
//        if(level().isClientSide) return;
        var rand = this.level().getRandom();

        if (this.timeToLive > 0)
            --timeToLive;
        else this.kill();

        var xDelta = this.xDelta;
        var yDelta = this.yDelta;
        var zDelta = this.zDelta;

//        yDelta -= gravity;

//        addDeltaMovement(new Vec3(0, yDelta - gravity, 0));

        handleLavaMovement(rand);

        this.move(MoverType.SELF, getDeltaMovement());
        float f = 0.98F;

        if (this.onGround()) {
            if (hitGroundTTL == 0) {
                hitGroundTTL = timeToLive;
            }

            f = (float)(this.level()
                    .getBlockState(getEntityBlockPos(this))
                    .getBlock()
                    .getFriction() * 0.98);
        }

//        xDelta *= f;
//        yDelta *= 0.9800000190734863D;
//        zDelta *= f;

        if (this.onGround()) {
            yo *= BOUNCE;
        }

        var motionScale = this.isInWater() ? this.getWaterInertia() : 1f;

//        this.setDeltaMovement(new Vec3(xDelta, yDelta, zDelta).scale(motionScale));

        this.setDeltaMovement(getDeltaMovement().scale(motionScale));
        handleGravity();
        particleTick();
    }

    private void particleTick() {
        if (this.level().isClientSide) {
            for (int i = 5; i > 0; i--) {
                this.level().addParticle(ModParticleTypes.BLOOD.get(), true,
                        this.getX() - (this.getDeltaMovement().x() / i),
                        this.getY() - (this.getDeltaMovement().y() / i),
                        this.getZ() - (this.getDeltaMovement().z() / i),
                        0, 0, 0);
            }
//            if (this.level().random.nextInt(2) == 0) {
//                this.level().addParticle(ParticleTypes.SMOKE, true, this.getX(), this.getY(), this.getZ(), 0, 0, 0);
//                this.level().addParticle(ParticleTypes.FLAME, true, this.getX(), this.getY(), this.getZ(), 0, 0, 0);
//            }
        }
    }

    private void handleGravity(){
        var gravity = 0.08D;

        this.checkSlowFallDistance();
        var vec3 = this.getDeltaMovement();
        var vec31 = this.getLookAngle();
        var f = this.getXRot() * ((float)Math.PI / 180F);
        var d1 = Math.sqrt(vec31.x * vec31.x + vec31.z * vec31.z);
        var d3 = vec3.horizontalDistance();
        var d4 = vec31.length();
        var d5 = Math.cos(f);

        d5 = d5 * d5 * Math.min(1.0D, d4 / 0.4D);
        vec3 = this.getDeltaMovement().add(0.0D, gravity * (-1.0D + d5 * 0.75D), 0.0D);
        if (vec3.y < 0.0D && d1 > 0.0D) {
            double d6 = vec3.y * -0.1D * d5;
            vec3 = vec3.add(vec31.x * d6 / d1, d6, vec31.z * d6 / d1);
        }

        if (f < 0.0F && d1 > 0.0D) {
            double d10 = d3 * (double)(-Mth.sin(f)) * 0.04D;
            vec3 = vec3.add(-vec31.x * d10 / d1, d10 * 3.2D, -vec31.z * d10 / d1);
        }

        if (d1 > 0.0D) {
            vec3 = vec3.add((vec31.x / d1 * d3 - vec3.x) * 0.1D, 0.0D, (vec31.z / d1 * d3 - vec3.z) * 0.1D);
        }

        this.setDeltaMovement(vec3.multiply(0.99F, 0.95F, 0.99F));
        this.move(MoverType.SELF, this.getDeltaMovement());

        if (this.horizontalCollision && !this.level().isClientSide) {
            double d11 = this.getDeltaMovement().horizontalDistance();
        }

        if (this.onGround() && !this.level().isClientSide) {
            this.setSharedFlag(7, false);
        }
    }


    private void handleLavaMovement(RandomSource rand) {
        if (this.level().getBlockState(ClientProxy.getEntityBlockPos(this)).getFluidState().is(LAVA)) {
            this.xDelta = 0.20000000298023224D;
            this.yDelta = (rand.nextFloat() - rand.nextFloat()) * 0.2F;
            this.zDelta = (rand.nextFloat() - rand.nextFloat()) * 0.2F;

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

    private final Lazy<LivingEntity> localEntity = Lazy.of(() -> (LivingEntity)Minecraft.getInstance().level.getEntity(getEntityId()));

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
