package com.nukateam.ntgl.common.foundation.entity.throwable;

import com.nukateam.ntgl.common.data.config.weapon.ProjectileConfig;
import com.nukateam.ntgl.common.foundation.entity.ProjectileEntity;
import com.nukateam.ntgl.common.foundation.init.NtglEntityDataSerializers;
import com.nukateam.ntgl.common.foundation.item.interfaces.IThrowable;
import com.nukateam.ntgl.common.foundation.item.interfaces.IWeapon;
import com.nukateam.ntgl.common.util.interfaces.IProjectile;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializer;
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
import net.neoforged.jarjar.nio.util.Lazy;
import org.jetbrains.annotations.NotNull;

public abstract class ThrowableItemEntity<T extends Item & IWeapon & IThrowable> extends ThrowableProjectile implements IProjectile {
    private static final EntityDataAccessor<ItemStack> ITEM = getDataAccessor(EntityDataSerializers.ITEM_STACK);
    private static final Lazy<EntityDataAccessor<ProjectileConfig>> PROJECTILE = Lazy.of(() ->getDataAccessor(NtglEntityDataSerializers.PROJECTILE_CONFIG.get()));

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
        setProjectile(item.getConfig().getThrowable().getProjectile());
        this.setItem(new ItemStack(item));

        if (thrower instanceof net.minecraft.world.entity.player.Player player) {
            float pitch = player.getXRot();
            float yaw = player.getYRot();
            float radYaw = yaw * net.minecraft.util.Mth.DEG_TO_RAD;
            float radPitch = pitch * net.minecraft.util.Mth.DEG_TO_RAD;
            float dirX = -net.minecraft.util.Mth.sin(radYaw) * net.minecraft.util.Mth.cos(radPitch);
            float dirY = -net.minecraft.util.Mth.sin(radPitch);
            float dirZ = net.minecraft.util.Mth.cos(radYaw) * net.minecraft.util.Mth.cos(radPitch);
            float rightX = -net.minecraft.util.Mth.cos(radYaw);
            float rightZ = -net.minecraft.util.Mth.sin(radYaw);
            double offsetX = rightX * 0.35 + dirX * 0.1;
            double offsetY = -0.3 + dirY * 0.1;
            double offsetZ = rightZ * 0.35 + dirZ * 0.1;
            this.setPos(this.getX() + offsetX, this.getY() + (offsetY + 0.1), this.getZ() + offsetZ);
        }
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        builder.define(ITEM, ItemStack.EMPTY);
        builder.define(PROJECTILE.get(), new ProjectileConfig());
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag compound) {
        var provider = this.level().registryAccess();
        compound.putBoolean("ShouldBounce", shouldBounce);
        compound.putFloat("GravityVelocity", gravityVelocity);
        compound.put("Item", getItem().save(provider, new CompoundTag()));
        compound.put("Projectile", getProjectile().serializeNBT(provider));
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag compound) {
        var provider = this.level().registryAccess();
        this.shouldBounce = compound.getBoolean("ShouldBounce");
        this.gravityVelocity = compound.getFloat("GravityVelocity");
        setItem(ItemStack.parseOptional(provider, compound.getCompound("Item")));
        setProjectile(ProjectileConfig.create(compound.getCompound("Projectile")));
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
                var damage = getProjectile().getDamage();
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
        return !getProjectile().isGravity();
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

    public ProjectileConfig getProjectile() {
        return this.entityData.get(PROJECTILE.get());
    }

    public void setProjectile(ProjectileConfig projectile) {
        this.entityData.set(PROJECTILE.get(), projectile);
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

    private static @NotNull <T> EntityDataAccessor<T> getDataAccessor(EntityDataSerializer<T> serializer) {
        return SynchedEntityData.defineId(ThrowableItemEntity.class, serializer);
    }
}
