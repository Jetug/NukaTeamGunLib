package com.nukateam.gunscore.common.foundation.entity;

import com.nukateam.example.common.data.interfaces.IExplosiveOnHit;
import com.nukateam.gunscore.Config;
import com.nukateam.gunscore.common.base.gun.Gun;
import com.nukateam.gunscore.common.data.interfaces.IDamageable;
import com.nukateam.gunscore.common.data.util.ReflectionUtil;
import com.nukateam.gunscore.common.data.util.math.ExtendedEntityRayTraceResult;
import com.nukateam.gunscore.common.event.GunProjectileHitEvent;
import com.nukateam.gunscore.common.foundation.ModTags;
import com.nukateam.gunscore.common.foundation.init.ModEnchantments;
import com.nukateam.gunscore.common.foundation.item.GunItem;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.stats.Stats;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseFireBlock;
import net.minecraft.world.level.block.BellBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.TargetBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.MinecraftForge;

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
			distance = startVec.distanceTo(hitVec);
		}

		laserPitch = this.getXRot();
		laserYaw = this.getYRot();
		if (distance <= 0) {
			distance = this.projectile.getSpeed();
		}

		this.startVec  	= startVec;
		this.endVec  	= endVec  ;

//		this.setPos(endVec);
	}

	@Override
	protected boolean removeOnHit() {
		return false;
	}

	@Override
	public void writeSpawnData(FriendlyByteBuf buffer) {
		super.writeSpawnData(buffer);

		var startPosTag = new CompoundTag();
		startPosTag.putDouble("x", startVec.x);
		startPosTag.putDouble("y", startVec.y);
		startPosTag.putDouble("z", startVec.z);
		buffer.writeNbt(startPosTag);

		var endPosTag = new CompoundTag();
		endPosTag.putDouble("x", endVec.x);
		endPosTag.putDouble("y", endVec.y);
		endPosTag.putDouble("z", endVec.z);
		buffer.writeNbt(endPosTag);

		buffer.writeDouble(this.distance);
		buffer.writeFloat(this.laserPitch);
		buffer.writeFloat(this.laserYaw);
		buffer.writeShort(this.maxTicks);
	}

	@Override
	public void readSpawnData(FriendlyByteBuf buffer) {
		super.readSpawnData(buffer);

		var startPos = buffer.readNbt();
		if(startPos != null) {
			this.startVec = new Vec3(
					startPos.getDouble("x"),
					startPos.getDouble("y"),
					startPos.getDouble("z"));
		}

		var endPos = buffer.readNbt();
		if(endPos != null) {
			this.endVec = new Vec3(
					endPos.getDouble("x"),
					endPos.getDouble("y"),
					endPos.getDouble("z"));
		}

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
