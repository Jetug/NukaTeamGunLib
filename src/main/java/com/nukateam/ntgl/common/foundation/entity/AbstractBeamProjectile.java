package com.nukateam.ntgl.common.foundation.entity;

import com.nukateam.ntgl.common.data.WeaponData;
import com.nukateam.ntgl.common.util.util.math.ExtendedEntityRayTraceResult;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

import static net.minecraft.network.syncher.EntityDataSerializers.*;
import static net.minecraft.network.syncher.SynchedEntityData.*;

public abstract class AbstractBeamProjectile extends ProjectileEntity {
	protected float distance = -1f;
	protected float laserPitch = 0.0f;
	protected float laserYaw = 0.0f;
	protected short maxTicks = 0;

	protected Vec3 startVec = new Vec3(0 ,0 ,0);
	protected Vec3 endVec   = new Vec3(0 ,0 ,0);

	public static final EntityDataAccessor<Float> START_X = defineId(AbstractBeamProjectile.class, FLOAT);
	public static final EntityDataAccessor<Float> START_Y = defineId(AbstractBeamProjectile.class, FLOAT);
	public static final EntityDataAccessor<Float> START_Z = defineId(AbstractBeamProjectile.class, FLOAT);

	public static final EntityDataAccessor<Float> END_X = defineId(AbstractBeamProjectile.class, FLOAT);
	public static final EntityDataAccessor<Float> END_Y = defineId(AbstractBeamProjectile.class, FLOAT);
	public static final EntityDataAccessor<Float> END_Z = defineId(AbstractBeamProjectile.class, FLOAT);

	public static final EntityDataAccessor<Float> DISTANCE = defineId(AbstractBeamProjectile.class, FLOAT);

	public AbstractBeamProjectile(EntityType<? extends Entity> entityType, Level worldIn) {
		super(entityType, worldIn);
		life = 20;
		maxTicks = (short) life;
	}

	public AbstractBeamProjectile(EntityType<? extends Entity> entityType, Level worldIn, WeaponData data) {
		super(entityType, worldIn, data);
	}

	@Override
	protected void defineSynchedData(SynchedEntityData.Builder builder) {
		super.defineSynchedData(builder);
		builder.define(START_X	, 0f);
		builder.define(START_Y	, 0f);
		builder.define(START_Z	, 0f);
		builder.define(END_X  	, 0f);
		builder.define(END_Y  	, 0f);
		builder.define(END_Z  	, 0f);
		builder.define(DISTANCE  , 0f);
	}

	@Override
	public void tick() {
		if (isServerSide && this.tickCount >= this.life) {
			if (this.isAlive())
				this.onExpired();
			this.remove(RemovalReason.KILLED);
		}
	}

	public float getDistance() {
		return getEntityData().get(DISTANCE);
	}

	public Vec3 getStartVec(){
		var x = this.getEntityData().get(START_X);
		var y = this.getEntityData().get(START_Y);
		var z = this.getEntityData().get(START_Z);
		return new Vec3(x, y, z);
	}

	public Vec3 getEndVec(){
		var x= this.getEntityData().get(END_X);
		var y= this.getEntityData().get(END_Y);
		var z= this.getEntityData().get(END_Z);
		return new Vec3(x, y, z);
	}


	public void trace() {
		if (owner == null || level().isClientSide)
			return;

		var startVec = new Vec3(this.getX(), this.getY(), this.getZ());
		var endVec = startVec.add(this.getDeltaMovement());

		HitResult raytraceresult = rayTraceBlocks(this.level(), new ClipContext(startVec, endVec, ClipContext.Block.COLLIDER,
				ClipContext.Fluid.NONE, this), IGNORE_LEAVES);

		if (raytraceresult.getType() != HitResult.Type.MISS)
			endVec = raytraceresult.getLocation();

		var entityResult = this.findEntityOnPath(owner, startVec, endVec);

		if (entityResult != null) {
			raytraceresult = new ExtendedEntityRayTraceResult(entityResult);
			if (((EntityHitResult)raytraceresult).getEntity() instanceof Player player) {
				if (this.owner instanceof Player && !((Player) this.owner).canHarmPlayer(player)) {
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
			distance = this.projectile.getSpeed();
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
	protected boolean removeOnHit(HitTarget hitTarget) {
		return false;
	}

	@Override
	public void addAdditionalSaveData(CompoundTag compound) {
		super.addAdditionalSaveData(compound);

		compound.put	 ("StartVec"	, saveVec(this.startVec));
		compound.put	 ("EndVec"		, saveVec(this.endVec)	);
		compound.putFloat("distance"	, this.distance			);
		compound.putFloat("laserPitch"	, this.laserPitch		);
		compound.putFloat("laserYaw"	, this.laserYaw			);
		compound.putShort("maxTicks"	, this.maxTicks			);
	}

	@Override
	public void readAdditionalSaveData(CompoundTag compound) {
		super.readAdditionalSaveData(compound);
        this.startVec 	= readVec(compound.getCompound("StartVec"));
        this.endVec 	= readVec(compound.getCompound("EndVec"));
		this.distance 	= compound.getFloat("distance"	);
		this.laserPitch = compound.getFloat("laserPitch");
		this.laserYaw 	= compound.getFloat("laserYaw"	);
		this.maxTicks 	= compound.getShort("maxTicks"	);
	}

	private CompoundTag saveVec(Vec3 vec) {
		var start = new CompoundTag();
		start.putDouble("x", vec.x);
		start.putDouble("y", vec.y);
		start.putDouble("z", vec.z);
		return start;
	}

	private Vec3 readVec(CompoundTag tag) {
		var x = tag.getDouble("x");
		var y = tag.getDouble("y");
		var z = tag.getDouble("z");
		return new Vec3(x, y, z);
	}
}
