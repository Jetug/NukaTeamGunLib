package com.nukateam.ntgl.common.foundation.entity;


import com.nukateam.ntgl.ClientProxy;
import com.nukateam.ntgl.client.particle.TGParticleSystem;
import com.nukateam.ntgl.common.foundation.entity.projectile.DeathEffect;
import com.nukateam.ntgl.common.foundation.init.Projectiles;
import dev.kosmx.playerAnim.core.util.Vec3d;
import mod.azure.azurelib.core.math.functions.limit.Min;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.client.resources.model.Material;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.level.Level;

import static com.nukateam.ntgl.ClientProxy.getEntityBlockPos;
import static net.minecraft.tags.FluidTags.LAVA;

public class FlyingGibs extends Entity {
    private RandomSource rand;
    public int maxTimeToLive = 200;
    public int timeToLive = 200;

    public double gravity = 0.029999999329447746D;
    public float size;
    public Vec3d rotationAxis;
    public int bodypart;
    public int hitGroundTTL = 0;

    //the exploding entity
    public LivingEntity entity;

    public DeathEffect.GoreData data;

    public TGParticleSystem trail_system;

    //public EntityDT entityDT;

    public FlyingGibs(EntityType<FlyingGibs> type, Level level) {
        super(type, level);
    }

    public FlyingGibs(Level world, LivingEntity entity, DeathEffect.GoreData data, double posX, double posY, double posZ, double xo, double yo, double zo, float size, int bodypart) {
        this(Projectiles.FLYING_GIBS.get(), world);
        this.setPos(posX, posY, posZ);
//        this.setDeltaMovement( xo, yo, zo);
        this.rand = this.level().getRandom();

        this.xo = xo;
        this.yo = yo;
        this.zo = zo;

        var rand = this.level().getRandom();
        this.size = size;
        this.maxTimeToLive = 75 + rand.nextInt(50);
        this.timeToLive = maxTimeToLive;
        this.entity = entity;
        this.bodypart = bodypart;
        //this.entityDT = entityDT;
        this.rotationAxis = new Vec3d(rand.nextDouble(), rand.nextDouble(), rand.nextDouble());
        this.data = data;

        trail_system = new TGParticleSystem(this, data.type_trail);
        ClientProxy.particleManager.addEffect(trail_system);
    }

    @Override
    public void tick() {
        super.tick();

        if (this.timeToLive > 0) {
            --timeToLive;
        } else {
            this.kill();
        }

        this.xOld = this.getX();
        this.yOld = this.getY();
        this.zOld = this.getZ();
        this.yo -= gravity;

        if (this.level().getBlockState(ClientProxy.getEntityBlockPos(this)).getFluidState().is(LAVA)) {
            this.xo = 0.20000000298023224D;
            this.yo = (double) ((this.rand.nextFloat() - this.rand.nextFloat()) * 0.2F);
            this.zo = (double) ((this.rand.nextFloat() - this.rand.nextFloat()) * 0.2F);

            this.playSound(SoundEvents.GENERIC_BURN, 0.4F, 2.0F + this.rand.nextFloat() * 0.4F);
        }


        this.move(MoverType.SELF, getDeltaMovement());
        float f = 0.98F;

        if (this.onGround()) {
            //System.out.println("onGround.");
            if (hitGroundTTL == 0) {
                hitGroundTTL = timeToLive;
                trail_system.setExpired();
            }

            f = (float) (this.level().getBlockState(getEntityBlockPos(this))
                    .getBlock().getFriction() * 0.98);

        }

        this.xo *= (double) f;
        this.yo *= 0.9800000190734863D;
        this.zo *= (double) f;

        if (this.onGround()) {
            this.yo *= -0.8999999761581421D;
        }

//        this.posX += this.xo;
//        this.posY += this.yo;
//        this.posZ += this.zo;

//        double e = 0.001d;
//        if (xo >= e || yo >= e || zo >= e) {
//        	
//        }
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

    @Override
    protected void defineSynchedData() {

    }

    @Override
    protected void readAdditionalSaveData(CompoundTag pCompound) {

    }

    @Override
    protected void addAdditionalSaveData(CompoundTag pCompound) {

    }
}
