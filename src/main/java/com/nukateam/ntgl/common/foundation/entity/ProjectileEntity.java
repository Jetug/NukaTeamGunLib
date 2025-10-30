package com.nukateam.ntgl.common.foundation.entity;

import com.mrcrayfish.framework.api.network.LevelLocation;
import com.nukateam.ntgl.Ntgl;
import com.nukateam.ntgl.common.data.WeaponData;
import com.nukateam.ntgl.common.data.config.weapon.ProjectileConfig;
import com.nukateam.ntgl.Config;
import com.nukateam.ntgl.common.data.config.weapon.General;
import com.nukateam.ntgl.common.data.holders.WeaponMode;
import com.nukateam.ntgl.common.foundation.init.NtglDamageTypes;
import com.nukateam.ntgl.common.foundation.item.interfaces.IWeapon;
import com.nukateam.ntgl.common.util.helpers.compatibility.SubtleEffectsHelper;
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
import com.nukateam.ntgl.common.network.message.*;
import com.nukateam.ntgl.modules.enchantment.GunEnchantmentHelper;
import com.nukateam.ntgl.modules.enchantment.ModEnchantments;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.stats.Stats;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.*;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.*;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.entity.IEntityAdditionalSpawnData;
import net.minecraftforge.network.NetworkHooks;
import net.minecraftforge.registries.ForgeRegistries;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Predicate;

public class ProjectileEntity extends Entity implements IEntityAdditionalSpawnData {
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
    protected float additionalDamage = 0.0F;
    protected EntityDimensions entitySize;
    protected double modifiedGravity;
    protected int life;

    public ProjectileEntity(EntityType<? extends Entity> entityType, Level worldIn) {
        super(entityType, worldIn);
    }

    public ProjectileEntity(EntityType<? extends Entity> entityType, Level level, WeaponData data) {
        this(entityType, level);

        var item = (IWeapon) data.weapon.getItem();
        var weaponStack = data.weapon;

        this.weaponData = data;
        this.shooter = data.wielder;
        this.weaponAction = data.weaponAction;
        this.shooterId = shooter.getId();
        this.weapon = weaponStack;
        this.general = WeaponModifierHelper.getGeneral(data);
        this.projectile = WeaponStateHelper.getProjectileConfig(data);
        this.entitySize = new EntityDimensions(this.projectile.getSize(), this.projectile.getSize(), false);
        setBoundingBox(new AABB(
                projectile.getSize(), projectile.getSize(), projectile.getSize(),
                -projectile.getSize(), -projectile.getSize(), -projectile.getSize()));
        this.modifiedGravity = WeaponModifierHelper.getModifiedProjectileGravity(data, -0.04);
        this.life = WeaponModifierHelper.getModifiedProjectileLife(data, this.projectile.getLife());
        var hand = shooter.getMainHandItem() == weaponStack ? InteractionHand.MAIN_HAND : InteractionHand.OFF_HAND;
        this.isRightHand = hand == InteractionHand.MAIN_HAND;
        this.ammo = setupAmmo(data);

        var dir = this.getDirection(shooter, weaponStack, item);
        var speedModifier = GunEnchantmentHelper.getProjectileSpeedModifier(weaponStack);
        var speed = WeaponModifierHelper.getModifiedProjectileSpeed(data, this.projectile.getSpeed() * speedModifier);

        this.setDeltaMovement(dir.x * speed, dir.y * speed, dir.z * speed);
        this.updateHeading();
        this.setupDirection(shooter, weaponStack, item);
        this.setupStartPosition(shooter);
    }

    @Override
    protected void defineSynchedData() {}

