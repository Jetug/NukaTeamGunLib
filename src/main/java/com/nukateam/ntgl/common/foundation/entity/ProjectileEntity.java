package com.nukateam.ntgl.common.foundation.entity;

import com.mrcrayfish.framework.api.network.LevelLocation;
import com.nukateam.ntgl.common.data.WeaponData;
import com.nukateam.ntgl.common.data.config.weapon.ProjectileConfig;
import com.nukateam.ntgl.Config;
import com.nukateam.ntgl.common.data.config.weapon.General;
import com.nukateam.ntgl.common.data.holders.WeaponMode;
import com.nukateam.ntgl.common.foundation.init.NtglComponents;
import com.nukateam.ntgl.common.foundation.item.interfaces.IWeapon;
import com.nukateam.ntgl.common.network.message.weapon.S2CMessageBlood;
import com.nukateam.ntgl.common.network.message.weapon.S2CMessageProjectileHitBlock;
import com.nukateam.ntgl.common.network.message.weapon.S2CMessageProjectileHitEntity;
import com.nukateam.ntgl.common.network.message.weapon.S2CMessageProjectileHitFluid;
import com.nukateam.ntgl.common.util.helpers.EntityResult;
import com.nukateam.ntgl.common.util.helpers.RayTraceHelper;
import com.nukateam.ntgl.common.util.interfaces.IDamageable;
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
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
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

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Predicate;

public class ProjectileEntity extends Entity{
    protected static final Predicate<Entity> PROJECTILE_TARGETS = input -> input != null && input.isPickable() && !input.isSpectator();
    protected static final Predicate<BlockState> IGNORE_LEAVES = input -> input != null && Config.COMMON.gameplay.ignoreLeaves.get() && input.getBlock() instanceof LeavesBlock;

    protected WeaponMode weaponAction;
    protected WeaponData weaponData;
    protected boolean isServerSide = !level().isClientSide();
    protected boolean isRightHand;
    protected int shooterId;
    protected LivingEntity shooter;
    protected General general;
    protected ProjectileConfig projectile = new ProjectileConfig();
    protected ItemStack weapon = ItemStack.EMPTY;
    protected ItemStack ammo = ItemStack.EMPTY;
    protected EntityDimensions entitySize;
    protected double modifiedGravity;
    protected int life;
    protected int pierceCount;

    public ProjectileEntity(EntityType<? extends Entity> entityType, Level worldIn) {
        super(entityType, worldIn);
    }

