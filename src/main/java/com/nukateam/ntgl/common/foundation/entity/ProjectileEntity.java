package com.nukateam.ntgl.common.foundation.entity;

import com.mrcrayfish.framework.api.network.LevelLocation;
import com.nukateam.example.common.data.interfaces.IExplosiveOnHit;
import com.nukateam.ntgl.Config;
import com.nukateam.ntgl.common.base.config.Ammo;
import com.nukateam.ntgl.common.base.config.Gun;
import com.nukateam.ntgl.common.base.utils.BoundingBoxManager;
import com.nukateam.ntgl.common.base.utils.SpreadTracker;
import com.nukateam.ntgl.common.data.interfaces.*;
import com.nukateam.ntgl.common.data.util.*;
import com.nukateam.ntgl.common.data.util.math.ExtendedEntityRayTraceResult;
import com.nukateam.ntgl.common.event.GunProjectileHitEvent;
import com.nukateam.ntgl.common.foundation.ModTags;
import com.nukateam.ntgl.common.foundation.init.*;
import com.nukateam.ntgl.common.foundation.item.GunItem;
import com.nukateam.ntgl.common.foundation.world.ProjectileExplosion;
import com.nukateam.ntgl.common.network.PacketHandler;
import com.nukateam.ntgl.common.network.message.*;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.game.ClientboundExplodePacket;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
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
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.phys.*;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.entity.IEntityAdditionalSpawnData;
import net.minecraftforge.registries.ForgeRegistries;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Predicate;

import static com.nukateam.ntgl.common.network.message.S2CMessageProjectileHitEntity.HitType;
import static net.minecraft.network.syncher.SynchedEntityData.defineId;

public class ProjectileEntity extends Entity implements IEntityAdditionalSpawnData {
    protected static final Predicate<Entity> PROJECTILE_TARGETS = input -> input != null && input.isPickable() && !input.isSpectator();
    protected static final Predicate<BlockState> IGNORE_LEAVES = input -> input != null &&
            Config.COMMON.gameplay.ignoreLeaves.get() && input.getBlock() instanceof LeavesBlock;
    public static final EntityDataAccessor<Integer> SHOOTER = defineId(ProjectileEntity.class, EntityDataSerializers.INT);
    public static final EntityDataAccessor<Integer> LIFE    = defineId(ProjectileEntity.class, EntityDataSerializers.INT);
    public static final EntityDataAccessor<Boolean> IS_RIGHT = defineId(ProjectileEntity.class, EntityDataSerializers.BOOLEAN);
    public static final EntityDataAccessor<Boolean> IS_VISIBLE = defineId(ProjectileEntity.class, EntityDataSerializers.BOOLEAN);
    public static final EntityDataAccessor<String> ITEM = defineId(ProjectileEntity.class, EntityDataSerializers.STRING);

    private boolean hasClientData = false;
    protected boolean isServerSide = !level().isClientSide();
    protected int shooterId;
    protected LivingEntity shooter;
    protected Gun modifiedGun;
    protected Gun.General general;
    protected Ammo projectile;
    protected ItemStack weapon = ItemStack.EMPTY;
    protected ItemStack ammoStack = ItemStack.EMPTY;
    protected float additionalDamage = 0.0F;
    protected EntityDimensions entitySize;
    protected double modifiedGravity;
    protected int life;
    protected boolean isRightHand;

    public ProjectileEntity(EntityType<? extends Entity> entityType, Level worldIn) {
        super(entityType, worldIn);
    }

