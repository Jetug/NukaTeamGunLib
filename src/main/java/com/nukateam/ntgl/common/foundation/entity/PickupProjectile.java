package com.nukateam.ntgl.common.foundation.entity;

import com.nukateam.ntgl.common.data.WeaponData;
import com.nukateam.ntgl.common.util.world.ExplosionUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseFireBlock;
import net.minecraft.world.level.block.CampfireBlock;
import net.minecraft.world.level.block.CandleBlock;
import net.minecraft.world.level.block.CandleCakeBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;

public class PickupProjectile extends ProjectileEntity {
    private boolean inGround;
    private BlockPos inBlockPos;
    private int shakeTime;

    public PickupProjectile(EntityType<? extends ProjectileEntity> entityType, Level worldIn) {
        super(entityType, worldIn);
    }

    public PickupProjectile(EntityType<? extends ProjectileEntity> entityType, Level worldIn, WeaponData data) {
        super(entityType, worldIn, data);
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag compound) {
        super.addAdditionalSaveData(compound);
        compound.putBoolean("inGround", this.inGround);
        if (this.inBlockPos != null) {
            compound.put("inBlock", NbtUtils.writeBlockPos(inBlockPos));
        }
        compound.putInt("shakeTime", this.shakeTime);
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag compound) {
        super.readAdditionalSaveData(compound);
        this.inGround = compound.getBoolean("inGround");
        if (compound.contains("inBlock")) {
            var blockPos = compound.getCompound("inBlockPos");
            this.inBlockPos = NbtUtils.readBlockPos(blockPos);
        }
        this.shakeTime = compound.getInt("shakeTime");
    }

    @Override
    public void writeSpawnData(FriendlyByteBuf buffer) {
        super.writeSpawnData(buffer);
        buffer.writeBoolean(this.inGround);
        buffer.writeBlockPos(this.inBlockPos != null ? this.inBlockPos : BlockPos.ZERO);
        buffer.writeVarInt(this.shakeTime);
    }

    @Override
    public void readSpawnData(FriendlyByteBuf buffer) {
        super.readSpawnData(buffer);
        this.inGround = buffer.readBoolean();
        this.inBlockPos = buffer.readBlockPos();
        this.shakeTime = buffer.readVarInt();
    }

    @Override
    protected void travel() {
        if (this.inGround) {
            this.checkInsideBlocks();

            if (this.shakeTime > 0) {
                this.shakeTime--;
            }
        } else {
            super.travel();
        }
    }

    @Override
    protected void onHitBlock(BlockHitResult hitResult, BlockState blockState) {
        if (!this.inGround) {
            this.inGround = true;
            this.inBlockPos = hitResult.getBlockPos().immutable();
            this.shakeTime = 20;
            this.hasImpulse = false;
            this.setDeltaMovement(Vec3.ZERO);
            this.setPos(hitResult.getLocation());
            playHitSound();
        }

        super.onHitBlock(hitResult, blockState);
    }

    @Override
    protected boolean removeOnHit(HitTarget hitTarget) {
        return hitTarget == HitTarget.ENTITY;
    }

    @Override
    protected void onExpired() {
        if (!this.inGround && ExplosionUtils.isExplosive(projectile.getExplosion())) {
            ExplosionUtils.createExplosion(this, projectile.getExplosion(), position());
        }
    }

    @Override
    public void playerTouch(Player player) {
        if ((this.level().isClientSide || this.inGround && this.shakeTime <= 0) && this.canBePickedUp(player)) {
            this.pickup(player);
        }
    }

    private boolean canBePickedUp(Player player) {
        return !this.isRemoved() && (player.getInventory().add(this.getPickupItem()) || player.getAbilities().instabuild);
    }

    protected ItemStack getPickupItem() {
        return this.ammo.copy();
    }

    protected void playHitSound() {
        var sound = BuiltInRegistries.SOUND_EVENT.get(projectile.getHitSound());
        this.playSound(sound, 1.0F, 1.2F / (this.random.nextFloat() * 0.2F + 0.9F));
    }

    private void pickup(Player player) {
        if (player.getAbilities().instabuild) {
            this.discard();
        } else {
            player.take(this, 1);
            this.discard();
        }

        this.playSound(SoundEvents.ITEM_PICKUP, 0.2F, 1.0F);
    }
}
