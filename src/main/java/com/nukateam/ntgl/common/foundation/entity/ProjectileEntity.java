package com.nukateam.ntgl.common.foundation.entity;

import com.nukateam.ntgl.Ntgl;
import com.nukateam.ntgl.common.foundation.init.NtglEntityDataSerializers;
import com.nukateam.ntgl.common.network.LevelLocation;
import com.nukateam.ntgl.common.data.WeaponData;
import com.nukateam.ntgl.common.data.config.weapon.ProjectileConfig;
import com.nukateam.ntgl.Config;
import com.nukateam.ntgl.common.data.holders.AmmoHolder;
import com.nukateam.ntgl.common.data.holders.WeaponMode;
import com.nukateam.ntgl.common.foundation.item.interfaces.IWeapon;
import com.nukateam.ntgl.common.network.message.weapon.S2CMessageBlood;
import com.nukateam.ntgl.common.network.message.weapon.S2CMessageProjectileHitBlock;
import com.nukateam.ntgl.common.network.message.weapon.S2CMessageProjectileHitEntity;
import com.nukateam.ntgl.common.network.message.weapon.S2CMessageProjectileHitFluid;
import com.nukateam.ntgl.common.util.helpers.EntityResult;
import com.nukateam.ntgl.common.util.helpers.RayTraceHelper;
import com.nukateam.ntgl.common.util.interfaces.IDamageable;
import com.nukateam.ntgl.common.util.interfaces.IProjectile;
import com.nukateam.ntgl.common.util.managers.BoundingBoxManager;
import com.nukateam.ntgl.common.util.trackers.SpreadTracker;
import com.nukateam.ntgl.common.util.interfaces.IHeadshotBox;
import com.nukateam.ntgl.common.util.util.*;
import com.nukateam.ntgl.common.util.util.math.ExtendedEntityRayTraceResult;
import com.nukateam.ntgl.common.event.GunProjectileHitEvent;
import com.nukateam.ntgl.common.foundation.ModTags;
import com.nukateam.ntgl.common.foundation.init.ModSyncedDataKeys;
import com.nukateam.ntgl.common.util.world.ExplosionUtils;
import com.nukateam.ntgl.common.network.PacketHandler;
import com.nukateam.ntgl.common.foundation.event.GunProjectileSpawnEvent;
import com.nukateam.ntgl.common.compat.sable.SableSupport;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializer;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.stats.Stats;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.*;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.*;
import net.neoforged.neoforge.common.NeoForge;
import org.jetbrains.annotations.NotNull;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.animation.AnimatableManager;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Predicate;

import static software.bernie.geckolib.util.GeckoLibUtil.createInstanceCache;

public class ProjectileEntity extends Entity implements GeoEntity, IProjectile {
    private static final EntityDataAccessor<ItemStack> AMMO = getDataAccessor(EntityDataSerializers.ITEM_STACK);
    private static final EntityDataAccessor<ProjectileConfig> PROJECTILE = getDataAccessor(NtglEntityDataSerializers.PROJECTILE_CONFIG_SERIALIZER);
    private static final EntityDataAccessor<Integer> WIELDER_ID = getDataAccessor(EntityDataSerializers.INT);

    protected static final Predicate<Entity> PROJECTILE_TARGETS = input -> input != null && input.isPickable() && !input.isSpectator();
    protected static final Predicate<BlockState> IGNORE_LEAVES = input -> input != null && Config.COMMON.gameplay.ignoreLeaves.get() && input.getBlock() instanceof LeavesBlock;
    protected final AnimatableInstanceCache cache = createInstanceCache(this);
    protected AmmoHolder ammoHolder;
    protected WeaponMode weaponAction;
    protected WeaponData weaponData;
    protected boolean isServerSide = !level().isClientSide();
    protected boolean isRightHand;
    protected int shooterId;
    protected LivingEntity owner;
    protected ItemStack weapon = ItemStack.EMPTY;
    protected EntityDimensions entitySize;
    protected double modifiedGravity;
    protected int life;
    protected int pierceCount;
    protected float damageMultiplier = 1.0f;
    protected float criticalChanceMultiplier = 1.0f;
    protected float criticalDamageMultiplier = 1.0f;

    public ProjectileEntity(EntityType<? extends Entity> entityType, Level worldIn) {
        super(entityType, worldIn);
    }

