package com.nukateam.chassis_core.common.foundation.entity;

import com.google.common.collect.ImmutableMap;
import com.nukateam.chassis_core.ChassisCore;
import com.nukateam.chassis_core.Global;
import com.nukateam.chassis_core.client.animators.HandAnimator;
import com.nukateam.chassis_core.client.render.renderers.CustomHandRenderer;
import com.nukateam.chassis_core.common.data.holders.ChassisPart;
import com.nukateam.chassis_core.common.foundation.item.ChassisEquipment;
import com.nukateam.chassis_core.common.util.helpers.Speedometer;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.damagesource.CombatRules;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.util.Lazy;
import net.minecraftforge.network.NetworkHooks;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.Map;

import static com.nukateam.chassis_core.common.data.constants.Resources.resourceLocation;
import static net.minecraft.util.Mth.cos;
import static net.minecraft.util.Mth.sin;
import static software.bernie.geckolib.util.GeckoLibUtil.createInstanceCache;

public abstract class WearableChassis extends Chassis implements GeoEntity {
    public static final float ROTATION = (float) Math.PI / 180F;
    public static final int EFFECT_DURATION = 9;
    public static final HandAnimator HAND_ENTITY = new HandAnimator();
    private static final Lazy<CustomHandRenderer> HAND_RENDERER = Lazy.of(() -> new CustomHandRenderer());
    public static final ResourceLocation DEFAULT_ICON = resourceLocation("textures/item/chassis.png");
    public static final float STEP_HEIGHT = 0.5f;
    public static final double PLAYER_RIDING_OFFSET = -0.35;
    public final Speedometer speedometer = new Speedometer(this);
    private final AnimatableInstanceCache cache = createInstanceCache(this);
    protected boolean isJumping;
    protected float jumpScale;
    public float bob;
    public float oBob;

    public static final EntityDimensions STANDING_DIMENSIONS = EntityDimensions.scalable(1.0f, 2.3f);
    public static final EntityDimensions CROUCHING_DIMENSIONS = EntityDimensions.scalable(1.0f, 2.0f);

    protected final Map<Pose, EntityDimensions> POSES = ImmutableMap.<Pose, EntityDimensions>builder()
            .put(Pose.STANDING, getStandingDimensions())
            .put(Pose.CROUCHING, getCrouchingDimensions())
            .build();

    public WearableChassis(EntityType<? extends Chassis> type, Level worldIn) {
        super(type, worldIn);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Chassis.createLivingAttributes()
                .add(Attributes.MAX_HEALTH, 1000.0D)
                .add(Attributes.ATTACK_DAMAGE, 0.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.1D)
                .add(Attributes.ATTACK_KNOCKBACK, 0.0D)
                .add(Attributes.JUMP_STRENGTH, 0.5D)
                .add(Attributes.KNOCKBACK_RESISTANCE, 0.8D);
    }

    @Override
    public @NotNull EntityDimensions getDimensions(@NotNull Pose pPose) {
        return POSES.getOrDefault(pPose, STANDING_DIMENSIONS);
    }

    @Override
    public void tick() {
        super.tick();
        speedometer.tick();
        timer.tick();
        updatePose();
    }

    @Override
    public boolean canSprint() {
        return true;
    }

    @Override
    public float getStepHeight() {
        if(hasPlayerPassenger())
            return STEP_HEIGHT;
        return super.getStepHeight();
    }

    @Override
    public boolean hurt(DamageSource damageSource, float damage) {
        var passenger = getControllingPassenger();

        if (passenger != null)
            passenger.hurt(damageSource, damage);

        return false;
    }

    public float getDamageAfterAbsorb(DamageSource damageSource, float damage) {
        float finalDamage = getDamageAfterAbsorb(damage);
        damageArmor(damageSource, damage);

        var passenger = getControllingPassenger();

        if ((hasPassenger() && damageSource.getEntity() == passenger))
            return 0;

        return finalDamage;
    }

    @Override
    public void aiStep() {
        updateBobing();
        super.aiStep();
        if (hasPassenger()) this.yHeadRot = this.getYRot();
    }

    @Override
    public boolean isInvulnerableTo(DamageSource damageSource) {
        if (super.isInvulnerableTo(damageSource))
            return true;
        else
            return damageSource.getEntity() == getControllingPassenger();
    }