    @Override
    protected void addAdditionalSaveData(CompoundTag compound) {
        compound.put("Weapon", weapon.save(new CompoundTag()));
        compound.putString("WeaponAction", weaponAction.toString());
        compound.put("Ammo", ammo.save(new CompoundTag()));
        compound.put("Projectile", this.projectile.serializeNBT());
        compound.put("General", this.general.serializeNBT());
        compound.putDouble("ModifiedGravity", this.modifiedGravity);
        compound.putInt("MaxLife", this.life);
        compound.putBoolean("IsRightHand", this.isRightHand);
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag compound) {
        this.weapon = ItemStack.of(compound.getCompound("Weapon"));
        this.weaponAction = WeaponMode.getType(compound.getString("WeaponAction"));
        this.ammo = ItemStack.of(compound.getCompound("Ammo"));
        this.projectile = ProjectileConfig.create(compound.getCompound("Projectile"));
        this.general = General.create(compound.getCompound("General"));
        this.modifiedGravity = compound.getDouble("ModifiedGravity");
        this.life = compound.getInt("MaxLife");
        this.isRightHand = compound.getBoolean("IsRightHand");
    }

    @Override
    public void writeSpawnData(FriendlyByteBuf buffer) {
        buffer.writeNbt(this.projectile.serializeNBT());
        buffer.writeNbt(this.general.serializeNBT());
        buffer.writeInt(this.shooterId);
        BufferUtil.writeItemStackToBufIgnoreTag(buffer, this.ammo);
        buffer.writeDouble(this.modifiedGravity);
        buffer.writeVarInt(this.life);
        buffer.writeBoolean(this.isRightHand);
        buffer.writeUtf(this.weaponAction.toString());
    }