    public ProjectileEntity(EntityType<? extends Entity> entityType, Level level, WeaponData data) {
        this(entityType, level);

        var item = (IWeapon) data.weapon.getItem();
        var weaponStack = data.weapon;

        this.weaponData = data;
        this.shooter = data.wielder;
        this.weaponAction = data.weaponMode;
        this.shooterId = shooter.getId();
        this.weapon = weaponStack;
        this.general = WeaponModifierHelper.getGeneral(data);
        this.projectile = WeaponStateHelper.getProjectileConfig(data);
        this.entitySize = EntityDimensions.fixed(this.projectile.getSize(), this.projectile.getSize());
        setBoundingBox(new AABB(
                projectile.getSize(), projectile.getSize(), projectile.getSize(),
                -projectile.getSize(), -projectile.getSize(), -projectile.getSize()));
        this.modifiedGravity = WeaponModifierHelper.getProjectileGravity(data, -0.04);
        this.life = WeaponModifierHelper.getProjectileLife(data, this.projectile.getLife());
        var hand = shooter.getMainHandItem() == weaponStack ? InteractionHand.MAIN_HAND : InteractionHand.OFF_HAND;
        this.isRightHand = hand == InteractionHand.MAIN_HAND;
        this.ammo = setupAmmo(data);
        this.pierceCount = projectile.getPierceLevel();
        var dir = this.getDirection(shooter, weaponStack, item);
        var speed =  this.projectile.getSpeed();

        this.setDeltaMovement(dir.x * speed, dir.y * speed, dir.z * speed);
        this.updateHeading();
        this.setupDirection(shooter, weaponStack, item);
        this.setupStartPosition(shooter);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {}

    @Override
    protected void addAdditionalSaveData(CompoundTag compound) {
        var provider = getProvider();
        compound.put("Weapon", weapon.save(provider, new CompoundTag()));
        compound.putString("WeaponAction", weaponAction.toString());
        compound.put("Ammo", ammo.save(provider, new CompoundTag()));
        compound.put("Projectile", this.projectile.serializeNBT(provider));
        compound.put("General", this.general.serializeNBT(provider));
        compound.putDouble("ModifiedGravity", this.modifiedGravity);
        compound.putInt("MaxLife", this.life);
        compound.putBoolean("IsRightHand", this.isRightHand);
        compound.putInt("ShooterId", this.shooterId);
    }

    private @NotNull RegistryAccess getProvider() {
        return level().registryAccess();
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag compound) {
        var provider = getProvider();
        this.weapon = ItemStack.parseOptional(provider, compound.getCompound("Weapon"));
        this.weaponAction = WeaponMode.getType(compound.getString("WeaponAction"));
        this.ammo = ItemStack.parseOptional(provider, compound.getCompound("Ammo"));
        this.projectile = ProjectileConfig.create(compound.getCompound("Projectile"));
        this.general = General.create(compound.getCompound("General"));
        this.modifiedGravity = compound.getDouble("ModifiedGravity");
        this.life = compound.getInt("MaxLife");
        this.isRightHand = compound.getBoolean("IsRightHand");
        this.shooterId = compound.getInt("ShooterId");

        this.entitySize = EntityDimensions.fixed(this.projectile.getSize(), this.projectile.getSize());
        setBoundingBox(new AABB(
                projectile.getSize(), projectile.getSize(), projectile.getSize(),
                -projectile.getSize(), -projectile.getSize(), -projectile.getSize()));
    }


    @Override
    public EntityDimensions getDimensions(Pose pose) {
        return this.entitySize;
    }

    @Override
    public boolean shouldRenderAtSqrDistance(double distance) {
        return true;
    }

//    @Override
//    public Packet<ClientGamePacketListener> getAddEntityPacket(ServerEntity entity) {
//        return super.getAddEntityPacket(entity);
//    }
//
//    @Override
//    public Packet<ClientGamePacketListener> getAddEntityPacket() {
//        return NetworkHooks.getEntitySpawningPacket(this);
//    }

    @Override
    public void tick() {
        super.tick();
        this.updateHeading();
        this.onProjectileTick();

        if (isServerSide) {
            rayTraceTargets();
        }
        travel();

        if (this.tickCount >= this.life) {
            if (this.isAlive()) {
                this.onExpired();
            }
            this.remove(RemovalReason.KILLED);
        }
    }

    @Override
    protected double getDefaultGravity() {
        return projectile.isGravity() || isAffectedByFluid() ? modifiedGravity : 0.0;
    }

    private boolean isAffectedByFluid() {
        return projectile.affectedByFluid() && this.isInFluidType();
    }

    public boolean isVisible(){
        return projectile.isVisible();
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

    public void setItem(ItemStack item) {
        this.ammo = item;
    }

    public ItemStack getItem() {
        return this.ammo;
    }

    public double getModifiedGravity() {
        return this.modifiedGravity;
    }

    public int getLife(){
        return projectile.getLife();
    }

    public ProjectileConfig getProjectile() {
        return this.projectile;
    }

    public LivingEntity getShooter() {
        return this.shooter;
    }

    public int getShooterId() {
        return this.shooterId;
    }

    public float getDamage() {
        if(weapon.isEmpty())
            return 0;

        var data = new WeaponData(this.weapon, this.shooter);

        float initialDamage = WeaponModifierHelper.getProjectileDamage(BuiltInRegistries.ITEM.getKey(ammo.getItem()), data);

        if (this.projectile.isDamageReduceOverLife()) {
            float modifier = ((float) this.projectile.getLife() - (float) (this.tickCount - 1)) / (float) this.projectile.getLife();
            initialDamage *= modifier;
        }

        var projectileAmount = WeaponModifierHelper.getProjectileAmount(data);
        var damage = initialDamage / projectileAmount;

        return Math.max(0F, damage);
    }

    protected void travel() {
        var nextPosX = this.getX() + this.getDeltaMovement().x();
        var nextPosY = this.getY() + this.getDeltaMovement().y();
        var nextPosZ = this.getZ() + this.getDeltaMovement().z();

        this.setPos(nextPosX, nextPosY, nextPosZ);

        if (this.projectile.isGravity() || isAffectedByFluid()) {
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

        if (result.getType() != HitResult.Type.MISS) {
            if (!(result instanceof BlockHitResult bhr && !level().getBlockState(bhr.getBlockPos()).getFluidState().isEmpty())) {
                endVec = result.getLocation();
            }
        }

        var hitEntities = getHitEntityResult(startVec, endVec);

        if (hitEntities != null && !hitEntities.isEmpty()) {
            for (var hit : hitEntities) {
                var entityHitResult = new ExtendedEntityRayTraceResult(hit);

                if (entityHitResult.getEntity() instanceof Player playerTarget) {
                    if (this.shooter instanceof Player playerShooter && !playerShooter.canHarmPlayer(playerTarget)) {
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
            var entityResult = this.findEntityOnPath(shooter, startVec, endVec);
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
        if(ExplosionUtils.isExplosive(projectile.getExplosion())){
            ExplosionUtils.createExplosion(this, projectile.getExplosion(), position());
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
            if (!target.equals(this.shooter)) {
                var result = this.getHitResult(target, startVec, endVec);
                if (result == null) continue;
                var hitPos = result.getHitPos();
                var distanceToHit = startVec.distanceTo(hitPos);
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
            if (!entity.equals(this.shooter)) {
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

        if (entity.getId() == this.shooter.getId()) return;
        if (this.shooter instanceof Player player && entity.hasIndirectPassenger(player)) return;

        burnEntity(entity);
        damageEntity(entity, entityHitResult);
        PacketHandler.getPlayChannel().sendToTrackingEntity(() -> entity, new S2CMessageBlood(hitVec));
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
        var hitSound = projectile.getHitSound();
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
            PacketHandler.getPlayChannel().sendToNearbyPlayers(
                    () -> LevelLocation.create((ServerLevel) level(), pos, 32),
                    new S2CMessageProjectileHitFluid(
                            pos,
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
        var burnTime = projectile.getBurnSeconds();
        if (burnTime > 0) {
            entity.igniteForSeconds(burnTime);
        }
    }

    private void damageEntity(Entity entity, ExtendedEntityRayTraceResult hitResult) {
        var damage = this.getDamage();
        var criticalDamage = this.getCriticalDamage(this.weapon, this.random, damage);
        var isCritical = damage != criticalDamage;

        if (hitResult.isHeadshot()) criticalDamage *= Config.COMMON.gameplay.headShotDamageMultiplier.get();

        entity.hurt(getDamageSource(), criticalDamage);
        sendEntityHitMessage(entity, hitResult.getLocation(), hitResult.isHeadshot(), isCritical);
    }

    protected void onContact(Vec3 hitVec) {
        if(projectile.getExplosion().isExplodeOnContact() && ExplosionUtils.isExplosive(projectile.getExplosion())){
            ExplosionUtils.createExplosion(this, projectile.getExplosion(), hitVec);
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

    protected static BlockHitResult rayTraceBlocks(Level level, ClipContext context, Predicate<BlockState> ignorePredicate) {
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
        var speed = this.projectile.getSpeed();
        this.setDeltaMovement(dir.x * speed, dir.y * speed, dir.z * speed);
        this.updateHeading();
    }

    protected float getCriticalDamage(ItemStack weapon, RandomSource rand, float damage) {
        var data = new WeaponData(weapon, shooter);
        float chance = WeaponModifierHelper.getCriticalChance(data);
        if (rand.nextFloat() < chance) {
            return (float) (damage * Config.COMMON.gameplay.criticalDamageMultiplier.get());
        }
        return damage;
    }

    protected Vec3 getDirection(LivingEntity shooter, ItemStack weapon, IWeapon item) {
        var data = new WeaponData(weapon, shooter);
        float gunSpread = WeaponModifierHelper.getSpread(data);

        if (gunSpread == 0F) {
            return this.getVectorFromRotation(shooter.getXRot(), shooter.getYRot());
        }

        if (!WeaponModifierHelper.isAlwaysSpread(data)) {
            gunSpread *= SpreadTracker.get(shooter).getSpread(item);
        }

        if (ModSyncedDataKeys.AIMING.getValue(shooter)) {
            gunSpread *= 0.5F;
        }

        return this.getVectorFromRotation(shooter.getXRot() - (gunSpread / 2.0F) + random.nextFloat() * gunSpread, shooter.getYHeadRot() - (gunSpread / 2.0F) + random.nextFloat() * gunSpread);
    }

    private @NotNull DamageSource getDamageSource() {
        return new DamageSource(getProvider()
                .registryOrThrow(Registries.DAMAGE_TYPE)
                .getHolderOrThrow( projectile.getDamageType()));
    }

    private void sendEntityHitMessage(Entity entity, Vec3 hitVec, boolean headshot, boolean isCritical) {
        if (this.shooter instanceof ServerPlayer playerShooter) {
            var bodyHitType = headshot ? S2CMessageProjectileHitEntity.HitType.HEADSHOT : S2CMessageProjectileHitEntity.HitType.NORMAL;
            var hitType = isCritical ? S2CMessageProjectileHitEntity.HitType.CRITICAL : bodyHitType;

            PacketHandler.getPlayChannel().sendToPlayer(() -> playerShooter,
                    new S2CMessageProjectileHitEntity(hitVec.x, hitVec.y, hitVec.z, hitType, entity instanceof Player));
        }
    }

    private void sendHitBlockMessage(BlockHitResult hitResult, Vec3 hitVec) {
        var blockPos = hitResult.getBlockPos();
        var message = new S2CMessageProjectileHitBlock(hitVec, blockPos, hitResult.getDirection());
        PacketHandler.getPlayChannel()
                .sendToTrackingChunk(() -> level().getChunkAt(blockPos), message);
    }

    @Nullable
    private EntityResult getHitResult(Entity target, Vec3 startVec, Vec3 endVec) {
        var expandHeight = target instanceof Player && !target.isCrouching() ? 0.0625 : 0.0;
        var boundingBox = target.getBoundingBox();

        if (Config.COMMON.gameplay.improvedHitboxes.get()
                && target instanceof ServerPlayer targetPlayer
                && shooter instanceof ServerPlayer shooterPlayer) {
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

    private Vec3 getVectorFromRotation(float pitch, float yaw) {
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
        if (state.getBlock() instanceof TargetBlock targetBlock) {
            int power = ReflectionUtil.updateTargetBlock(targetBlock, this.level(), state, blockHitResult, this);
            if (this.shooter instanceof ServerPlayer serverPlayer) {
                serverPlayer.awardStat(Stats.TARGET_HIT);
                CriteriaTriggers.TARGET_BLOCK_HIT.trigger(serverPlayer, this, blockHitResult.getLocation(), power);
            }
        }
    }
}