    @Override
    public InteractionResult interactAt(Player player, Vec3 vector, InteractionHand hand) {
        if (isServerSide && !player.isPassenger()) {
            if (player.isShiftKeyDown()) {
                openGUI(player);
                return InteractionResult.SUCCESS;
            } else if (!isVehicle()) {
                this.ride(player);
                return InteractionResult.SUCCESS;
            }
        }

        return InteractionResult.PASS;
    }

    @Nullable
    @Override
    public LivingEntity getControllingPassenger() {
        if (!this.getPassengers().isEmpty() && this.getPassengers().get(0) instanceof LivingEntity livingEntity)
            return livingEntity;
        return null;
    }

    @Override
    public void positionRider(Entity entity, MoveFunction pCallback) {
        super.positionRider(entity, pCallback);

        var passenger = getControllingPassenger();
        if (passenger == null) return;

        var yOffset = 1.0f;
        var posY = getY() + getPassengersRidingOffset() + PLAYER_RIDING_OFFSET - yOffset;
        entity.setPos(getX(), posY, getZ());

        if (entity instanceof LivingEntity livingEntity)
            livingEntity.yBodyRot = yBodyRot;
    }

    @Override
    public float getFlyingSpeed() {
        if (hasPlayerPassenger() && isPlayerFlying() && !this.isPassenger()) {
            var speed = this.getPlayerPassenger().getAbilities().getFlyingSpeed();
            return this.isSprinting() ? speed * 2.0F : speed;
        } else {
            return this.isSprinting() ? 0.025999999F : 0.02F;
        }
    }

    @Override
    public boolean isAffectedByFluids() {
        return isPlayerFlying();
    }

    @Override
    public boolean isCrouching() {
        if (hasPlayerPassenger()) {
            return getPlayerPassenger().isShiftKeyDown();
        }
        return super.isCrouching();
    }

    @Override
    public void travel(Vec3 travelVector) {
        if (!isAlive()) return;

        if (isPlayerFlying()) {
            creativeFlyTravel();
            return;
        }

        this.setNoGravity(false);
        if (isVehicle() && hasPassenger())
            travelWithPassenger(travelVector);
        else {
            super.travel(travelVector);
        }
    }

    @Override
    public boolean isShiftKeyDown() {
        return getControllingPassenger() instanceof Player player ?
                player.isShiftKeyDown() :
                super.isShiftKeyDown();
    }

    @Override
    protected Vec3 maybeBackOffFromEdge(Vec3 pVec, MoverType pMover) {
        if (!isPlayerFlying() && pVec.y <= 0 && (pMover == MoverType.SELF || pMover == MoverType.PLAYER) && this.isShiftKeyDown() && this.isAboveGround()) {
            var x = pVec.x;
            var z = pVec.z;
            var edge = 0.05;

            while(x != 0 && this.level().noCollision(this, this.getBoundingBox().move(x, (-this.maxUpStep()), 0))) {
                if (x < edge && x >= -edge) {
                    x = 0;
                } else if (x > 0) {
                    x -= edge;
                } else {
                    x += edge;
                }
            }

            while(z != 0 && this.level().noCollision(this, this.getBoundingBox().move(0, (-this.maxUpStep()), z))) {
                if (z < edge && z >= -edge) {
                    z = 0;
                } else if (z > 0) {
                    z -= edge;
                } else {
                    z += edge;
                }
            }

            while(x != 0 && z != 0 && this.level().noCollision(this, this.getBoundingBox().move(x, (-this.maxUpStep()), z))) {
                if (x < edge && x >= -edge) {
                    x = 0;
                } else if (x > 0) {
                    x -= edge;
                } else {
                    x += edge;
                }

                if (z < edge && z >= -edge) {
                    z = 0;
                } else if (z > 0) {
                    z -= edge;
                } else {
                    z += edge;
                }
            }

            pVec = new Vec3(x, pVec.y, z);
        }

        return pVec;
    }

    @Override
    public boolean isNoGravity() {
        if (getControllingPassenger() instanceof Player player) {
            return player.getAbilities().flying;
        }
        return super.isNoGravity();
    }