    @Override
    public void readSpawnData(FriendlyByteBuf buffer) {
        this.projectile = ProjectileConfig.create(buffer.readNbt());
        this.general = General.create(buffer.readNbt());
        this.shooterId = buffer.readInt();
        this.ammo = BufferUtil.readItemStackFromBufIgnoreTag(buffer);
        this.modifiedGravity = buffer.readDouble();
        this.life = buffer.readVarInt();
        this.isRightHand = buffer.readBoolean();
        this.weaponAction = WeaponMode.getType(buffer.readUtf());

        this.entitySize = new EntityDimensions(this.projectile.getSize(), this.projectile.getSize(), false);
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

    @Override
    public void onRemovedFromWorld() {
        if (!this.level().isClientSide) {
            PacketHandler.getPlayChannel().sendToNearbyPlayers(this::getDeathTargetPoint, new S2CMessageRemoveProjectile(this.getId()));
        }
    }

    @Override
    public Packet<ClientGamePacketListener> getAddEntityPacket() {
        return NetworkHooks.getEntitySpawningPacket(this);
    }


    protected double getGravity(){
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

    public void setAdditionalDamage(float additionalDamage) {
        this.additionalDamage = additionalDamage;
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

        float initialDamage = WeaponModifierHelper.getModifiedDamage(data)  + this.additionalDamage;

        if (this.projectile.isDamageReduceOverLife()) {
            float modifier = ((float) this.projectile.getLife() - (float) (this.tickCount - 1)) / (float) this.projectile.getLife();
            initialDamage *= modifier;
        }

        var projectileAmount = WeaponModifierHelper.getProjectileAmount(data);
        var damage = initialDamage / projectileAmount;
        damage = GunEnchantmentHelper.getAcceleratorDamage(this.weapon, damage);

        return Math.max(0F, damage);
    }

    @Override
    public void tick() {
        super.tick();
        this.updateHeading();
        this.onProjectileTick();

        if (shooter != null) {
            if (isServerSide) {
                var startVec = this.position();
                var endVec = startVec.add(this.getDeltaMovement());
                HitResult result = rayTraceBlocks(this.level(), new ClipContext(startVec, endVec, ClipContext.Block.COLLIDER, ClipContext.Fluid.ANY, this), getBlockFilter());

                if (result.getType() != HitResult.Type.MISS) {
                    if (!(result instanceof BlockHitResult bhr && !level().getBlockState(bhr.getBlockPos()).getFluidState().isEmpty())) {
                        endVec = result.getLocation();
                    }
                }

                var hitEntities = getHitEntityResult(startVec, endVec);

                if (hitEntities != null && !hitEntities.isEmpty()) {
                    for (var entityResult : hitEntities) {
                        result = new ExtendedEntityRayTraceResult(entityResult);

                        if (((EntityHitResult) result).getEntity() instanceof Player playerTarget) {
                            if (this.shooter instanceof Player playerShooter && !playerShooter.canHarmPlayer(playerTarget)) {
                                result = null;
                            }
                        }
                        if (result != null) {
                            this.onHit(result, startVec, endVec);
                        }
                    }
                } else {
                    this.onHit(result, startVec, endVec);
                }
            }

            double nextPosX = this.getX() + this.getDeltaMovement().x();
            double nextPosY = this.getY() + this.getDeltaMovement().y();
            double nextPosZ = this.getZ() + this.getDeltaMovement().z();

            this.setPos(nextPosX, nextPosY, nextPosZ);

            if (this.projectile.isGravity() || isAffectedByFluid()) {
                this.setDeltaMovement(this.getDeltaMovement().add(0, this.getGravity(), 0));
            }

            if (isAffectedByFluid()) {
                var motion = this.getDeltaMovement();
                double drag = this.getFluidDrag();
                this.setDeltaMovement(motion.x * drag, motion.y * drag, motion.z * drag);
            }
        }

        if (this.tickCount >= this.life) {
            if (this.isAlive()) {
                this.onExpired();
            }
            this.remove(RemovalReason.KILLED);
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
        int level = EnchantmentHelper.getItemEnchantmentLevel(ModEnchantments.COLLATERAL.get(), this.weapon);

        if (level == 0) {
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

    protected void doImpactEffects(Vec3 hitVec) {}

    protected boolean removeOnHit() {
        return true;
    }

    @Nullable
    protected EntityResult findEntityOnPath(LivingEntity shooter, Vec3 startVec, Vec3 endVec) {
        Vec3 hitVec = null;
        Entity hitEntity = null;
        var headshot = false;
        var entities = this.level().getEntities(this,
                this.getBoundingBox()
                        .expandTowards(this.getDeltaMovement())
                        .inflate(1.0), (entity) -> PROJECTILE_TARGETS.test(entity) && shooter.getVehicle() != entity);

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
        if (MinecraftForge.EVENT_BUS.post(new GunProjectileHitEvent(result, this))) {
            return;
        }

        if (result instanceof BlockHitResult blockHitResult) {
            if (blockHitResult.getType() == HitResult.Type.MISS) {
                return;
            }

            var hitVec = result.getLocation();
            var pos = blockHitResult.getBlockPos();
            var state = this.level().getBlockState(pos);
            var block = state.getBlock();
            var inFluid = false;

            if (!state.getFluidState().isEmpty()) {
                this.onHitFluid(blockHitResult);
                inFluid = true;
            }

            handleBlockBreaking(pos, state);

            if (!inFluid && !state.canBeReplaced() && removeOnHit()) {
                this.remove(RemovalReason.KILLED);
            }

            if (block instanceof IDamageable) {
                ((IDamageable) block).onBlockDamaged(this.level(), state, pos, this, this.getDamage(), (int) Math.ceil(this.getDamage() / 2.0) + 1);
            }

            this.onHitBlock(state, blockHitResult, hitVec);

            if (block instanceof TargetBlock targetBlock) {
                int power = ReflectionUtil.updateTargetBlock(targetBlock, this.level(), state, blockHitResult, this);
                if (this.shooter instanceof ServerPlayer serverPlayer) {
                    serverPlayer.awardStat(Stats.TARGET_HIT);
                    CriteriaTriggers.TARGET_BLOCK_HIT.trigger(serverPlayer, this, blockHitResult.getLocation(), power);
                }
            }

            if (block instanceof BellBlock bell) {
                bell.attemptToRing(this.level(), pos, blockHitResult.getDirection());
            }
            return;
        }

        if (result instanceof ExtendedEntityRayTraceResult entityHitResult) {
            var entity = entityHitResult.getEntity();
            if (entity.getId() == this.shooter.getId()) {
                return;
            }

            if (this.shooter instanceof Player player) {
                if (entity.hasIndirectPassenger(player)) {
                    return;
                }
            }

            int fireStarterLevel = EnchantmentHelper.getItemEnchantmentLevel(ModEnchantments.FIRE_STARTER.get(), this.weapon);
            if (fireStarterLevel > 0) {
                entity.setSecondsOnFire(2);
            }

            this.onHitEntity(entity, result.getLocation(), startVec, endVec, entityHitResult.isHeadshot());

            int collateralLevel = EnchantmentHelper.getItemEnchantmentLevel(ModEnchantments.COLLATERAL.get(), weapon);
            if (collateralLevel == 0 && removeOnHit()) {
                this.remove(RemovalReason.KILLED);
            }

            entity.invulnerableTime = 0;
        }
    }

    protected void onHitFluid(BlockHitResult hitResult) {
        var pos = hitResult.getLocation();

        PacketHandler.getPlayChannel().sendToNearbyPlayers(
                () -> LevelLocation.create(level(), pos, 32),
                new S2CMessageProjectileHitFluid(pos, this.getId()));
    }

    @Override
    protected void doWaterSplashEffect() {
        super.doWaterSplashEffect();
    }

    public void doSplashEffect(Vec3 pos) {
        if(Ntgl.subtleEffectsLoaded && SubtleEffectsHelper.doSplashEffect(this))
            return;

        if (!this.wasTouchingWater) {
            wasTouchingWater = true;
            if(isInWater()) {
                doWaterSplashEffect(pos);
            }
        }
    }

    private void doWaterSplashEffect(Vec3 pos) {
        var velocity = this.getDeltaMovement();
        playSplashSound(velocity);
        var waterLevelY = Mth.floor(pos.y);

        for(int i = 0; i < 1.0F + this.getBbWidth() * 20.0F; ++i) {
            var offsetX = (this.random.nextDouble() * 2.0D - 1.0D) * this.getBbWidth();
            var offsetZ  = (this.random.nextDouble() * 2.0D - 1.0D) * this.getBbWidth();
            this.level().addParticle(
                    ParticleTypes.BUBBLE,
                    pos.x + offsetX,
                    waterLevelY + 1.0F,
                    pos.z + offsetZ,
                    velocity.x,
                    velocity.y - this.random.nextDouble() * 0.2D,
                    velocity.z
            );
        }

        for(int j = 0; j < 1.0F + this.getBbWidth() * 20.0F; ++j) {
            var offsetX = (this.random.nextDouble() * 2.0D - 1.0D) * this.getBbWidth();
            var offsetZ = (this.random.nextDouble() * 2.0D - 1.0D) * this.getBbWidth();

            this.level().addParticle(
                    ParticleTypes.SPLASH,
                    pos.x + offsetX,
                    waterLevelY + 1.0F,
                    pos.z + offsetZ,
                    velocity.x,
                    velocity.y,
                    velocity.z
            );
        }

        this.gameEvent(GameEvent.SPLASH);
    }

    private void playSplashSound(Vec3 velocity) {
        var volumeModifier = 0.2F;
        var splashStrength = (float) Math.sqrt(
                velocity.x * velocity.x * 0.2D +
                        velocity.y * velocity.y +
                        velocity.z * velocity.z * 0.2D
        ) * volumeModifier;
        splashStrength = Math.min(1.0F, splashStrength);

        var pitch = 1.0F + (this.random.nextFloat() - this.random.nextFloat()) * 0.4F;
        if (splashStrength < 0.25F) {
            this.playSound(this.getSwimSplashSound(), splashStrength, pitch);
        } else {
            this.playSound(this.getSwimHighSpeedSplashSound(), splashStrength, pitch);
        }
    }

    protected void onHitEntity(Entity entity, Vec3 hitVec, Vec3 startVec, Vec3 endVec, boolean headshot) {
        var damage = this.getDamage();
        var newDamage = this.getCriticalDamage(this.weapon, this.random, damage);
        var critical = damage != newDamage;
        damage = newDamage;

        if (headshot) damage *= Config.COMMON.gameplay.headShotDamageMultiplier.get();

        var source = NtglDamageTypes.Sources.source(this.level().registryAccess(), projectile.getDamageType(),this, this.shooter);
        entity.hurt(source, damage);

        if (this.shooter instanceof ServerPlayer playerShooter) {
            var bodyHitType = headshot ? S2CMessageProjectileHitEntity.HitType.HEADSHOT : S2CMessageProjectileHitEntity.HitType.NORMAL;
            var hitType = critical ? S2CMessageProjectileHitEntity.HitType.CRITICAL : bodyHitType;

            PacketHandler.getPlayChannel().sendToPlayer(() -> playerShooter,
                    new S2CMessageProjectileHitEntity(hitVec.x, hitVec.y, hitVec.z, hitType, entity instanceof Player));
        }

        PacketHandler.getPlayChannel().sendToTracking(() -> entity, new S2CMessageBlood(hitVec.x, hitVec.y, hitVec.z));

        doImpactEffects(hitVec);
        onContact(hitVec);
    }

    protected void onHitBlock(BlockState state, BlockHitResult hitResult, Vec3 hitVec) {
        var blockPos = hitResult.getBlockPos();
        var dir = hitResult.getDirection();
        var channel = PacketHandler.getPlayChannel();
        if(state.getFluidState().isEmpty()) {
            channel.sendToTrackingChunk(() -> this.level().getChunkAt(blockPos),
                    new S2CMessageProjectileHitBlock(hitVec, blockPos, dir));
            doImpactEffects(hitVec);
            onContact(hitVec);
        }
    }

    protected void onContact(Vec3 hitVec) {
        if(projectile.getExplosion().isExplodeOnContact() && ExplosionUtils.isExplosive(projectile.getExplosion())){
            ExplosionUtils.createExplosion(this, projectile.getExplosion(), hitVec);
        }
    }

    protected boolean handleBlockBreaking(BlockPos pos, BlockState state) {
        if (ModTags.isFragile(state)) {
            float destroySpeed = state.getDestroySpeed(this.level(), pos);
            if (destroySpeed >= 0) {
                float chance = Config.COMMON.gameplay.griefing.fragileBaseBreakChance.get().floatValue() / (destroySpeed + 1);
                if (this.random.nextFloat() < chance) {
                    this.level().destroyBlock(pos, Config.COMMON.gameplay.griefing.fragileBlockDrops.get());
                    return true;
                }
            }
        }
        return false;
    }

    protected void updateHeading() {
        double horizontalDistance = this.getDeltaMovement().horizontalDistance();
        this.setYRot((float) (Mth.atan2(this.getDeltaMovement().x(), this.getDeltaMovement().z()) * (180D / Math.PI)));
        this.setXRot((float) (Mth.atan2(this.getDeltaMovement().y(), horizontalDistance) * (180D / Math.PI)));
        this.yRotO = this.getYRot();
        this.xRotO = this.getXRot();
    }

    protected static BlockHitResult rayTraceBlocks(Level level, ClipContext context, Predicate<BlockState> ignorePredicate) {
        return performRayTrace(context, (rayTraceContext, blockPos) -> {
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

    private LevelLocation getDeathTargetPoint() {
        return LevelLocation.create(this.level(), this.getX(), this.getY(), this.getZ(), 256);
    }

    private static <T> T performRayTrace(ClipContext context, BiFunction<ClipContext, BlockPos, T> hitFunction, Function<ClipContext, T> onFinish) {
        var startVec = context.getFrom();
        var endVec = context.getTo();

        if (!startVec.equals(endVec)) {
            var startX = Mth.lerp(-0.0000001, endVec.x, startVec.x);
            var startY = Mth.lerp(-0.0000001, endVec.y, startVec.y);
            var startZ = Mth.lerp(-0.0000001, endVec.z, startVec.z);

            var endX = Mth.lerp(-0.0000001, startVec.x, endVec.x);
            var endY = Mth.lerp(-0.0000001, startVec.y, endVec.y);
            var endZ = Mth.lerp(-0.0000001, startVec.z, endVec.z);

            var blockX = Mth.floor(endX);
            var blockY = Mth.floor(endY);
            var blockZ = Mth.floor(endZ);

            var mutablePos = new BlockPos.MutableBlockPos(blockX, blockY, blockZ);
            T t = hitFunction.apply(context, mutablePos);

            if (t != null) return t;

            double deltaX = startX - endX;
            double deltaY = startY - endY;
            double deltaZ = startZ - endZ;

            int signX = Mth.sign(deltaX);
            int signY = Mth.sign(deltaY);
            int signZ = Mth.sign(deltaZ);

            double d9 = signX == 0 ? Double.MAX_VALUE : (double) signX / deltaX;
            double d10 = signY == 0 ? Double.MAX_VALUE : (double) signY / deltaY;
            double d11 = signZ == 0 ? Double.MAX_VALUE : (double) signZ / deltaZ;
            double d12 = d9 * (signX > 0 ? 1.0D - Mth.frac(endX) : Mth.frac(endX));
            double d13 = d10 * (signY > 0 ? 1.0D - Mth.frac(endY) : Mth.frac(endY));
            double d14 = d11 * (signZ > 0 ? 1.0D - Mth.frac(endZ) : Mth.frac(endZ));

            while (d12 <= 1.0D || d13 <= 1.0D || d14 <= 1.0D) {
                if (d12 < d13) {
                    if (d12 < d14) {
                        blockX += signX;
                        d12 += d9;
                    } else {
                        blockZ += signZ;
                        d14 += d11;
                    }
                } else if (d13 < d14) {
                    blockY += signY;
                    d13 += d10;
                } else {
                    blockZ += signZ;
                    d14 += d11;
                }

                var t1 = hitFunction.apply(context, mutablePos.set(blockX, blockY, blockZ));
                if (t1 != null)
                    return t1;
            }

        }
        return onFinish.apply(context);
    }

    private ItemStack setupAmmo(WeaponData data) {
        var weapon = data.weapon;

        var ammoHolder = WeaponStateHelper.getCurrentAmmo(data);
        if(ammoHolder.canReturnAmmo()) {
            var ammo = ForgeRegistries.ITEMS.getValue(ammoHolder.getId());
            if (ammo != null) {
                int customModelData = -1;
                if (weapon.getTag() != null) {
                    if (weapon.getTag().contains("Model", Tag.TAG_COMPOUND)) {
                        ItemStack model = ItemStack.of(weapon.getTag().getCompound("Model"));
                        if (model.getTag() != null && model.getTag().contains("CustomModelData")) {
                            customModelData = model.getTag().getInt("CustomModelData");
                        }
                    }
                }
                var ammoStack = new ItemStack(ammo);
                if (customModelData != -1) {
                    ammoStack.getOrCreateTag().putInt("CustomModelData", customModelData);
                }
                return ammoStack;
            }
        }
        return ItemStack.EMPTY;
    }

    protected void setupDirection(LivingEntity shooter, ItemStack weapon, IWeapon item) {
        var dir = this.getDirection(shooter, weapon, item);
        var speedModifier = GunEnchantmentHelper.getProjectileSpeedModifier(weapon);
        var data = new WeaponData(weapon, shooter);
        var speed = WeaponModifierHelper.getModifiedProjectileSpeed(data, this.projectile.getSpeed() * speedModifier);
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
        float gunSpread = WeaponModifierHelper.getModifiedSpread(data);

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

    @Nullable
    @SuppressWarnings("unchecked")
    private EntityResult getHitResult(Entity target, Vec3 startVec, Vec3 endVec) {
        var expandHeight = target instanceof Player && !target.isCrouching() ? 0.0625 : 0.0;
        var boundingBox = target.getBoundingBox();

        if (Config.COMMON.gameplay.improvedHitboxes.get()
                && target instanceof ServerPlayer targetPlayer
                && shooter instanceof ServerPlayer shooterPlayer) {
            int ping = (int) Math.floor((shooterPlayer.latency / 1000.0) * 20.0 + 0.5);
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

    public static class EntityResult {
        private final Entity entity;
        private final Vec3 hitVec;
        private final boolean headshot;

        public EntityResult(Entity entity, Vec3 hitVec, boolean headshot) {
            this.entity = entity;
            this.hitVec = hitVec;
            this.headshot = headshot;
        }

        public Entity getEntity() {
            return this.entity;
        }

        public Vec3 getHitPos() {
            return this.hitVec;
        }

        public boolean isHeadshot() {
            return this.headshot;
        }
    }
}
