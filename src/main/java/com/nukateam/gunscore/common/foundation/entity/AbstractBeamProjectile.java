package com.nukateam.gunscore.common.foundation.entity;

import com.nukateam.gunscore.common.base.gun.Gun;
import com.nukateam.gunscore.common.data.util.math.ExtendedEntityRayTraceResult;
import com.nukateam.gunscore.common.foundation.item.GunItem;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

public abstract class AbstractBeamProjectile extends ProjectileEntity {

	public double distance = -1d;
	public float laserPitch = 0.0f;
	public float laserYaw = 0.0f;
	public short maxTicks = 0;
	
	public AbstractBeamProjectile(EntityType<? extends Entity> entityType, Level worldIn) {
		super(entityType, worldIn);
		life = 20;
		maxTicks = (short) life;
	}

	public AbstractBeamProjectile(EntityType<? extends Entity> entityType, Level worldIn, LivingEntity shooter, ItemStack weapon, GunItem item, Gun modifiedGun) {
		super(entityType, worldIn, shooter, weapon, item, modifiedGun);
	}

	@Override
	public void tick() {
		if (this.tickCount >= this.life) {
			if (this.isAlive()) {
				this.onExpired();
			}
			this.remove(RemovalReason.KILLED);
		}
	}

	public Vec3 startVec = new Vec3(0 ,0 ,0);
	public Vec3 endVec   = new Vec3(0 ,0 ,0);

	protected void trace() {
		Vec3 startVec = new Vec3(this.getX(), this.getY(), this.getZ());
		Vec3 endVec = startVec.add(this.getDeltaMovement());

		HitResult raytraceresult = rayTraceBlocks(this.level, new ClipContext(startVec, endVec, ClipContext.Block.COLLIDER,
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
//			this.onHit(raytraceresult, startVec, endVec);
			var hitVec = raytraceresult.getLocation();
			distance = startVec.distanceTo(hitVec);
		}

		laserPitch = this.getXRot();
		laserYaw = this.getYRot();
		if (distance <= 0) {
			distance = this.projectile.getSpeed();
		}

		this.startVec  	= startVec;
		this.endVec  	= endVec  ;

		this.setPos(endVec);
	}

	@Override
	public void writeSpawnData(FriendlyByteBuf buffer) {
		buffer.writeDouble(this.distance);
		buffer.writeFloat(this.laserPitch);
		buffer.writeFloat(this.laserYaw);
		buffer.writeShort(this.maxTicks);
	}

	@Override
	public void readSpawnData(FriendlyByteBuf buffer) {
		this.distance = buffer.readDouble();
		this.laserPitch = buffer.readFloat();
		this.laserYaw = buffer.readFloat();
		this.maxTicks = buffer.readShort();
	}
	
//	@Override
//	public AxisAlignedBB getRenderBoundingBox() { //TODO
//		//Vec3 pos2 = new Vec3(0, 0, distance).rotatePitch(laserPitch).rotateYaw(laserYaw);
//		Vec3 pos2 = new Vec3(this.motionX, this.motionY, this.motionZ).normalize().scale(distance);
//		return new AxisAlignedBB(this.getX(), this.getY(), this.getZ(), this.getX()+pos2.z, this.getY() + pos2.y, this.getZ() + pos2.z);
//	}
}