    private Vec3 calculateDesiredMotion(Vec3 look, float forward, float strafe, float vertical) {
        var horizontal = new Vec3(look.x, 0, look.z).normalize()
                .scale(forward)
                .add(new Vec3(look.z, 0, -look.x).normalize().scale(strafe));

        var verticalVec = new Vec3(0, vertical, 0);

        return horizontal.add(verticalVec).normalize();
    }

    private Vec3 applyAcceleration(Vec3 current, Vec3 desired, float acceleration) {
        return new Vec3(
                lerp(current.x, desired.x, acceleration),
                lerp(current.y, desired.y, acceleration),
                lerp(current.z, desired.z, acceleration)
        );
    }

    private double lerp(double a, double b, float t) {
        return a + (b - a) * t;
    }

    @Override
    public Vec3 getDismountLocationForPassenger(LivingEntity p_20123_) {
        return super.getDismountLocationForPassenger(p_20123_);
    }

    @Override
    public void checkDespawn() {}

    @Override
    public boolean canBreatheUnderwater() {
        return true;
    }

    @Override
    public boolean dismountsUnderwater() {
        return false;
    }

    @Override
    protected float tickHeadTurn(float pYRot, float pAnimStep) {
        if (hasPassenger())
            return super.tickHeadTurn(pYRot, pAnimStep);
        return pAnimStep;
    }

    @Override
    public float maxUpStep() {
        float f = super.maxUpStep();
        return !hasPlayerPassenger() ? Math.max(f, 1.0F) : f;
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return cache;
    }

    @Nullable
    public ChassisEquipment getEquipmentItem(ChassisPart part) {
        var stack = getEquipment(part);
        if (!stack.isEmpty())
            return (ChassisEquipment) stack.getItem();
        return null;
    }

    @OnlyIn(Dist.CLIENT)
    public HandAnimator getHandEntity() {
        return HAND_ENTITY;
    }

    @OnlyIn(Dist.CLIENT)
    public CustomHandRenderer getHandRenderer() {
        return HAND_RENDERER.get();
    }

    public ResourceLocation getIcon() {
        return DEFAULT_ICON;
    }

    public boolean renderHand(){
        return true;
    }

    public void openGUI(Player player) {
        Global.referenceMob = this;

        var provider = getMenuProvider();

        if (provider != null && player instanceof ServerPlayer serverPlayer) {
            NetworkHooks.openScreen(
                    serverPlayer,
                    provider,
                    buf -> buf.writeInt(this.getId())
            );
        }
    }

    public void openStationGUI(Player player) {
        Global.referenceMob = this;
        var provider = getStantionMenuProvider();

        if (isServerSide && provider != null) {
            player.openMenu(provider);
        }
    }

    public EntityDimensions getStandingDimensions(){
        return STANDING_DIMENSIONS;
    }

    public EntityDimensions getCrouchingDimensions(){
        return CROUCHING_DIMENSIONS;
    }


    @Nullable
    protected abstract MenuProvider getStantionMenuProvider();

    protected float getCrouchingSpeed(){
        return getSpeedAttribute() / 2f;
    }

    protected float getSprintingSpeed(){
        return getSpeedAttribute() * 1.5f;
    }

    protected void updatePose() {
        Pose pose;
        if (hasPlayerPassenger() && getPlayerPassenger().isShiftKeyDown()) {
            pose = Pose.CROUCHING;
            setSpeed(getCrouchingSpeed());

        } else {
            pose = Pose.STANDING;
            if(hasPlayerPassenger() && getPlayerPassenger().isSprinting())
                setSpeed(getSprintingSpeed());
            else
                setSpeed(getSpeedAttribute());
        }

        this.setPose(pose);
    }


    protected void updateBobing() {
        this.oBob = this.bob;

        float f;
        if (this.onGround() && !this.isDeadOrDying() && !this.isSwimming())
            f = Math.min(0.1F, (float) this.getDeltaMovement().horizontalDistance());
        else f = 0.0F;

        this.bob += (f - this.bob) * 0.4F;
    }

    public Boolean isWalking() {
        if (!hasPassenger())
            return false;

        var entity = getControllingPassenger();
        return entity.xxa != 0.0 || entity.zza != 0.0;
    }