    public ProjectileEntity(EntityType<? extends Entity> entityType, Level level, WeaponData data) {
        this(entityType, level);

        var weaponItem = (IWeapon) data.weapon.getItem();
        var weaponStack = data.weapon;

        var projectile = WeaponStateHelper.getProjectileConfig(data);

        this.weaponData = data;
        this.owner = data.wielder;
        this.weaponAction = data.weaponMode;
        this.weapon = weaponStack;
        this.entitySize = EntityDimensions.fixed(projectile.getSize(), projectile.getSize());
        this.modifiedGravity = WeaponModifierHelper.getProjectileGravity(data, -0.04);
        this.life = WeaponModifierHelper.getProjectileLife(data, projectile.getLife());
        var hand = owner.getMainHandItem() == weaponStack ? InteractionHand.MAIN_HAND : InteractionHand.OFF_HAND;
        this.isRightHand = hand == InteractionHand.MAIN_HAND;
        this.ammoHolder = WeaponStateHelper.getCurrentAmmo(data);
        this.pierceCount = projectile.getPierceLevel();

        setOwnerId(owner.getId());
        setItem(setupAmmo(data));
        setProjectile(projectile);
        setBoundingBox(new AABB(
                projectile.getSize(), projectile.getSize(), projectile.getSize(),
                -projectile.getSize(), -projectile.getSize(), -projectile.getSize()));

        var dir = this.getDirection(owner, weaponStack, weaponItem);
        var speed =  projectile.getSpeed();

        this.setDeltaMovement(dir.x * speed, dir.y * speed, dir.z * speed);
        this.updateHeading();
        this.setupDirection(owner, weaponStack, weaponItem);
        this.setupStartPosition(owner);
    }

    @Override
    public Entity getOwner() {
        return owner;
    }

    @Deprecated
    public Entity getShooter() {
        return getOwner();
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        builder.define(AMMO, ItemStack.EMPTY);
        builder.define(PROJECTILE, new ProjectileConfig());
        builder.define(WIELDER_ID, -1);
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag compound) {
        var provider = getProvider();
        compound.put("Weapon", weapon.save(provider, new CompoundTag()));
        compound.putString("WeaponAction", weaponAction.toString());
        compound.putString("AmmoHolder", ammoHolder.toString());
        compound.putDouble("ModifiedGravity", this.modifiedGravity);
        compound.putInt("MaxLife", this.life);
        compound.putBoolean("IsRightHand", this.isRightHand);
        compound.putInt("ShooterId", this.shooterId);

        compound.put("Ammo", getItem().save(provider, new CompoundTag()));
        compound.put("Projectile", getProjectile().serializeNBT(provider));
    }

    private @NotNull RegistryAccess getProvider() {
        return level().registryAccess();
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag compound) {
        var provider = getProvider();
        this.weapon = ItemStack.parseOptional(provider, compound.getCompound("Weapon"));
        this.weaponAction = WeaponMode.getType(compound.getString("WeaponAction"));
        this.ammoHolder = AmmoHolder.getType(compound.getString("AmmoHolder"));
        this.modifiedGravity = compound.getDouble("ModifiedGravity");
        this.life = compound.getInt("MaxLife");
        this.isRightHand = compound.getBoolean("IsRightHand");
        this.shooterId = compound.getInt("ShooterId");
        this.entitySize = EntityDimensions.fixed(this.getProjectile().getSize(), this.getProjectile().getSize());

        setBoundingBox(new AABB(
                getProjectile().getSize(), getProjectile().getSize(), getProjectile().getSize(),
                -getProjectile().getSize(), -getProjectile().getSize(), -getProjectile().getSize()));

        setItem(ItemStack.parseOptional(provider, compound.getCompound("Ammo")));
        setProjectile(ProjectileConfig.create(compound.getCompound("Projectile")));
    }

    @Override
    public EntityDimensions getDimensions(Pose pose) {
        return this.entitySize;
    }

    @Override
    public boolean shouldRenderAtSqrDistance(double distance) {
        return true;
    }

    @Override
    public void tick() {
        super.tick();
        this.updateHeading();
        this.onProjectileTick();

        if (isServerSide) {
            rayTraceTargets();
            travel();
        }

        if (this.tickCount >= this.life) {
            if (this.isAlive()) {
                this.onExpired();
            }
            this.remove(RemovalReason.KILLED);
        }
    }

