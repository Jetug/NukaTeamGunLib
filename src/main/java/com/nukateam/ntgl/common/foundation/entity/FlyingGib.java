package com.nukateam.ntgl.common.foundation.entity;


import com.nukateam.ntgl.ClientProxy;
import com.nukateam.ntgl.common.foundation.entity.projectile.GoreData;
import com.nukateam.ntgl.common.foundation.init.ModEntityTypes;
import com.nukateam.ntgl.common.foundation.init.ModParticleTypes;
import net.minecraft.client.Minecraft;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.PowderSnowBlock;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.neoforge.common.util.Lazy;

import java.util.Optional;

import static com.nukateam.ntgl.common.foundation.entity.projectile.DeathEffect.*;
import static net.minecraft.network.syncher.SynchedEntityData.defineId;
import static net.minecraft.tags.FluidTags.LAVA;

    public class FlyingGib extends Entity {
    public static final EntityDataAccessor<Integer> ENTITY = defineId(FlyingGib.class, EntityDataSerializers.INT);
    public static final EntityDataAccessor<Integer> PART = defineId(FlyingGib.class, EntityDataSerializers.INT);
    public static final EntityDataAccessor<Float> SIZE = defineId(FlyingGib.class, EntityDataSerializers.FLOAT);
    public static final EntityDataAccessor<Float> GRAVITY = defineId(FlyingGib.class, EntityDataSerializers.FLOAT);
    public static final EntityDataAccessor<CompoundTag> DATA = defineId(FlyingGib.class, EntityDataSerializers.COMPOUND_TAG);
    public static final int LIFE = 20 * 3;
        private final Lazy<Optional<LivingEntity>> localEntity =
                Lazy.of(() -> Optional.ofNullable((LivingEntity) Minecraft.getInstance().level.getEntity(getEntityId())));

    private RandomSource rand;
//    private double xDelta = 0;
//    private double yDelta = 0;
//    private double zDelta = 0;

    public int maxTimeToLive = LIFE;
    public int timeToLive = LIFE;
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
        this(ModEntityTypes.FLYING_GIBS.get(), world);
        this.setPos(pos);
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
        getEntityData().set(DATA, data.serializeNBT(level().registryAccess()));
//        this.gravity = data.gravity * (1 + entity.getRandom().nextFloat());
//        this.gravity = data.gravity;
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        builder.define(ENTITY, -1);
        builder.define(PART, 0);
        builder.define(SIZE, 1f);
        builder.define(GRAVITY, 0f);
        builder.define(DATA, new CompoundTag());
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

            var pos = getBlockPosBelowThatAffectsMyMovement();
            var friction = (this.level().getBlockState(pos).getFriction(this.level(), pos, this) * 0.98F) / 5;
            friction = friction * 0.91F;
            this.setDeltaMovement(this.getDeltaMovement().multiply(friction, 0.98D, friction));

//            var blockpos = this.getBlockPosBelowThatAffectsMyMovement();
//            var friction = this.level().getBlockState(this.getBlockPosBelowThatAffectsMyMovement()).getFriction(level(), this.getBlockPosBelowThatAffectsMyMovement(), this);
//            var newFriction = this.onGround() ? friction * 0.91F : 0.91F;
//            var vec35 = this.handleRelativeFrictionAndCalculateMovement(getDeltaMovement(), friction);
//            var d2 = vec35.y;
//
//            if (this.level().isClientSide && !this.level().hasChunkAt(blockpos)) {
//                if (this.getY() > (double)this.level().getMinBuildHeight())
//                    d2 = -0.1D;
//                else d2 = 0.0D;
//            }
//
//            this.setDeltaMovement(vec35.x * (double)newFriction, d2 * (double)0.98F, vec35.z * (double)newFriction);
        }

        var motionScale = this.isInWater() ? this.getWaterInertia() : 1f;

        this.setDeltaMovement(getDeltaMovement().scale(motionScale));
        handleGravity();
        particleTick();
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

    @Override
    protected double getDefaultGravity() {
        return getEntityData().get(GRAVITY);

    }
    public GoreData getData() {
        if(data == null) {
            data = new GoreData();
            data.deserializeNBT(null, getEntityData().get(DATA));
        }
        return data;
    }

    @OnlyIn(Dist.CLIENT)
    public LivingEntity getLocalEntity(){
        return localEntity.get().isPresent() ? localEntity.get().get() : null;
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
