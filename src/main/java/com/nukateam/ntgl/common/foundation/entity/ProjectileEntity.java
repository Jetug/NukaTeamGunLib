package com.nukateam.ntgl.common.foundation.entity;

import com.mrcrayfish.framework.api.network.LevelLocation;
import com.nukateam.ntgl.common.data.config.Ammo;
import com.nukateam.ntgl.Config;
import com.nukateam.ntgl.common.data.config.gun.General;
import com.nukateam.ntgl.common.data.config.gun.Gun;
import com.nukateam.ntgl.common.base.utils.BoundingBoxManager;
import com.nukateam.ntgl.common.base.utils.SpreadTracker;
import com.nukateam.ntgl.common.util.interfaces.*;
import com.nukateam.ntgl.common.util.util.*;
import com.nukateam.ntgl.common.util.util.math.ExtendedEntityRayTraceResult;
import com.nukateam.ntgl.common.event.GunProjectileHitEvent;
import com.nukateam.ntgl.common.foundation.ModTags;
import com.nukateam.ntgl.common.foundation.init.*;
import com.nukateam.ntgl.common.foundation.item.GunItem;
import com.nukateam.ntgl.common.util.world.ProjectileExplosion;
import com.nukateam.ntgl.common.network.PacketHandler;
import com.nukateam.ntgl.common.network.message.*;
import com.nukateam.ntgl.modules.enchantment.GunEnchantmentHelper;
import com.nukateam.ntgl.modules.enchantment.ModEnchantments;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundExplodePacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.stats.Stats;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
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
    protected boolean isServerSide = !level().isClientSide();

    protected boolean isRightHand;
    protected int shooterId;
    protected LivingEntity shooter;
    protected Gun modifiedGun;
    protected General general;
    protected Ammo projectile;
    protected ItemStack weapon = ItemStack.EMPTY;
    protected ItemStack item = ItemStack.EMPTY;
    protected float additionalDamage = 0.0F;
    protected EntityDimensions entitySize;
    protected double modifiedGravity;
    protected int life;

    public ProjectileEntity(EntityType<? extends Entity> entityType, Level worldIn) {
        super(entityType, worldIn);
    }
    protected Predicate<BlockState> getBlockFilter() {
        return (value) -> false;
    }

    public boolean isVisible(){
       return projectile.isVisible();
    }

    public boolean isRightHand(){
       return true;
    }

    public ProjectileEntity(EntityType<? extends Entity> entityType, Level worldIn, LivingEntity shooter, ItemStack weapon, GunItem item, Gun modifiedGun) {
        this(entityType, worldIn);
        this.shooterId = shooter.getId();
        this.shooter = shooter;
        this.modifiedGun = modifiedGun;
        this.general = modifiedGun.getGeneral();
        var data = new GunData(weapon, shooter);
        this.projectile = GunStateHelper.getAmmoConfig(data);
        this.entitySize = new EntityDimensions(this.projectile.getSize(), this.projectile.getSize(), false);
        this.modifiedGravity = projectile.isGravity() ? GunModifierHelper.getModifiedProjectileGravity(data, -0.04) : 0.0;
        this.life = GunModifierHelper.getModifiedProjectileLife(data, this.projectile.getLife());
        this.isRightHand = shooter.getItemInHand(InteractionHand.MAIN_HAND) == weapon;
        /* Get speed and set motion */
        Vec3 dir = this.getDirection(shooter, weapon, item, modifiedGun);
        double speedModifier = GunEnchantmentHelper.getProjectileSpeedModifier(weapon);
        double speed = GunModifierHelper.getModifiedProjectileSpeed(data, this.projectile.getSpeed() * speedModifier);
        this.setDeltaMovement(dir.x * speed, dir.y * speed, dir.z * speed);
        this.updateHeading();
        setupDirection(shooter, weapon, item, modifiedGun);
        /* Spawn the projectile halfway between the previous and current position */
        double posX = shooter.xOld + (shooter.getX() - shooter.xOld) / 2.0;
        double posY = shooter.yOld + (shooter.getY() - shooter.yOld) / 2.0 + shooter.getEyeHeight();
        double posZ = shooter.zOld + (shooter.getZ() - shooter.zOld) / 2.0;
        this.setPos(posX, posY, posZ);

        Item ammo = ForgeRegistries.ITEMS.getValue(GunStateHelper.getAmmoId(data));
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
            ItemStack ammoStack = new ItemStack(ammo);
            if (customModelData != -1) {
                ammoStack.getOrCreateTag().putInt("CustomModelData", customModelData);
            }
            this.item = ammoStack;
        }
    }

    @Override
    protected void defineSynchedData() {
    }

    @Override
    public EntityDimensions getDimensions(Pose pose) {
        return this.entitySize;
    }

    private Vec3 getDirection(LivingEntity shooter, ItemStack weapon, GunItem item, Gun modifiedGun) {
        var data = new GunData(weapon, shooter);
        float gunSpread = GunModifierHelper.getModifiedSpread(data);

        if (gunSpread == 0F) {
            return this.getVectorFromRotation(shooter.getXRot(), shooter.getYRot());
        }

        if (shooter instanceof Player) {
            if (!modifiedGun.getGeneral().isAlwaysSpread()) {
                gunSpread *= SpreadTracker.get((Player) shooter).getSpread(item);
            }

            if (ModSyncedDataKeys.AIMING.getValue((Player) shooter)) {
                gunSpread *= 0.5F;
            }
        }

        return this.getVectorFromRotation(shooter.getXRot() - (gunSpread / 2.0F) + random.nextFloat() * gunSpread, shooter.getYHeadRot() - (gunSpread / 2.0F) + random.nextFloat() * gunSpread);
    }

    public void setWeapon(ItemStack weapon) {
        this.weapon = weapon.copy();
    }

    public ItemStack getWeapon() {
        return this.weapon;
    }

    public void setItem(ItemStack item) {
        this.item = item;
    }

    public ItemStack getItem() {
        return this.item;
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

        if (shooter != null) {
            if (isServerSide) {
                var startVec = this.position();
                var endVec = startVec.add(this.getDeltaMovement());
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

        }

        if (this.tickCount >= this.life) {
            if (this.isAlive()) {
                this.onExpired();
            }
            this.remove(RemovalReason.KILLED);
        }
    }


    /**
     * A simple method to perform logic on each tick of the projectile. This method is appropriate
     * for spawning particles. Override {@link #tick()} to make changes to physics
     */
    protected void onProjectileTick() {
    }

    /**
     * Called when the projectile has run out of it's life. In other words, the projectile managed
     * to not hit any blocks and instead aged. The grenade uses this to explode in the air.
     */
    protected void onExpired() {
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

    @Nullable
    @SuppressWarnings("unchecked")
    private EntityResult getHitResult(Entity entity, Vec3 startVec, Vec3 endVec) {
        var expandHeight = entity instanceof Player && !entity.isCrouching() ? 0.0625 : 0.0;
        var boundingBox = entity.getBoundingBox();

        if (Config.COMMON.gameplay.improvedHitboxes.get()
                && entity instanceof ServerPlayer targetPlayer
                && shooter instanceof ServerPlayer shooterPlayer) {
            int ping = (int) Math.floor((shooterPlayer.latency / 1000.0) * 20.0 + 0.5);
            boundingBox = BoundingBoxManager.getBoundingBox(targetPlayer, ping);
        }

        boundingBox = boundingBox.expandTowards(0, expandHeight, 0);

        var hitPos = boundingBox.clip(startVec, endVec).orElse(null);
        var grownHitPos = boundingBox.inflate(Config.COMMON.gameplay.growBoundingBoxAmount.get(), 0,
                Config.COMMON.gameplay.growBoundingBoxAmount.get()).clip(startVec, endVec).orElse(null);

        if (hitPos == null && grownHitPos != null) {
            var clipContext = new ClipContext(startVec, grownHitPos, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, this);
            var rayTraceResult = rayTraceBlocks(this.level(), clipContext, getBlockFilter());

            if (rayTraceResult.getType() == HitResult.Type.BLOCK)
                return null;

            hitPos = grownHitPos;
        }

        /* Check for headshot */
        boolean headshot = false;
        if (Config.COMMON.gameplay.enableHeadShots.get() && entity instanceof LivingEntity livingEntity) {
            var headshotBox = (IHeadshotBox<LivingEntity>) BoundingBoxManager.getHeadshotBoxes(entity.getType());

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

        return new EntityResult(entity, hitPos, headshot);
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

            // Fire
            /*int fireStarterLevel = EnchantmentHelper.getItemEnchantmentLevel(ModEnchantments.FIRE_STARTER.get(), this.weapon);
            if(fireStarterLevel > 0 && Config.COMMON.gameplay.griefing.setFireToBlocks.get())
            {
                BlockPos offsetPos = pos.relative(blockHitResult.getDirection());
                if(BaseFireBlock.canBePlacedAt(this.level(), offsetPos, blockHitResult.getDirection()))
                {
                    BlockState fireState = BaseFireBlock.getState(this.level(), offsetPos);
                    this.level().setBlock(offsetPos, fireState, 11);
                    ((ServerLevel) this.level()).sendParticles(ParticleTypes.LAVA, hitVec.x - 1.0 + this.random.nextDouble() * 2.0, hitVec.y, hitVec.z - 1.0 + this.random.nextDouble() * 2.0, 4, 0, 0, 0, 0);
                }
            }*/
            return;
        }

        if (result instanceof ExtendedEntityRayTraceResult entityHitResult) {
            Entity entity = entityHitResult.getEntity();
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

    public int getLife(){
        return projectile.getLife();
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

        var source = ModDamageTypes.Sources.source(this.level().registryAccess(), projectile.getDamageType(),this, this.shooter);
        entity.hurt(source, damage);

        if (this.shooter instanceof ServerPlayer playerShooter) {
            var bodyHitType = headshot ? S2CMessageProjectileHitEntity.HitType.HEADSHOT : S2CMessageProjectileHitEntity.HitType.NORMAL;
            var hitType = critical ? S2CMessageProjectileHitEntity.HitType.CRITICAL : bodyHitType;

            PacketHandler.getPlayChannel().sendToPlayer(() -> playerShooter,
                    new S2CMessageProjectileHitEntity(hitVec.x, hitVec.y, hitVec.z, hitType, entity instanceof Player));
        }

        /* Send blood particle to tracking clients. */
        PacketHandler.getPlayChannel().sendToTracking(() -> entity, new S2CMessageBlood(hitVec.x, hitVec.y, hitVec.z));

        doImpactEffects(hitVec);
    }

    protected void onHitBlock(BlockState state, BlockPos pos, Direction face, double x, double y, double z) {
        PacketHandler.getPlayChannel().sendToTrackingChunk(
                () -> this.level().getChunkAt(pos),
                new S2CMessageProjectileHitBlock(x, y, z, pos, face));
        doImpactEffects(new Vec3(x, y, z));
    }

    protected void doImpactEffects(Vec3 hitVec) {

    }

    @Override
    protected void readAdditionalSaveData(CompoundTag compound) {
        this.projectile = new Ammo();
        this.projectile.deserializeNBT(compound.getCompound("Projectile"));
        this.general = new General();
        this.general.deserializeNBT(compound.getCompound("General"));
        this.modifiedGravity = compound.getDouble("ModifiedGravity");
        this.life = compound.getInt("MaxLife");
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag compound) {
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
        BufferUtil.writeItemStackToBufIgnoreTag(buffer, this.item);
        buffer.writeDouble(this.modifiedGravity);
        buffer.writeVarInt(this.life);
    }

    @Override
    public void readSpawnData(FriendlyByteBuf buffer) {
        this.projectile = new Ammo();
        this.projectile.deserializeNBT(buffer.readNbt());
        this.general = new General();
        this.general.deserializeNBT(buffer.readNbt());
        this.shooterId = buffer.readInt();
        this.item = BufferUtil.readItemStackFromBufIgnoreTag(buffer);
        this.modifiedGravity = buffer.readDouble();
        this.life = buffer.readVarInt();
        this.entitySize = new EntityDimensions(this.projectile.getSize(), this.projectile.getSize(), false);
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

    private Vec3 getVectorFromRotation(float pitch, float yaw) {
        float f = Mth.cos(-yaw * 0.017453292F - (float) Math.PI);
        float f1 = Mth.sin(-yaw * 0.017453292F - (float) Math.PI);
        float f2 = -Mth.cos(-pitch * 0.017453292F);
        float f3 = Mth.sin(-pitch * 0.017453292F);
        return new Vec3(f1 * f2, f3, f * f2);
    }

    /**
     * Gets the entity who spawned the projectile
     */
    public LivingEntity getShooter() {
        return this.shooter;
    }

    /**
     * Gets the id of the entity who spawned the projectile
     */
    public int getShooterId() {
        return this.shooterId;
    }

    public float getDamage() {
        if(weapon.isEmpty())
            return 0;

        var data = new GunData(this.weapon, this.shooter);

        float initialDamage = GunModifierHelper.getModifiedDamage(data)  + this.additionalDamage;

        if (this.projectile.isDamageReduceOverLife()) {
            float modifier = ((float) this.projectile.getLife() - (float) (this.tickCount - 1)) / (float) this.projectile.getLife();
            initialDamage *= modifier;
        }

        var projectileAmount = GunModifierHelper.getProjectileAmount(data);
        var damage = initialDamage / projectileAmount;
        damage = GunEnchantmentHelper.getAcceleratorDamage(this.weapon, damage);

        return Math.max(0F, damage);
    }

    private float getCriticalDamage(ItemStack weapon, RandomSource rand, float damage) {
        var data = new GunData(weapon, shooter);
        float chance = GunModifierHelper.getCriticalChance(data);
        if (rand.nextFloat() < chance) {
            return (float) (damage * Config.COMMON.gameplay.criticalDamageMultiplier.get());
        }
        return damage;
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

    private LevelLocation getDeathTargetPoint() {
        return LevelLocation.create(this.level(), this.getX(), this.getY(), this.getZ(), 256);
    }

    @Override
    public Packet<ClientGamePacketListener> getAddEntityPacket() {
        return NetworkHooks.getEntitySpawningPacket(this);
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
    public static BlockHitResult rayTraceBlocks(Level world, ClipContext context, Predicate<BlockState> ignorePredicate) {
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

    protected void setupDirection(LivingEntity shooter, ItemStack weapon, GunItem item, Gun modifiedGun) {
        /* Get speed and set motion */
        var dir = this.getDirection(shooter, weapon, item, modifiedGun);
        var speedModifier = GunEnchantmentHelper.getProjectileSpeedModifier(weapon);
        var data = new GunData(weapon, shooter);
        var speed = GunModifierHelper.getModifiedProjectileSpeed(data, this.projectile.getSpeed() * speedModifier);
        this.setDeltaMovement(dir.x * speed, dir.y * speed, dir.z * speed);
        this.updateHeading();
    }

    protected boolean removeOnHit() {
        return true;
    }

    /**
     * Creates a projectile explosion for the specified entity.
     *
     * @param entity    The entity to explode
     * @param radius    The amount of radius the entity should deal
     * @param forceNone If true, forces the explosion mode to be NONE instead of config value
     */
    public static void createExplosion(Entity entity, float radius, boolean forceNone) {
        Level world = entity.level();
        if (world.isClientSide())
            return;

        DamageSource source = entity instanceof ProjectileEntity projectile ? entity.damageSources().explosion(entity, projectile.getShooter()) : null;
//        Explosion.BlockInteraction mode = Config.COMMON.gameplay.griefing.enableBlockRemovalOnExplosions.get() && !forceNone ? Explosion.BlockInteraction.DESTROY : Explosion.BlockInteraction.KEEP;
        Explosion explosion = new ProjectileExplosion(world, entity, source, null, entity.getX(), entity.getY(), entity.getZ(), radius, false, Explosion.BlockInteraction.DESTROY_WITH_DECAY);

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

    public static void createFireExplosion(Entity entity, float radius, boolean forceNone) {
        Level world = entity.level();
        if (world.isClientSide())
            return;

        DamageSource source = entity instanceof ProjectileEntity projectile ? entity.damageSources().explosion(entity, projectile.getShooter()) : null;
        Explosion.BlockInteraction mode = Explosion.BlockInteraction.KEEP;
        Explosion explosion = new ProjectileExplosion(world, entity, source, null, entity.getX(), entity.getY(), entity.getZ(), radius, true, mode);

        if (net.minecraftforge.event.ForgeEventFactory.onExplosionStart(world, explosion))
            return;

        // Do explosion logic
        explosion.explode();
        explosion.finalizeExplosion(true);

    }

    /**
     * Author: MrCrayfish
     */
    public static class EntityResult {
        private final Entity entity;
        private final Vec3 hitVec;
        private final boolean headshot;

        public EntityResult(Entity entity, Vec3 hitVec, boolean headshot) {
            this.entity = entity;
            this.hitVec = hitVec;
            this.headshot = headshot;
        }

        /**
         * Gets the entity that was hit by the projectile
         */
        public Entity getEntity() {
            return this.entity;
        }

        /**
         * Gets the position the projectile hit
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