    @Override
    protected double getDefaultGravity() {
        return getProjectile().isGravity() || isAffectedByFluid() ? modifiedGravity : 0.0;
    }

    private boolean isAffectedByFluid() {
        return getProjectile().affectedByFluid() && this.isInFluidType();
    }

    public boolean isVisible(){
        return getProjectile().isVisible();
    }

    public boolean isRightHand(){
        return isRightHand;
    }

    public void setWeapon(ItemStack weapon) {
        this.weapon = weapon.copy();
    }

    public ItemStack getWeapon() {
        return this.weapon;
    }

    public ItemStack getItem() {
        return this.entityData.get(AMMO);
    }

    public void setItem(ItemStack item) {
        this.entityData.set(AMMO, item);
    }

    public ProjectileConfig getProjectile() {
        return this.entityData.get(PROJECTILE);
    }

    public void setProjectile(ProjectileConfig projectile) {
        this.entityData.set(PROJECTILE, projectile);
    }

    public double getModifiedGravity() {
        return this.modifiedGravity;
    }

    public int getLife(){
        return getProjectile().getLife();
    }

    public int getOwnerId() {
        return this.entityData.get(WIELDER_ID);
    }

    public void setOwnerId(int id) {
        this.entityData.set(WIELDER_ID, id);
    }

    public float getDamage() {
        if(weapon.isEmpty())
            return 0;

        var data = new WeaponData(this.weapon, this.owner);

        float initialDamage = WeaponModifierHelper.getProjectileDamage(BuiltInRegistries.ITEM.getKey(getItem().getItem()), data);

        if (this.getProjectile().isDamageReduceOverLife()) {
            float modifier = ((float) this.getProjectile().getLife() - (float) (this.tickCount - 1)) / (float) this.getProjectile().getLife();
            initialDamage *= modifier;
        }

        var projectileAmount = WeaponModifierHelper.getProjectileAmount(data);
        var damage = initialDamage / projectileAmount;

        damage *= this.damageMultiplier;

        return Math.max(0F, damage);
    }

    protected void travel() {
        var nextPosX = this.getX() + this.getDeltaMovement().x();
        var nextPosY = this.getY() + this.getDeltaMovement().y();
        var nextPosZ = this.getZ() + this.getDeltaMovement().z();

        this.setPos(nextPosX, nextPosY, nextPosZ);

        if (this.getProjectile().isGravity() || isAffectedByFluid()) {
            this.setDeltaMovement(this.getDeltaMovement().add(0, this.getDefaultGravity(), 0));
        }

        if (isAffectedByFluid()) {
            var motion = this.getDeltaMovement();
            double drag = this.getFluidDrag();
            this.setDeltaMovement(motion.x * drag, motion.y * drag, motion.z * drag);
        }
    }

    protected void rayTraceTargets() {
        var startVec = this.position();
        var endVec = startVec.add(this.getDeltaMovement());
        var result = (HitResult) rayTraceBlocks(this.level(), new ClipContext(startVec, endVec, ClipContext.Block.COLLIDER, ClipContext.Fluid.ANY, this), getBlockFilter());

        double bestDistSqr = result.getType() == HitResult.Type.MISS
                ? Double.MAX_VALUE
                : startVec.distanceToSqr(result.getLocation());

        // SABLE: also test against blocks belonging to any sub-level whose plot currently
        // overlaps this ray segment (getAllIntersecting + inverse pose transform).

        if(Ntgl.sableLoaded) {
            var subLevelHit = SableSupport.findSubLevelBlockHit(this.level(), startVec, endVec, getBlockFilter(), this);
            if (subLevelHit != null) {
                double subLevelDistSqr = startVec.distanceToSqr(subLevelHit.getLocation());
                if (subLevelDistSqr < bestDistSqr) {
                    bestDistSqr = subLevelDistSqr;
                    result = subLevelHit;
                }
            }
        }
        if (result.getType() != HitResult.Type.MISS) {
            if (!(result instanceof BlockHitResult bhr && !level().getBlockState(bhr.getBlockPos()).getFluidState().isEmpty())) {
                // getLocation() is global here (see note above), so entity search below still
                // operates in the correct (global) coordinate space.
                endVec = result.getLocation();
            }
        }

        var hitEntities = getHitEntityResult(startVec, endVec);

        if (hitEntities != null && !hitEntities.isEmpty()) {
            for (var hit : hitEntities) {
                var entityHitResult = new ExtendedEntityRayTraceResult(hit);

                if (entityHitResult.getEntity() instanceof Player playerTarget) {
                    if (this.owner instanceof Player playerShooter && !playerShooter.canHarmPlayer(playerTarget)) {
                        entityHitResult = null;
                    }
                }
                if (entityHitResult != null) {
                    this.onHit(entityHitResult, startVec, endVec);
                }
            }
        } else {
            this.onHit(result, startVec, endVec);
        }
    }