    public ProjectileEntity(EntityType<? extends Entity> entityType, Level worldIn, LivingEntity shooter,
                            ItemStack weapon, GunItem item, Gun modifiedGun) {
        this(entityType, worldIn);
        this.shooterId = shooter.getId();
        this.shooter = shooter;
        this.modifiedGun = modifiedGun;
        this.general = modifiedGun.getGeneral();
        this.projectile = GunModifierHelper.getCurrentProjectile(weapon);
        this.entitySize = new EntityDimensions(this.projectile.getSize(), this.projectile.getSize(), false);
        this.modifiedGravity = GunModifierHelper.getCurrentProjectile(weapon).isGravity() ? GunModifierHelper.getModifiedProjectileGravity(weapon, -0.04) : 0.0;
        this.life = GunModifierHelper.getModifiedProjectileLife(weapon, this.projectile.getLife());
        this.isRightHand = shooter.getItemInHand(InteractionHand.MAIN_HAND) == weapon;
        this.weapon = weapon;

        getEntityData().set(LIFE    , life);
        getEntityData().set(SHOOTER , shooterId);
        getEntityData().set(IS_RIGHT, isRightHand);
        getEntityData().set(IS_VISIBLE, projectile.isVisible());
        getEntityData().set(ITEM, GunModifierHelper.getCurrentAmmo(weapon).toString());

        /* Get speed and set motion */
        setupDirection(shooter, weapon, item, modifiedGun);

        /* Spawn the ammo half way between the previous and current position */
        var posX = shooter.xOld + (shooter.getX() - shooter.xOld) / 2.0;
        var posY = shooter.yOld + (shooter.getY() - shooter.yOld) / 2.0 + shooter.getEyeHeight();
        var posZ = shooter.zOld + (shooter.getZ() - shooter.zOld) / 2.0;
        this.setPos(posX, posY, posZ);

        var ammo = ForgeRegistries.ITEMS.getValue(GunModifierHelper.getCurrentAmmo(weapon));

        if (ammo != null) {
            int customModelData = -1;
            if (weapon.getTag() != null) {
                if (weapon.getTag().contains("Model", Tag.TAG_COMPOUND)) {
                    var model = ItemStack.of(weapon.getTag().getCompound("Model"));
                    if (model.getTag() != null && model.getTag().contains("CustomModelData")) {
                        customModelData = model.getTag().getInt("CustomModelData");
                    }
                }
            }
            var ammoStack = new ItemStack(ammo);
            if (customModelData != -1) {
                ammoStack.getOrCreateTag().putInt("CustomModelData", customModelData);
            }
            this.ammoStack = ammoStack;
        }
    }

    protected void setupDirection(LivingEntity shooter, ItemStack weapon, GunItem item, Gun modifiedGun) {
        var dir = this.getDirection(shooter, weapon, item, modifiedGun);
        var speedModifier = GunEnchantmentHelper.getProjectileSpeedModifier(weapon);
        var speed = GunModifierHelper.getModifiedProjectileSpeed(weapon, this.projectile.getSpeed() * speedModifier);
        this.setDeltaMovement(dir.x * speed, dir.y * speed, dir.z * speed);
        this.updateHeading();
    }

    @Override
    protected void defineSynchedData() {
        entityData.define(SHOOTER, -1);
        entityData.define(LIFE, 0);
        entityData.define(IS_RIGHT, true);
        entityData.define(IS_VISIBLE, false);
        entityData.define(ITEM, "");
    }

    @Override
    public EntityDimensions getDimensions(Pose pose) {
        return this.entitySize;
    }

    public void setWeapon(ItemStack weapon) {
        this.weapon = weapon.copy();
    }

    public ItemStack getWeapon() {
        return this.weapon;
    }

    public void setItem(ItemStack item) {
        this.ammoStack = item;
    }

    public ItemStack getItem(){
        if (!ammoStack.isEmpty())
            return ammoStack;

        var name = getEntityData().get(ITEM);
        var item = ForgeRegistries.ITEMS.getValue(new ResourceLocation(name));
        return new ItemStack(item);
    }

    public boolean isRightHand() {
        isRightHand = getEntityData().get(IS_RIGHT);
        return isRightHand;
    }

    public boolean isVisible() {
        return getEntityData().get(IS_VISIBLE);
    }

    public void setAdditionalDamage(float additionalDamage) {
        this.additionalDamage = additionalDamage;
    }

    public double getModifiedGravity() {
        return this.modifiedGravity;
    }

