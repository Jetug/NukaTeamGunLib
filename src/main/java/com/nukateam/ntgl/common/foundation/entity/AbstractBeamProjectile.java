package com.nukateam.ntgl.common.foundation.entity;

import com.nukateam.ntgl.common.base.gun.Gun;
import com.nukateam.ntgl.common.data.util.math.ExtendedEntityRayTraceResult;
import com.nukateam.ntgl.common.foundation.item.GunItem;
import com.nukateam.ntgl.common.network.PacketHandler;
import com.nukateam.ntgl.common.network.message.S2CMessageEntityData;
import com.nukateam.ntgl.common.network.message.S2CMessageUpdateGuns;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.raid.Raider;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;

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

	public AbstractBeamProjectile(EntityType<? extends Entity> entityType, Level worldIn,
								  LivingEntity shooter, ItemStack weapon, GunItem item, Gun modifiedGun) {
		super(entityType, worldIn, shooter, weapon, item, modifiedGun);
//		this.startVec = new Vec3(this.getX(), this.getY(), this.getZ());
	}

	@Override
	protected void defineSynchedData() {
		super.defineSynchedData();
		entityData.define(START_X	, 0f);
		entityData.define(START_Y	, 0f);
		entityData.define(START_Z	, 0f);
		entityData.define(END_X  	, 0f);
		entityData.define(END_Y  	, 0f);
		entityData.define(END_Z  	, 0f);
		entityData.define(DISTANCE  , 0f);
	}

	@Override
	public void tick() {
		if (isServerSide && this.tickCount >= this.life) {
			if (this.isAlive())
				this.onExpired();
			this.remove(RemovalReason.KILLED);
		}
//		updateClient();
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
		if (shooter == null || level().isClientSide)
			return;
//		setupDirection(shooter, weapon, (GunItem)weapon.getItem(), modifiedGun);

		var startVec = new Vec3(this.getX(), this.getY(), this.getZ());
		var endVec = startVec.add(this.getDeltaMovement());

//		setPos(shooter.getEyePosition());

		HitResult raytraceresult = rayTraceBlocks(this.level(), new ClipContext(startVec, endVec, ClipContext.Block.COLLIDER,
				ClipContext.Fluid.NONE, this), IGNORE_LEAVES);

		if (raytraceresult.getType() != HitResult.Type.MISS)
			endVec = raytraceresult.getLocation();

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
//			if (raytraceresult instanceof BlockHitResult blockHitResult &&
//					blockHitResult.getType() != HitResult.Type.MISS) {
//				var	blockPos = blockHitResult.getBlockPos();
//				Vec3 hitVec = blockHitResult.getLocation();
//
//				onHitBlock(level.getBlockState(blockPos),
//						blockPos,
//						blockHitResult.getDirection(),
//						hitVec.x, hitVec.y, hitVec.z
//				);
//			}
//			else if(raytraceresult instanceof ExtendedEntityRayTraceResult result){
//				this.onHitEntity(result.getEntity(), result.getLocation(), startVec, endVec, result.isHeadshot());
//			}

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
//		this.setPos(endVec);
	}

	@Override
	protected boolean removeOnHit() {
		return false;
	}

	@Override
	public void readAdditionalSaveData(CompoundTag compound) {
		super.readAdditionalSaveData(compound);
		var start = compound.getCompound("StartVec");
		startVec = readVec(start);

		var end = compound.getCompound("EndVec");
		endVec = readVec(end);
	}

	@Override
	public void addAdditionalSaveData(CompoundTag compound) {
		super.addAdditionalSaveData(compound);

		var start = saveVec(startVec);
		compound.put("StartVec", start);

		var end = saveVec(endVec);
		compound.put("EndVec", end);
	}

	@Override
	public void writeSpawnData(FriendlyByteBuf buffer) {
		super.writeSpawnData(buffer);

		var start = saveVec(startVec);
		buffer.writeNbt(start);

		var end = saveVec(endVec);
		buffer.writeNbt(end);

		buffer.writeFloat(this.distance);
		buffer.writeFloat(this.laserPitch);
		buffer.writeFloat(this.laserYaw);
		buffer.writeShort(this.maxTicks);
	}

	@Override
	public void readSpawnData(FriendlyByteBuf buffer) {
		super.readSpawnData(buffer);

		var startPos = buffer.readNbt();
		if(startPos != null) this.startVec = readVec(startPos);

		var endPos = buffer.readNbt();
		if(endPos != null) this.endVec = readVec(endPos);

		this.distance = buffer.readFloat();
		this.laserPitch = buffer.readFloat();
		this.laserYaw = buffer.readFloat();
		this.maxTicks = buffer.readShort();
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