    protected double getFluidDrag() {
        var fluidState = this.level().getFluidState(blockPosition());
        var fluid = fluidState.getType();
        var density = fluid.getFluidType().getDensity();
        if (density <= 0) return 1.0;
        return 1.0 / (1.0 + (density / 1000.0));
    }

    private @Nullable List<EntityResult> getHitEntityResult(Vec3 startVec, Vec3 endVec) {
        List<EntityResult> hitEntities = null;

        if (pierceCount == 0) {
            var entityResult = this.findEntityOnPath(owner, startVec, endVec);
            if (entityResult != null) {
                hitEntities = Collections.singletonList(entityResult);
            }
        } else {
            hitEntities = this.findEntitiesOnPath(startVec, endVec);
        }
        return hitEntities;
    }

    protected Predicate<BlockState> getBlockFilter() {
        return (value) -> false;
    }

    protected void onProjectileTick() {}

    protected void onExpired() {
        if(ExplosionUtils.isExplosive(getProjectile().getExplosion())){
            ExplosionUtils.createExplosion(this, getProjectile().getExplosion(), position());
        }
    }

    protected boolean removeOnHit(HitTarget hitTarget) {
        return hitTarget != HitTarget.FLUID;
    }

    @Nullable
    protected EntityResult findEntityOnPath(LivingEntity shooter, Vec3 startVec, Vec3 endVec) {
        Vec3 hitVec = null;
        Entity hitEntity = null;
        var headshot = false;
        var entities = this.level().getEntities(this,
                this.getBoundingBox()
                        .expandTowards(this.getDeltaMovement())
                        .inflate(1.0), (entity) -> PROJECTILE_TARGETS.test(entity) && shooter != null && shooter.getVehicle() != entity);

        var closestDistance = Double.MAX_VALUE;
        for (Entity target : entities) {
            if (!target.equals(this.owner)) {
                var result = this.getHitResult(target, startVec, endVec);
                if (result == null) continue;
                var hitPos = result.getHitPos();

                // SABLE: use the sub-level-aware distance instead of a raw distanceTo, so
                // targets riding/standing on a sub-level are ranked correctly even though
                // Sable already fixes Entity#distanceToSqr for tracking entities in general.
                var distanceToHit = SableSupport.distanceSquared(this.level(), startVec, hitPos);

                if (distanceToHit < closestDistance) {
                    hitVec = hitPos;
                    hitEntity = target;
                    closestDistance = distanceToHit;
                    headshot = result.isHeadshot();
                }
            }
        }
        return hitEntity != null ? new EntityResult(hitEntity, hitVec, headshot) : null;
    }

    @Nullable
    protected List<EntityResult> findEntitiesOnPath(Vec3 startVec, Vec3 endVec) {
        List<EntityResult> hitEntities = new ArrayList<>();
        List<Entity> entities = this.level().getEntities(this, this.getBoundingBox().expandTowards(this.getDeltaMovement()).inflate(1.0), PROJECTILE_TARGETS);
        for (Entity entity : entities) {
            if (!entity.equals(this.owner)) {
                EntityResult result = this.getHitResult(entity, startVec, endVec);
                if (result == null)
                    continue;
                hitEntities.add(result);
            }
        }
        return hitEntities;
    }

