package com.nukateam.ntgl.common.foundation.entity.throwable;

import com.nukateam.ntgl.common.data.config.weapon.ProjectileConfig;
import com.nukateam.ntgl.common.foundation.item.interfaces.IThrowable;
import com.nukateam.ntgl.common.foundation.item.interfaces.IWeapon;
import com.nukateam.ntgl.common.util.interfaces.IProjectile;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.ThrowableProjectile;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;

public abstract class ThrowableItemEntity<T extends Item & IWeapon & IThrowable> extends ThrowableProjectile implements IProjectile {
    private static final EntityDataAccessor<ItemStack> ITEM = SynchedEntityData.defineId(ThrowableItemEntity.class, EntityDataSerializers.ITEM_STACK);
//    private static final EntityDataAccessor<CompoundTag> PROJECTILE = SynchedEntityData.defineId(ThrowableItemEntity.class, EntityDataSerializers.COMPOUND_TAG);

    protected ProjectileConfig projectile = new ProjectileConfig();
    private ItemStack item = ItemStack.EMPTY;
    private boolean shouldBounce;
    private float gravityVelocity = 0.03F;
    public float rotation;
    public float prevRotation;

    /* The max life of the entity. If -1, will stay alive forever and will need to be explicitly removed. */
    private int maxLife = 20 * 10;

    public ThrowableItemEntity(EntityType<? extends ThrowableItemEntity> entityType, Level worldIn) {
        super(entityType, worldIn);
    }

    public ThrowableItemEntity(EntityType<? extends ThrowableItemEntity> entityType, Level world, LivingEntity thrower, T item) {
        super(entityType, thrower, world);
        this.projectile = item.getConfig().getThrowable().getProjectile();
        this.setItem(new ItemStack(item));
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        builder.define(ITEM, ItemStack.EMPTY);
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag compound) {
        var provider = this.level().registryAccess();
        compound.put("Projectile", this.projectile.serializeNBT(provider));
        compound.putBoolean("ShouldBounce", shouldBounce);
        compound.putFloat("GravityVelocity", gravityVelocity);
        compound.put("Item", getItem().save(provider, new CompoundTag()));
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag compound) {
        var provider = this.level().registryAccess();
        this.projectile = ProjectileConfig.create(compound.getCompound("Projectile"));
        this.shouldBounce = compound.getBoolean("ShouldBounce");
        this.gravityVelocity = compound.getFloat("GravityVelocity");
        setItem(ItemStack.parseOptional(provider, compound.getCompound("Item")));
    }

    @Override
    public void tick() {
        super.tick();
        if (this.shouldBounce && this.tickCount >= this.maxLife) {
            this.remove(RemovalReason.KILLED);
            this.onDeath();
        }
    }

    @Override
    protected void onHit(HitResult result) {
        switch (result.getType()) {
            case BLOCK:
                onHitBlock((BlockHitResult) result);
                break;
            case ENTITY:
                onHitEntity((EntityHitResult) result);
                break;
            default:
                break;
        }
    }

    protected void onHitEntity(EntityHitResult result) {
        var entity = result.getEntity();
        if (this.shouldBounce) {
            double speed = this.getDeltaMovement().length();
            if (speed > 0.1) {
                var damage = getProjectileConfig().getDamage();
                entity.hurt(entity.damageSources().thrown(this, this.getOwner()), damage);
            }
            this.bounce(Direction.getNearest(this.getDeltaMovement().x(), this.getDeltaMovement().y(), this.getDeltaMovement().z()).getOpposite());
            this.setDeltaMovement(this.getDeltaMovement().multiply(0.25, 1.0, 0.25));
        } else {
            this.remove(RemovalReason.KILLED);
            this.onDeath();
        }
    }

    protected void onHitBlock(BlockHitResult result) {
        var resultPos = result.getBlockPos();
        var state = this.level().getBlockState(resultPos);

//        if(ModTags.isFragile(state)){
//            var pos = result.getBlockPos();
//            var destroySpeed = state.getDestroySpeed(this.level(), pos);
//            if (destroySpeed >= 0) {
//                float chance = Config.COMMON.gameplay.griefing.fragileBaseBreakChance.get().floatValue() / (destroySpeed + 1);
//                if (this.random.nextFloat() < chance) {
//                    this.level().destroyBlock(pos, Config.COMMON.gameplay.griefing.fragileBlockDrops.get());
//                }
//            }
//        }
//        else
            if (this.shouldBounce) {
            var event = state.getBlock().getSoundType(state, this.level(), resultPos, this).getStepSound();
            var speed = this.getDeltaMovement().length();
            if (speed > 0.1) {
                this.level().playSound(null, result.getLocation().x, result.getLocation().y, result.getLocation().z, event, SoundSource.AMBIENT, 1.0F, 1.0F);
                this.level().gameEvent(GameEvent.PROJECTILE_LAND, position(), GameEvent.Context.of(this));
            }
            this.bounce(result.getDirection());
        } else {
            this.remove(RemovalReason.KILLED);
            this.onDeath();
        }
    }

    @Override
    protected double getDefaultGravity() {
        return this.gravityVelocity;
    }

    @Override
    public boolean isNoGravity() {
        return !projectile.isGravity();
    }


//    @Override
//    public Packet<ClientGamePacketListener> getAddEntityPacket() {
//        return NetworkHooks.getEntitySpawningPacket(this);
//    }

//    @Override
//    public Packet<ClientGamePacketListener> getAddEntityPacket(ServerEntity p_entity) {
//        Entity entity = this.getOwner();
//        return new ClientboundAddEntityPacket(this, p_entity, entity == null ? 0 : entity.getId());
//    }

    public ProjectileConfig getProjectileConfig() {
        return projectile;
    }

    public ItemStack getItem() {
        return this.entityData.get(ITEM);
    }

    public void setItem(ItemStack item) {
        this.entityData.set(ITEM, item);
    }

    public void setMaxLife(int maxLife) {
        this.maxLife = maxLife;
    }

    protected void setShouldBounce(boolean shouldBounce) {
        this.shouldBounce = shouldBounce;
    }

    protected void setGravityVelocity(float gravity) {
        this.gravityVelocity = gravity;
    }

    public void onDeath() {}

    private void bounce(Direction direction) {
        switch (direction.getAxis()) {
            case X:
                this.setDeltaMovement(this.getDeltaMovement().multiply(-0.5, 0.75, 0.75));
                break;
            case Y:
                this.setDeltaMovement(this.getDeltaMovement().multiply(0.75, -0.25, 0.75));
                if (this.getDeltaMovement().y() < this.getGravity()) {
                    this.setDeltaMovement(this.getDeltaMovement().multiply(1, 0, 1));
                }
                break;
            case Z:
                this.setDeltaMovement(this.getDeltaMovement().multiply(0.75, 0.75, -0.5));
                break;
        }
    }
}