    public boolean hasPlayerPassenger() {
        return getControllingPassenger() instanceof Player;

    }

    public Player getPlayerPassenger() {
        if (getControllingPassenger() instanceof Player player)
            return player;
        return null;
    }

    public boolean hasPassenger() {
        return getControllingPassenger() instanceof LivingEntity;
    }

    public ItemStack getPassengerItem(EquipmentSlot slot) {
        return hasPassenger() ? getControllingPassenger().getItemBySlot(slot) : ItemStack.EMPTY;
    }

    public void jump() {
        jumpScale = 1.0F;
    }

    public void ride(LivingEntity entity) {
        entity.setYRot(getYRot());
        entity.setXRot(getXRot());
        entity.startRiding(this);
    }

    public void exitArmor(){
        if(hasPassenger()){
            getControllingPassenger().stopRiding();
        }
    }

    protected boolean isPlayerFlying() {
        return getControllingPassenger() instanceof Player player && player.isCreative() && player.getAbilities().flying;
    }

    private float getDamageAfterAbsorb(float damage) {
        updateTotalArmor();
        return CombatRules.getDamageAfterAbsorb(damage, totalDefense, totalToughness);
    }

    private boolean isJumping() {
        return this.isJumping;
    }

    private double getCustomJump() {
        return this.getAttributeValue(Attributes.JUMP_STRENGTH);
    }

    private void travelWithPassenger(Vec3 travelVector) {
        var entity = getControllingPassenger();
        if(entity == null) return;
        setRotationMatchingPassenger(entity);

        if (jumpScale > 0.0F && !isJumping() && onGround())
            jump(entity);

        if (isControlledByLocalInstance()) {
            super.travel(new Vec3(entity.xxa, travelVector.y, entity.zza));
        }
        else setDeltaMovement(Vec3.ZERO);
        if (onGround()) {
            jumpScale = 0.0F;
            isJumping = false;
        }
    }

    private void creativeFlyTravel() {
        var player = getPlayerPassenger();
        this.setNoGravity(true);
        setRotationMatchingPassenger(player);

        var acceleration = 0.1f;
        var forward = player.zza;
        var strafe = player.xxa;
        var vertical = 0.0f;

        if (player.jumping) vertical += 1.0f;
        if (player.isShiftKeyDown()) vertical -= 1.0f;

        var look = player.getLookAngle();
        var desiredMotion = calculateDesiredMotion(look, forward, strafe, vertical);
        var currentMotion = this.getDeltaMovement();
        var newMotion = applyAcceleration(currentMotion, desiredMotion, acceleration);
        var speedFactor = player.getAbilities().getWalkingSpeed() * 10.0f;

        if (player.isSprinting()) {
            speedFactor *= 1.2f;
        }
        newMotion = newMotion.scale(speedFactor);


        this.setDeltaMovement(newMotion);
        this.move(MoverType.SELF, this.getDeltaMovement());


        this.setDeltaMovement(this.getDeltaMovement().scale(0.8));


        this.fallDistance = 0.0f;
    }

    private boolean isAboveGround() {
        return this.onGround()
                || this.fallDistance < this.maxUpStep()
                && !this.level().noCollision(this, getMove());
    }

    private @NotNull AABB getMove() {
        return this.getBoundingBox().move(0, this.fallDistance - this.maxUpStep(), 0);
    }

    private void jump(LivingEntity entity) {
        var jump = getCustomJump() * jumpScale * getBlockJumpFactor();
        setDeltaMovement(getDeltaMovement().x, jump, getDeltaMovement().z);
        isJumping = true;
        hasImpulse = true;

        if (entity.zza > 0.0F) {
            float x = sin(getYRot() * ROTATION);
            float z = cos(getYRot() * ROTATION);
            setDeltaMovement(getDeltaMovement().add(
                    -0.4F * x * jumpScale,
                    0.0D,
                    0.4F * z * jumpScale));
        }

        jumpScale = 0.0F;
    }

    private void setRotationMatchingPassenger(LivingEntity livingEntity) {
        this.yRotO = getYRot();
        this.setYRot(livingEntity.getYRot());
        this.setXRot(livingEntity.getXRot() * STEP_HEIGHT);
        this.setRot(getYRot(), getXRot());
    }
}