    protected void onHit(HitResult result, Vec3 startVec, Vec3 endVec) {
        if (NeoForge.EVENT_BUS.post(new GunProjectileHitEvent(result, this)).isCanceled()) {
            return;
        }

        if (result instanceof BlockHitResult blockHitResult) {
            if (blockHitResult.getType() == HitResult.Type.MISS) {
                return;
            }

            var pos = blockHitResult.getBlockPos();
            // SABLE: if this hit came from SableSupport.findSubLevelBlockHit, the BlockPos is a
            // *local plot-grid* position, not a normal world position — getBlockState still works
            // because it's the same Level, but anything that needs the *visual*/global position
            // (particles, network hit location, block-break checks against unrelated systems)
            // must go through SableSupport.toGlobal(...) first. See onHitBlock/onHitEntity below.
            var state = this.level().getBlockState(pos);

            if (!state.getFluidState().isEmpty()) {
                this.onHitFluid(blockHitResult, state);
            }
            else {
                onHitBlock(blockHitResult, state);
            }
        }
        else if (result instanceof ExtendedEntityRayTraceResult entityHitResult) {
            onHitEntity(entityHitResult);
        }
    }

    protected void onHitEntity(ExtendedEntityRayTraceResult entityHitResult) {
        var entity = entityHitResult.getEntity();
        var hitVec = entityHitResult.getLocation();

        if (entity.getId() == this.owner.getId()) return;
        if (this.owner instanceof Player player && entity.hasIndirectPassenger(player)) return;

        burnEntity(entity);
        damageEntity(entityHitResult);

        // SABLE: project the blood-effect position to global space before sending it to
        // clients — harmless no-op when Sable isn't installed or the position isn't in a plot.
        var globalHitVec = SableSupport.toGlobal(this.level(), hitVec);
        PacketHandler.getPlayChannel().sendToTrackingEntity(() -> entity, new S2CMessageBlood(globalHitVec));
        onContact(hitVec);
        handlePierce(HitTarget.ENTITY);

        entity.invulnerableTime = 0;
    }

    private BlockPos hitBlockpos = BlockPos.ZERO;

    protected void onHitBlock(BlockHitResult blockHitResult, BlockState state) {
        var blockPos = blockHitResult.getBlockPos();
        var block = state.getBlock();
        var hitVec = blockHitResult.getLocation();

        sendHitBlockMessage(blockHitResult, hitVec);
        handleBlockBreaking(blockPos, state);
        onContact(hitVec);
        checkDamageable(state, blockPos);
        checkTargetBlock(blockHitResult, state);
        checkBellBlock(blockHitResult, block, blockPos);

        if(blockPos.equals(hitBlockpos) && !state.canBeReplaced()) {
            handlePierce(HitTarget.BLOCK);
            playHitSound();
        }
//        if (!state.canBeReplaced() && removeOnHit(HitTarget.BLOCK)) {
//            this.remove(RemovalReason.KILLED);
//        }

        hitBlockpos = blockPos;
    }


    protected void playHitSound() {
        var hitSound = getProjectile().getHitSound();
        if(hitSound != null) {
            var sound = BuiltInRegistries.SOUND_EVENT.get(hitSound);
            this.playSound(sound, 1.0F, 1.2F / (this.random.nextFloat() * 0.2F + 0.9F));
        }
    }

    protected void onHitFluid(BlockHitResult hitResult, BlockState state) {
        var pos = hitResult.getLocation();
        var fluidState = state.getFluidState();
        var isLava = fluidState.is(FluidTags.LAVA);

        if (!this.wasTouchingWater) {
            wasTouchingWater = true;

            // SABLE: fluid hit position projected to global space for the nearby-players packet.
            var globalPos = SableSupport.toGlobal(this.level(), pos);

            PacketHandler.getPlayChannel().sendToNearbyPlayers(
                    () -> LevelLocation.create((ServerLevel)level(), globalPos, 32),
                    new S2CMessageProjectileHitFluid(
                            globalPos,
                            getBbWidth(),
                            (float)getDeltaMovement().length(),
                            isLava,
                            this.getId())
            );
            this.gameEvent(GameEvent.SPLASH);
        }

        if (removeOnHit(HitTarget.FLUID)) {
            this.remove(RemovalReason.KILLED);
        }
    }

    protected void handlePierce(HitTarget hitTarget) {
        if (pierceCount == 0 && removeOnHit(hitTarget)) {
            this.remove(RemovalReason.KILLED);
        }
        pierceCount = Math.max(0, pierceCount - 1);
    }

    protected void burnEntity(Entity entity) {
        var burnTime = getProjectile().getBurnSeconds();
        if (burnTime > 0) {
            entity.igniteForSeconds(burnTime);
        }
    }