    @Override
    public void tick() {
        super.tick();
        this.updateHeading();
        this.onProjectileTick();

        if (shooter == null) {
            return;
        }

        if (isServerSide) {
            Vec3 startVec = this.position();
            Vec3 endVec = startVec.add(this.getDeltaMovement());
            HitResult result = rayTraceBlocks(this.level(), new ClipContext(startVec, endVec, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, this), getBlockFilter());
            if (result.getType() != HitResult.Type.MISS) {
                endVec = result.getLocation();
            }

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

            if (hitEntities != null && hitEntities.size() > 0) {
                for (var entityResult : hitEntities) {
                    result = new ExtendedEntityRayTraceResult(entityResult);
                    if (((EntityHitResult) result).getEntity() instanceof Player) {
                        Player player = (Player) ((EntityHitResult) result).getEntity();

                        if (this.shooter instanceof Player && !((Player) this.shooter).canHarmPlayer(player)) {
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

        if (this.projectile.isGravity()) {
            this.setDeltaMovement(this.getDeltaMovement().add(0, this.modifiedGravity, 0));
        }

        if (this.tickCount >= this.life) {
            if (this.isAlive()) {
                this.onExpired();
            }
            this.remove(RemovalReason.KILLED);
        }
    }

//    private void requestServerData(){
//        if (level().isClientSide)
//            PacketHandler.getPlayChannel().sendToServer(new C2SRequestEntityData(this.getId()));
//    }

    public void updateClient() {
        if(isServerSide) {
            var tag = new CompoundTag();
            addAdditionalSaveData(tag);
            sendS2CData(tag);
        }
    }

    @Override
    public boolean shouldRenderAtSqrDistance(double distance) {
        return true;
    }

    @Override
    public void onRemovedFromWorld() {
        if (isServerSide) {
            PacketHandler.getPlayChannel().sendToNearbyPlayers(this::getDeathTargetPoint, new S2CMessageRemoveProjectile(this.getId()));
        }
    }

    @Override
    public void readAdditionalSaveData(CompoundTag compound) {
        this.projectile = new Ammo();
        this.projectile.deserializeNBT(compound.getCompound("Projectile"));
        this.general = new Gun.General();
        this.general.deserializeNBT(compound.getCompound("General"));
        this.modifiedGravity = compound.getDouble("ModifiedGravity");
        this.life = compound.getInt("MaxLife");
    }

    @Override
    public void addAdditionalSaveData(CompoundTag compound) {
        compound.put("Projectile", this.projectile.serializeNBT());
        compound.put("General", this.general.serializeNBT());
        compound.putDouble("ModifiedGravity", this.modifiedGravity);
        compound.putInt("MaxLife", this.life);
    }

    @Override
    public void writeSpawnData(FriendlyByteBuf buffer) {
        buffer.writeNbt(this.projectile.serializeNBT());
        buffer.writeNbt(this.general.serializeNBT());
        buffer.writeInt(this.shooterId);
        BufferUtil.writeItemStackToBufIgnoreTag(buffer, this.ammoStack);
        buffer.writeDouble(this.modifiedGravity);
        buffer.writeVarInt(this.life);
        buffer.writeBoolean(this.isRightHand);
    }

    @Override
    public void readSpawnData(FriendlyByteBuf buffer) {
        this.projectile = new Ammo();
        this.projectile.deserializeNBT(buffer.readNbt());
        this.general = new Gun.General();
        this.general.deserializeNBT(buffer.readNbt());
        this.shooterId = buffer.readInt();
        this.ammoStack = BufferUtil.readItemStackFromBufIgnoreTag(buffer);
        this.modifiedGravity = buffer.readDouble();
        this.life = buffer.readVarInt();
        this.entitySize = new EntityDimensions(this.projectile.getSize(), this.projectile.getSize(), false);
        this.isRightHand = buffer.readBoolean();
    }

    public void updateHeading() {
        double horizontalDistance = this.getDeltaMovement().horizontalDistance();
        this.setYRot((float) (Mth.atan2(this.getDeltaMovement().x(), this.getDeltaMovement().z()) * (180D / Math.PI)));
        this.setXRot((float) (Mth.atan2(this.getDeltaMovement().y(), horizontalDistance) * (180D / Math.PI)));
        this.yRotO = this.getYRot();
        this.xRotO = this.getXRot();
    }

    public Ammo getProjectile() {
        return this.projectile;
    }

    /**
     * Gets the entity who spawned the ammo
     */
    public LivingEntity getShooter() {
        return this.shooter;
    }

    /**
     * Gets the id of the entity who spawned the ammo
     */
    public int getShooterId() {
        shooterId = getEntityData().get(SHOOTER);
        return shooterId;
    }

    public float getDamage() {
        float initialDamage = (this.projectile.getDamage() + this.additionalDamage);

        if (this.projectile.isDamageReduceOverLife()) {
            float modifier = ((float) this.projectile.getLife() - (float) (this.tickCount - 1)) / (float) this.projectile.getLife();
            initialDamage *= modifier;
        }

        var damage = initialDamage / this.general.getProjectileAmount();
        damage = GunModifierHelper.getModifiedDamage(this.weapon, damage);
        damage = GunEnchantmentHelper.getAcceleratorDamage(this.weapon, damage);

        return Math.max(0F, damage);
    }

    public int getLife(){
        return entityData.get(LIFE);
    }

    /**
     * Creates a ammo explosion for the specified entity.
     *
     * @param entity    The entity to explodeOnHit
     * @param radius    The amount of radius the entity should deal
     * @param forceNone If true, forces the explosion mode to be NONE instead of config value
     */
    public static void createExplosion(Entity entity, float radius, boolean forceNone) {
        var world = entity.level();
        if (world.isClientSide())
            return;

        var source = entity instanceof ProjectileEntity projectile ? entity.damageSources().explosion(entity, projectile.getShooter()) : null;
        var mode = forceNone ? Explosion.BlockInteraction.DESTROY : Explosion.BlockInteraction.KEEP;
        Explosion explosion = new ProjectileExplosion(world, entity, source, null, entity.getX(), entity.getY(), entity.getZ(), radius, false, mode);

        if (net.minecraftforge.event.ForgeEventFactory.onExplosionStart(world, explosion))
            return;

        // Do explosion logic
        explosion.explode();
        explosion.finalizeExplosion(true);

        // Send event to blocks that are exploded (none if mode is none)
        explosion.getToBlow().forEach(pos ->
        {
            if (world.getBlockState(pos).getBlock() instanceof IExplosionDamageable) {
                ((IExplosionDamageable) world.getBlockState(pos).getBlock()).onProjectileExploded(world, world.getBlockState(pos), pos, entity);
            }
        });

        // Clears the affected blocks if mode is none
        if (!explosion.interactsWithBlocks()) {
            explosion.clearToBlow();
        }

        for (ServerPlayer player : ((ServerLevel) world).players()) {
            if (player.distanceToSqr(entity.getX(), entity.getY(), entity.getZ()) < 4096) {
                player.connection.send(new ClientboundExplodePacket(entity.getX(), entity.getY(), entity.getZ(), radius, explosion.getToBlow(), explosion.getHitPlayers().get(player)));
            }
        }
    }

    /**
     * A simple method to perform logic on each tick of the ammo. This method is appropriate
     * for spawning particles. Override {@link #tick()} to make changes to physics
     */
    protected void onProjectileTick() {
    }

    /**
     * Called when the ammo has run out of it's life. In other words, the ammo managed
     * to not hit any blocks and instead aged. The grenade uses this to explodeOnHit in the air.
     */
    protected void onExpired() {
    }

    protected Predicate<BlockState> getBlockFilter() {
        return IGNORE_LEAVES;
    }

    protected boolean removeOnHit() {
        return true;
    }

    protected void doImpactEffects(Vec3 hitVec) {

    }

    @Nullable
    protected EntityResult findEntityOnPath(LivingEntity shooter, Vec3 startVec, Vec3 endVec) {
        Vec3 hitVec = null;
        Entity hitEntity = null;
        boolean headshot = false;
        var entities = this.level().getEntities(this,
                this.getBoundingBox()
                        .expandTowards(this.getDeltaMovement())
                        .inflate(1.0), (entity) -> PROJECTILE_TARGETS.test(entity) && shooter.getVehicle() != entity);

        double closestDistance = Double.MAX_VALUE;
        for (Entity entity : entities) {
            if (!entity.equals(this.shooter)) {
                EntityResult result = this.getHitResult(entity, startVec, endVec);
                if (result == null)
                    continue;
                Vec3 hitPos = result.getHitPos();
                double distanceToHit = startVec.distanceTo(hitPos);
                if (distanceToHit < closestDistance) {
                    hitVec = hitPos;
                    hitEntity = entity;
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

            handleBlockBreaking(pos, state);

            if (!state.canBeReplaced() && removeOnHit()) {
                this.remove(RemovalReason.KILLED);
            }

            if (block instanceof IDamageable) {
                ((IDamageable) block).onBlockDamaged(this.level(), state, pos, this, this.getDamage(), (int) Math.ceil(this.getDamage() / 2.0) + 1);
            }

            this.onHitBlock(state, pos, blockHitResult.getDirection(), hitVec.x, hitVec.y, hitVec.z);

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

            if (block instanceof IExplosiveOnHit explosive) {
                explosive.explodeOnHit(this.level(), blockHitResult.getBlockPos());
            }

//            if (block instanceof LandMineBlock mine) {
//                mine.explodeRand(this.level(), blockHitResult.getBlockPos());
//            }
//
//            if (block instanceof ExplosiveBarrel barrelItem) {
//                barrelItem.explosive(this.level(), pos);
//            }

            int fireStarterLevel = EnchantmentHelper.getItemEnchantmentLevel(ModEnchantments.FIRE_STARTER.get(), this.weapon);
            if (fireStarterLevel > 0 && Config.COMMON.gameplay.griefing.setFireToBlocks.get()) {
                BlockPos offsetPos = pos.relative(blockHitResult.getDirection());
                if (BaseFireBlock.canBePlacedAt(this.level(), offsetPos, blockHitResult.getDirection())) {
                    BlockState fireState = BaseFireBlock.getState(this.level(), offsetPos);
                    this.level().setBlock(offsetPos, fireState, 11);
                    ((ServerLevel) this.level()).sendParticles(ParticleTypes.LAVA, hitVec.x - 1.0 + this.random.nextDouble() * 2.0, hitVec.y, hitVec.z - 1.0 + this.random.nextDouble() * 2.0, 4, 0, 0, 0, 0);
                }
            }
            return;
        }

        if (result instanceof ExtendedEntityRayTraceResult entityHitResult) {
            Entity entity = entityHitResult.getEntity();
            if (entity.getId() == this.shooterId) {
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

    protected void handleBlockBreaking(BlockPos pos, BlockState state) {
        if (Config.COMMON.gameplay.griefing.enableGlassBreaking.get() && state.is(ModTags.Blocks.FRAGILE)) {
            float destroySpeed = state.getDestroySpeed(this.level(), pos);
            if (destroySpeed >= 0) {
                float chance = Config.COMMON.gameplay.griefing.fragileBaseBreakChance.get().floatValue() / (destroySpeed + 1);
                if (this.random.nextFloat() < chance) {
                    this.level().destroyBlock(pos, Config.COMMON.gameplay.griefing.fragileBlockDrops.get());
                }
            }
        }
    }

    protected void onHitEntity(Entity entity, Vec3 hitVec, Vec3 startVec, Vec3 endVec, boolean headshot) {
        var damage = this.getDamage();
        var newDamage = this.getCriticalDamage(this.weapon, this.random, damage);
        var critical = damage != newDamage;
        damage = newDamage;

        if (headshot) damage *= Config.COMMON.gameplay.headShotDamageMultiplier.get();

        var source = ModDamageTypes.Sources.projectile(this.level().registryAccess(), this, this.shooter);
        entity.hurt(source, damage);

        if (this.shooter instanceof Player) {
            var bodyHitType = headshot ? HitType.HEADSHOT : HitType.NORMAL;
            int hitType = critical ? HitType.CRITICAL : bodyHitType;

            PacketHandler.getPlayChannel().sendToPlayer(() -> (ServerPlayer) this.shooter, new S2CMessageProjectileHitEntity(hitVec.x, hitVec.y, hitVec.z, hitType, entity instanceof Player));
        }

        /* Send blood particle to tracking clients. */
        PacketHandler.getPlayChannel().sendToTracking(() -> entity, new S2CMessageBlood(hitVec.x, hitVec.y, hitVec.z));

        doImpactEffects(hitVec);
    }

    protected void onHitBlock(BlockState state, BlockPos pos, Direction face, double x, double y, double z) {
        PacketHandler.getPlayChannel().sendToTrackingChunk(() -> this.level().getChunkAt(pos), new S2CMessageProjectileHitBlock(x, y, z, pos, face));
        doImpactEffects(new Vec3(x, y, z));
    }

    @Nullable
    @SuppressWarnings("unchecked")
    private EntityResult getHitResult(Entity entity, Vec3 startVec, Vec3 endVec) {
        double expandHeight = entity instanceof Player && !entity.isCrouching() ? 0.0625 : 0.0;
        AABB boundingBox = entity.getBoundingBox();
        if (Config.COMMON.gameplay.improvedHitboxes.get() && entity instanceof ServerPlayer && this.shooter != null) {
            int ping = (int) Math.floor((((ServerPlayer) this.shooter).latency / 1000.0) * 20.0 + 0.5);
            boundingBox = BoundingBoxManager.getBoundingBox((Player) entity, ping);
        }
        boundingBox = boundingBox.expandTowards(0, expandHeight, 0);

        Vec3 hitPos = boundingBox.clip(startVec, endVec).orElse(null);
        Vec3 grownHitPos = boundingBox.inflate(Config.COMMON.gameplay.growBoundingBoxAmount.get(), 0, Config.COMMON.gameplay.growBoundingBoxAmount.get()).clip(startVec, endVec).orElse(null);
        if (hitPos == null && grownHitPos != null) {
            HitResult raytraceresult = rayTraceBlocks(this.level(), new ClipContext(startVec, grownHitPos, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, this), getBlockFilter());
            if (raytraceresult.getType() == HitResult.Type.BLOCK) {
                return null;
            }
            hitPos = grownHitPos;
        }

        /* Check for headshot */
        boolean headshot = false;
        if (Config.COMMON.gameplay.enableHeadShots.get() && entity instanceof LivingEntity) {
            IHeadshotBox<LivingEntity> headshotBox = (IHeadshotBox<LivingEntity>) BoundingBoxManager.getHeadshotBoxes(entity.getType());
            if (headshotBox != null) {
                AABB box = headshotBox.getHeadshotBox((LivingEntity) entity);
                if (box != null) {
                    box = box.move(boundingBox.getCenter().x, boundingBox.minY, boundingBox.getCenter().z);
                    Optional<Vec3> headshotHitPos = box.clip(startVec, endVec);
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

        if (hitPos == null) {
            return null;
        }

        return new EntityResult(entity, hitPos, headshot);
    }

    private Vec3 getVectorFromRotation(float pitch, float yaw) {
        var f = Mth.cos(-yaw * 0.017453292F - (float) Math.PI);
        var f1 = Mth.sin(-yaw * 0.017453292F - (float) Math.PI);
        var f2 = -Mth.cos(-pitch * 0.017453292F);
        var f3 = Mth.sin(-pitch * 0.017453292F);
        return new Vec3(f1 * f2, f3, f * f2);
    }

    private float getCriticalDamage(ItemStack weapon, RandomSource rand, float damage) {
        float chance = GunModifierHelper.getCriticalChance(weapon);
        if (rand.nextFloat() < chance) {
            return (float) (damage * Config.COMMON.gameplay.criticalDamageMultiplier.get());
        }
        return damage;
    }

    private void sendS2CData(CompoundTag data) {
        PacketHandler.getPlayChannel().sendToAll(new S2CMessageEntityData(this.getId(), data));
    }

    private LevelLocation getDeathTargetPoint() {
        return LevelLocation.create(this.level(), this.getX(), this.getY(), this.getZ(), 256);
    }

    private Vec3 getDirection(LivingEntity shooter, ItemStack weapon, GunItem item, Gun modifiedGun) {
        float gunSpread = GunModifierHelper.getModifiedSpread(weapon, modifiedGun.getGeneral().getSpread());

        if (gunSpread == 0F) return this.getVectorFromRotation(shooter.getXRot(), shooter.getYRot());

        if (!modifiedGun.getGeneral().isAlwaysSpread())
            gunSpread *= SpreadTracker.get(shooter).getSpread(item);

        if (ModSyncedDataKeys.AIMING.getValue(shooter)) gunSpread *= 0.5F;

        return this.getVectorFromRotation(shooter.getXRot() - (gunSpread / 2.0F) + random.nextFloat() * gunSpread, shooter.getYHeadRot() - (gunSpread / 2.0F) + random.nextFloat() * gunSpread);
    }

    /**
     * A custom implementation of ray tracing that allows you to pass a predicate to ignore certain
     * blocks when checking for collisions.
     *
     * @param world           the world to perform the ray trace
     * @param context         the ray trace context
     * @param ignorePredicate the block state predicate
     * @return a result of the raytrace
     */
    protected static BlockHitResult rayTraceBlocks(Level world, ClipContext context, Predicate<BlockState> ignorePredicate) {
        return performRayTrace(context, (rayTraceContext, blockPos) -> {
            BlockState blockState = world.getBlockState(blockPos);
            if (ignorePredicate.test(blockState)) return null;
            FluidState fluidState = world.getFluidState(blockPos);
            Vec3 startVec = rayTraceContext.getFrom();
            Vec3 endVec = rayTraceContext.getTo();
            VoxelShape blockShape = rayTraceContext.getBlockShape(blockState, world, blockPos);
            BlockHitResult blockResult = world.clipWithInteractionOverride(startVec, endVec, blockPos, blockShape, blockState);
            VoxelShape fluidShape = rayTraceContext.getFluidShape(fluidState, world, blockPos);
            BlockHitResult fluidResult = fluidShape.clip(startVec, endVec, blockPos);
            double blockDistance = blockResult == null ? Double.MAX_VALUE : rayTraceContext.getFrom().distanceToSqr(blockResult.getLocation());
            double fluidDistance = fluidResult == null ? Double.MAX_VALUE : rayTraceContext.getFrom().distanceToSqr(fluidResult.getLocation());
            return blockDistance <= fluidDistance ? blockResult : fluidResult;
        }, (rayTraceContext) -> {
            Vec3 Vector3d = rayTraceContext.getFrom().subtract(rayTraceContext.getTo());
            return BlockHitResult.miss(rayTraceContext.getTo(), Direction.getNearest(Vector3d.x, Vector3d.y, Vector3d.z), BlockPos.containing(rayTraceContext.getTo()));
        });
    }

    private static <T> T performRayTrace(ClipContext context, BiFunction<ClipContext, BlockPos, T> hitFunction, Function<ClipContext, T> p_217300_2_) {
        Vec3 startVec = context.getFrom();
        Vec3 endVec = context.getTo();
        if (startVec.equals(endVec)) {
            return p_217300_2_.apply(context);
        } else {
            double startX = Mth.lerp(-0.0000001, endVec.x, startVec.x);
            double startY = Mth.lerp(-0.0000001, endVec.y, startVec.y);
            double startZ = Mth.lerp(-0.0000001, endVec.z, startVec.z);
            double endX = Mth.lerp(-0.0000001, startVec.x, endVec.x);
            double endY = Mth.lerp(-0.0000001, startVec.y, endVec.y);
            double endZ = Mth.lerp(-0.0000001, startVec.z, endVec.z);
            int blockX = Mth.floor(endX);
            int blockY = Mth.floor(endY);
            int blockZ = Mth.floor(endZ);
            BlockPos.MutableBlockPos mutablePos = new BlockPos.MutableBlockPos(blockX, blockY, blockZ);
            T t = hitFunction.apply(context, mutablePos);
            if (t != null) {
                return t;
            }

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

                T t1 = hitFunction.apply(context, mutablePos.set(blockX, blockY, blockZ));
                if (t1 != null) {
                    return t1;
                }
            }

            return p_217300_2_.apply(context);
        }
    }

    public boolean isClientUpdated() {
        return hasClientData;
    }

    public void setClientUpdated() {
        this.hasClientData = true;
    }

    /**
     * Author: MrCrayfish
     */
    public static class EntityResult {
        private Entity entity;
        private Vec3 hitVec;
        private boolean headshot;

        public EntityResult(Entity entity, Vec3 hitVec, boolean headshot) {
            this.entity = entity;
            this.hitVec = hitVec;
            this.headshot = headshot;
        }

        /**
         * Gets the entity that was hit by the ammo
         */
        public Entity getEntity() {
            return this.entity;
        }

        /**
         * Gets the position the ammo hit
         */
        public Vec3 getHitPos() {
            return this.hitVec;
        }

        /**
         * Gets if this was a headshot
         */
        public boolean isHeadshot() {
            return this.headshot;
        }
    }
}