    protected void damageEntity(ExtendedEntityRayTraceResult hitResult) {
        var entity = hitResult.getEntity();
        var damage = this.getDamage();
        var criticalDamage = this.getCriticalDamage(this.weapon, this.random, damage);
        var isCritical = damage != criticalDamage;

        if (hitResult.isHeadshot()) criticalDamage *= Config.COMMON.gameplay.headShotDamageMultiplier.get();

        entity.hurt(getDamageSource(), criticalDamage);
        sendEntityHitMessage(entity, hitResult.getLocation(), hitResult.isHeadshot(), isCritical);
    }

    protected void onContact(Vec3 hitVec) {
        if(getProjectile().getExplosion().isExplodeOnContact() && ExplosionUtils.isExplosive(getProjectile().getExplosion())){
            // SABLE: explosions must be centered on the GLOBAL position — if hitVec came from a
            // sub-level-local block hit, an un-projected position would blow up the wrong spot
            // (somewhere in the plotgrid instead of where the player actually sees the impact).
            var globalHitVec = SableSupport.toGlobal(this.level(), hitVec);
            ExplosionUtils.createExplosion(this, getProjectile().getExplosion(), globalHitVec);
            this.remove(RemovalReason.KILLED);
        }
    }

    protected void handleBlockBreaking(BlockPos pos, BlockState state) {
        if (ModTags.isFragile(state)) {
            float destroySpeed = state.getDestroySpeed(this.level(), pos);
            if (destroySpeed >= 0) {
                float chance = Config.COMMON.gameplay.griefing.fragileBaseBreakChance.get().floatValue() / (destroySpeed + 1);
                if (this.random.nextFloat() < chance) {
                    this.level().destroyBlock(pos, Config.COMMON.gameplay.griefing.fragileBlockDrops.get());
                }
            }
        }
    }

    protected void updateHeading() {
        double horizontalDistance = this.getDeltaMovement().horizontalDistance();
        this.setYRot((float) (Mth.atan2(this.getDeltaMovement().x(), this.getDeltaMovement().z()) * (180D / Math.PI)));
        this.setXRot((float) (Mth.atan2(this.getDeltaMovement().y(), horizontalDistance) * (180D / Math.PI)));
        this.yRotO = this.getYRot();
        this.xRotO = this.getXRot();
    }

    public static BlockHitResult rayTraceBlocks(Level level, ClipContext context, Predicate<BlockState> ignorePredicate) {
        return RayTraceHelper.performRayTrace(context, (rayTraceContext, blockPos) -> {
            var blockState = level.getBlockState(blockPos);
            if (ignorePredicate.test(blockState)) return null;

            var fluidState = level.getFluidState(blockPos);
            var startVec = rayTraceContext.getFrom();
            var endVec = rayTraceContext.getTo();
            var blockShape = rayTraceContext.getBlockShape(blockState, level, blockPos);
            var blockResult = level.clipWithInteractionOverride(startVec, endVec, blockPos, blockShape, blockState);
            var fluidShape = rayTraceContext.getFluidShape(fluidState, level, blockPos);
            var fluidResult = fluidShape.clip(startVec, endVec, blockPos);

            var blockDistance = blockResult == null ? Double.MAX_VALUE : rayTraceContext.getFrom().distanceToSqr(blockResult.getLocation());
            var fluidDistance = fluidResult == null ? Double.MAX_VALUE : rayTraceContext.getFrom().distanceToSqr(fluidResult.getLocation());

            return blockDistance <= fluidDistance ? blockResult : fluidResult;

        }, (rayTraceContext) -> {
            Vec3 Vector3d = rayTraceContext.getFrom().subtract(rayTraceContext.getTo());
            return BlockHitResult.miss(rayTraceContext.getTo(), Direction.getNearest(Vector3d.x, Vector3d.y, Vector3d.z), BlockPos.containing(rayTraceContext.getTo()));
        });
    }

    private void setupStartPosition(LivingEntity shooter) {
        var posX = shooter.xOld + (shooter.getX() - shooter.xOld) / 2.0;
        var posY = shooter.yOld + (shooter.getY() - shooter.yOld) / 2.0 + shooter.getEyeHeight();
        var posZ = shooter.zOld + (shooter.getZ() - shooter.zOld) / 2.0;
        this.setPos(posX, posY, posZ);
    }

    private ItemStack setupAmmo(WeaponData data) {
        var ammoHolder = WeaponStateHelper.getCurrentAmmo(data);
        if(ammoHolder.canReturnAmmo()) {
            var ammo = BuiltInRegistries.ITEM.get(ammoHolder.getId());
            return new ItemStack(ammo);
        }
        return ItemStack.EMPTY;
    }

    protected void setupDirection(LivingEntity shooter, ItemStack weapon, IWeapon item) {
        var dir = this.getDirection(shooter, weapon, item);
        var speed = this.getProjectile().getSpeed();
        this.setDeltaMovement(dir.x * speed, dir.y * speed, dir.z * speed);
        this.updateHeading();
    }

    protected float getCriticalDamage(ItemStack weapon, RandomSource rand, float damage) {
        var data = new WeaponData(weapon, owner);
        float chance = WeaponModifierHelper.getCriticalChance(data);
        if (rand.nextFloat() < chance) {
            return (float) (damage * Config.COMMON.gameplay.criticalDamageMultiplier.get() * this.criticalDamageMultiplier);
        }
        return damage;
    }

    protected Vec3 getDirection(LivingEntity shooter, ItemStack weapon, IWeapon item) {
        var data = new WeaponData(weapon, shooter);
        float gunSpread = WeaponModifierHelper.getSpread(data);

        if (gunSpread == 0F) {
            return this.getViewVector(shooter.getXRot(), shooter.getYRot());
        }

        if (!WeaponModifierHelper.isAlwaysSpread(data)) {
            gunSpread *= SpreadTracker.get(shooter).getSpread(item);
        }

        if (ModSyncedDataKeys.AIMING.getValue(shooter)) {
            gunSpread *= 0.5F;
        }

        return this.getViewVector(
                shooter.getXRot() - (gunSpread / 2.0F) + random.nextFloat() * gunSpread,
                shooter.getYHeadRot() - (gunSpread / 2.0F) + random.nextFloat() * gunSpread);
    }

    private @NotNull DamageSource getDamageSource() {
        return new DamageSource(getProvider()
                .registryOrThrow(Registries.DAMAGE_TYPE)
                .getHolderOrThrow(getProjectile().getDamageType()), owner);
    }

    private void sendEntityHitMessage(Entity entity, Vec3 hitVec, boolean headshot, boolean isCritical) {
        if (this.owner instanceof ServerPlayer playerShooter) {
            var bodyHitType = headshot ? S2CMessageProjectileHitEntity.HitType.HEADSHOT : S2CMessageProjectileHitEntity.HitType.NORMAL;
            var hitType = isCritical ? S2CMessageProjectileHitEntity.HitType.CRITICAL : bodyHitType;

            // SABLE: project to global before sending — the client's world renders the target
            // at its global position, so the hit-marker position must match that, not the raw
            // (possibly plot-local) hitVec.
            var globalHitVec = SableSupport.toGlobal(this.level(), hitVec);

            PacketHandler.getPlayChannel().sendToPlayer(() -> playerShooter,
                    new S2CMessageProjectileHitEntity(globalHitVec.x, globalHitVec.y, globalHitVec.z, hitType, entity instanceof Player));
        }
    }

    private void sendHitBlockMessage(BlockHitResult hitResult, Vec3 hitVec) {
        var blockPos = hitResult.getBlockPos();

        // SABLE: hitVec must be GLOBAL (so the particle/decal appears where the player visually
        // sees the sub-level). blockPos must stay LOCAL — the real block data (and therefore its
        // texture/BlockState) only exists at the local plot-grid coordinate; converting it to
        // global here would make the client's getBlockState(blockPos) resolve to air, which is
        // exactly what caused block-hit particles to render with no texture.
        var globalHitVec = SableSupport.toGlobal(this.level(), hitVec);

        var message = new S2CMessageProjectileHitBlock(globalHitVec, blockPos, hitResult.getDirection());
        PacketHandler.getPlayChannel()
                .sendToTrackingChunk(() -> level().getChunkAt(blockPos), message);
    }

    @Nullable
    private EntityResult getHitResult(Entity target, Vec3 startVec, Vec3 endVec) {
        var expandHeight = target instanceof Player && !target.isCrouching() ? 0.0625 : 0.0;
        var boundingBox = target.getBoundingBox();

        if (Config.COMMON.gameplay.improvedHitboxes.get()
                && target instanceof ServerPlayer targetPlayer
                && owner instanceof ServerPlayer shooterPlayer) {
            int latency = shooterPlayer.connection.latency();
            int ping = (int) Math.floor((latency / 1000.0) * 20.0 + 0.5);
            boundingBox = BoundingBoxManager.getBoundingBox(targetPlayer, ping);
        }

        boundingBox = boundingBox.expandTowards(0, expandHeight, 0);

        var hitPos = boundingBox.clip(startVec, endVec).orElse(null);
        var grownHitPos = boundingBox.inflate(Config.COMMON.gameplay.growBoundingBoxAmount.get(), 0,
                Config.COMMON.gameplay.growBoundingBoxAmount.get()).clip(startVec, endVec).orElse(null);

        if (hitPos == null && grownHitPos != null) {
            var clipContext = new ClipContext(startVec, grownHitPos, ClipContext.Block.COLLIDER, ClipContext.Fluid.ANY, this);
            var rayTraceResult = rayTraceBlocks(this.level(), clipContext, getBlockFilter());

            if (rayTraceResult.getType() == HitResult.Type.BLOCK)
                return null;

            hitPos = grownHitPos;
        }

        boolean headshot = false;
        if (Config.COMMON.gameplay.enableHeadShots.get() && target instanceof LivingEntity livingEntity) {
            var headshotBox = (IHeadshotBox<LivingEntity>) BoundingBoxManager.getHeadshotBoxes(target.getType());

            if (headshotBox != null) {
                var box = headshotBox.getHeadshotBox(livingEntity);

                if (box != null) {
                    box = box.move(boundingBox.getCenter().x, boundingBox.minY, boundingBox.getCenter().z);
                    var headshotHitPos = box.clip(startVec, endVec);

                    if (!headshotHitPos.isPresent()) {
                        box = box.inflate(Config.COMMON.gameplay.growBoundingBoxAmount.get(), 0, Config.COMMON.gameplay.growBoundingBoxAmount.get());
                        headshotHitPos = box.clip(startVec, endVec);
                    }

                    if (headshotHitPos.isPresent() && (hitPos == null || headshotHitPos.get().distanceTo(hitPos) < 0.5)) {
                        hitPos = headshotHitPos.get();
                        headshot = true;
                    }
                }
            }
        }

        if (hitPos == null)
            return null;

        return new EntityResult(target, hitPos, headshot);
    }

    private Vec3 getViewVector(float pitch, float yaw) {
        float f = Mth.cos(-yaw * 0.017453292F - (float) Math.PI);
        float f1 = Mth.sin(-yaw * 0.017453292F - (float) Math.PI);
        float f2 = -Mth.cos(-pitch * 0.017453292F);
        float f3 = Mth.sin(-pitch * 0.017453292F);
        return new Vec3(f1 * f2, f3, f * f2);
    }

    private void checkDamageable(BlockState state, BlockPos blockPos) {
        if (state.getBlock() instanceof IDamageable damageable) {
            damageable.onBlockDamaged(this, state, blockPos, this.getDamage());
        }
    }

    private void checkBellBlock(BlockHitResult blockHitResult, Block block, BlockPos blockPos) {
        if (block instanceof BellBlock bell) {
            bell.attemptToRing(this.level(), blockPos, blockHitResult.getDirection());
        }
    }

    private void checkTargetBlock(BlockHitResult blockHitResult, BlockState state) {
        if (state.getBlock() instanceof TargetBlock) {
            int power = TargetBlock.updateRedstoneOutput(this.level(), state, blockHitResult, this);
            if (this.owner instanceof ServerPlayer serverPlayer) {
                serverPlayer.awardStat(Stats.TARGET_HIT);
                CriteriaTriggers.TARGET_BLOCK_HIT.trigger(serverPlayer, this, blockHitResult.getLocation(), power);
            }
        }
    }

    private static @NotNull <T> EntityDataAccessor<T> getDataAccessor(EntityDataSerializer<T> serializer) {
        return SynchedEntityData.defineId(ProjectileEntity.class, serializer);
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {}

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return cache;
    }
}
