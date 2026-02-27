package com.nukateam.ntgl.common.foundation.goals;

import com.nukateam.ntgl.common.data.WeaponData;
import com.nukateam.ntgl.common.data.holders.WeaponMode;

import com.nukateam.ntgl.common.foundation.item.interfaces.IWeapon;
import com.nukateam.ntgl.common.network.ServerPlayHandler;
import com.nukateam.ntgl.common.util.trackers.EntityReloadTracker;
import com.nukateam.ntgl.common.foundation.init.ModSyncedDataKeys;
import com.nukateam.ntgl.common.network.message.C2SMessageShoot;
import com.nukateam.ntgl.common.util.util.WeaponStateHelper;
import net.minecraft.util.TimeUtil;
import net.minecraft.util.valueproviders.UniformInt;
import net.minecraft.world.Difficulty;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;

import java.util.EnumSet;

public class GunAttackGoal extends Goal {
    public static final UniformInt PATHFINDING_DELAY_RANGE = TimeUtil.rangeOfSeconds(1, 3);
    private final PathfinderMob mob;
    private final double speedModifier;
    private final float minAttackDistance;
    private final float maxAttackDistance;
    private ItemStack gunStack;
    private IWeapon gunItem;

    private int seeTime;
    private int attackDelay;
    private int updatePathDelay;
    private int burstCounter;
    private int burstDelay;
    private int strafeDirection;
    private int strafeTimer;
    private boolean strafingClockwise;
    private float baseAccuracy;
    private float currentAccuracy;
    private int accuracyRecoveryTimer;

    private static final int BURST_COUNT = 20;
    private static final int BURST_DELAY = 5;
    private static final int BURST_COOLDOWN = 20;
    private static final int STRAFE_CHANGE_INTERVAL = 40;
    private static final int ACCURACY_RECOVERY_TIME = 30;
    private static final float MAX_ACCURACY_DEVIATION = 15.0f;
    private static final float ACCURACY_PENALTY_PER_SHOT = 2.5f;

    public GunAttackGoal(PathfinderMob mob, double speedModifier, float minAttackDistance, float maxAttackDistance) {
        this.mob = mob;
        this.speedModifier = speedModifier;
        this.minAttackDistance = minAttackDistance * minAttackDistance;
        this.maxAttackDistance = maxAttackDistance * maxAttackDistance;
        this.gunStack = mob.getMainHandItem();
        this.setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
        this.baseAccuracy = calculateBaseAccuracy();
        this.currentAccuracy = this.baseAccuracy;
        this.strafeDirection = 0;
        this.strafeTimer = 0;

        updateGunItem();
    }

    private void updateGunItem() {
        ItemStack currentStack = mob.getMainHandItem();
        if (currentStack.getItem() instanceof IWeapon weapon) {
            this.gunItem = weapon;
            this.gunStack = currentStack;
        } else {
            this.gunItem = null;
        }
    }

    private float calculateBaseAccuracy() {
        Difficulty difficulty = this.mob.level().getDifficulty();
        float accuracy;

        switch (difficulty) {
            case PEACEFUL:
            case EASY:
                accuracy = 0.7f;
                break;
            case NORMAL:
                accuracy = 0.85f;
                break;
            case HARD:
                accuracy = 0.95f;
                break;
            default:
                accuracy = 0.8f;
        }

        accuracy += (this.mob.getRandom().nextFloat() - 0.5f) * 0.1f;
        return Math.min(Math.max(accuracy, 0.1f), 1.0f);
    }

    private float getDifficultyMultiplier() {
        var difficulty = this.mob.level().getDifficulty();
        return switch (difficulty) {
            case PEACEFUL -> 0.5f;
            case EASY -> 0.75f;
            case NORMAL -> 1.0f;
            case HARD -> 1.25f;
        };
    }

    @Override
    public boolean canUse() {
        updateGunItem();
        var target = this.mob.getTarget();
        if (target == null) return false;

        double distance = this.mob.distanceToSqr(target);
        var tooClose = distance < this.minAttackDistance;
        var tooFar = distance > this.maxAttackDistance;
        return !tooClose && !tooFar && this.isValidTarget() && this.gunItem != null;
    }

    @Override
    public boolean canContinueToUse() {
        updateGunItem();
        return this.isValidTarget() && (this.gunItem != null || !this.mob.getNavigation().isDone());
    }

    private boolean isValidTarget() {
        return this.mob.getTarget() != null && this.mob.getTarget().isAlive();
    }

    @Override
    public void start() {
        super.start();
        this.mob.setAggressive(true);
    }

    @Override
    public void stop() {
        super.stop();
        this.mob.setAggressive(false);
        this.seeTime = 0;
        this.burstCounter = 0;
        this.burstDelay = 0;
        this.currentAccuracy = this.baseAccuracy;
        this.accuracyRecoveryTimer = 0;
        if (this.mob.isUsingItem()) {
            this.mob.stopUsingItem();
        }
        setReloading(false);
    }

    private void setReloading(boolean value) {
        ModSyncedDataKeys.RELOADING_RIGHT.setValue(mob, value);
    }

    @Override
    public boolean requiresUpdateEveryTick() {
        return true;
    }

    @Override
    public void tick() {
        LivingEntity target = this.mob.getTarget();
        if (target == null) return;

        updateGunItem();
        if (this.gunItem == null) return;

        double distance = this.mob.distanceToSqr(target);
        boolean hasLineOfSight = this.mob.getSensing().hasLineOfSight(target);

        if (hasLineOfSight != (this.seeTime > 0)) {
            this.seeTime = 0;
        }
        if (hasLineOfSight) {
            ++this.seeTime;
        } else {
            --this.seeTime;
        }

        if (this.accuracyRecoveryTimer > 0) {
            this.accuracyRecoveryTimer--;
            if (this.accuracyRecoveryTimer == 0) {
                this.currentAccuracy = Math.min(this.currentAccuracy + 0.1f, this.baseAccuracy);
            }
        }

        this.updateMovement(target, distance, hasLineOfSight);

        if (hasLineOfSight || this.seeTime > -10) {
            this.mob.getLookControl().setLookAt(target, 30.0F, 30.0F);
        }

        this.updateShooting(target, distance, hasLineOfSight);
        this.updateStrafe(target, distance);
    }

    private void updateMovement(LivingEntity target, double distance, boolean hasLineOfSight) {
        var tooClose = distance < this.minAttackDistance;
        var tooFar = distance > this.maxAttackDistance;
        var shouldMove = (tooClose || tooFar || !hasLineOfSight) && this.attackDelay == 0;

        if (shouldMove) {
            --this.updatePathDelay;
            if (this.updatePathDelay <= 0) {
                double speed = this.speedModifier;

                if (tooClose) {
                    var retreatDir = this.mob.position().subtract(target.position()).normalize();
                    var retreatPos = this.mob.position().add(retreatDir.scale(5));
                    this.mob.getNavigation().moveTo(retreatPos.x, retreatPos.y, retreatPos.z, speed);
                } else if (tooFar) {
                    if (this.mob.getRandom().nextFloat() < 0.7f) {
                        this.mob.getNavigation().moveTo(target, speed);
                    } else {
                        Vec3 toTarget = target.position().subtract(this.mob.position()).normalize();
                        Vec3 perpendicular = toTarget.cross(new Vec3(0, 1, 0)).normalize();
                        Vec3 approachPos = target.position().add(perpendicular.scale(
                                this.mob.getRandom().nextBoolean() ? 3 : -3
                        ));
                        this.mob.getNavigation().moveTo(approachPos.x, approachPos.y, approachPos.z, speed);
                    }
                }

                this.updatePathDelay = PATHFINDING_DELAY_RANGE.sample(this.mob.getRandom());
            }
        } else {
            this.updatePathDelay = 0;
            if (!this.shouldStrafe()) {
                this.mob.getNavigation().stop();
            }
        }
    }

    private void updateStrafe(LivingEntity target, double distance) {
        if (!this.shouldStrafe() || distance > this.maxAttackDistance || distance < this.minAttackDistance) {
            this.strafeDirection = 0;
            return;
        }

        this.strafeTimer--;
        if (this.strafeTimer <= 0) {
            this.strafeTimer = STRAFE_CHANGE_INTERVAL + this.mob.getRandom().nextInt(20);
            this.strafingClockwise = this.mob.getRandom().nextBoolean();
        }

        if (this.mob.getRandom().nextFloat() < 0.02f) {
            this.strafingClockwise = !this.strafingClockwise;
        }

        this.strafeDirection = this.strafingClockwise ? 1 : -1;

        var toTarget = target.position().subtract(this.mob.position()).normalize();
        var strafeDir = toTarget.cross(new Vec3(0, 1, 0)).normalize().scale(this.strafeDirection * 0.5);
        var strafePos = this.mob.position().add(strafeDir);

        this.mob.getNavigation().moveTo(strafePos.x, strafePos.y, strafePos.z, this.speedModifier * 0.7);
    }

    private boolean shouldStrafe() {
        return this.seeTime > 20 &&
                hasAmmo() &&
                !isReloading() &&
                this.mob.getRandom().nextFloat() < 0.6f; // 60% шанс на страф
    }

    private boolean hasAmmo() {
        if (this.gunItem == null) return false;
        var weaponData = new WeaponData(this.gunStack, mob);
        return WeaponStateHelper.hasAmmo(weaponData);
    }

    private boolean isReloading() {
        return EntityReloadTracker.isReloading(mob);
    }

    private void updateShooting(LivingEntity target, double distance, boolean hasLineOfSight) {
        boolean canShoot = hasLineOfSight &&
                this.seeTime >= 20 &&
                distance >= this.minAttackDistance &&
                distance <= this.maxAttackDistance &&
                this.gunItem != null;

        if (canShoot) {
            if (hasAmmo()) {
                if (this.burstDelay > 0) {
                    this.burstDelay--;
                    return;
                 }

                if (this.burstCounter < BURST_COUNT) {
                    if (this.attackDelay <= 0) {
                        this.fireAtTarget(target);
                        this.burstCounter++;
                        this.attackDelay = BURST_DELAY;

                        this.currentAccuracy = Math.max(
                                this.currentAccuracy - ACCURACY_PENALTY_PER_SHOT / 100f,
                                this.baseAccuracy * 0.3f
                        );
                        this.accuracyRecoveryTimer = ACCURACY_RECOVERY_TIME;
                    } else {
                        this.attackDelay--;
                    }
                } else {
                    this.burstCounter = 0;
                    this.burstDelay = BURST_COOLDOWN + this.mob.getRandom().nextInt(10);

                    if (this.mob.getRandom().nextFloat() < 0.3f) {
                        this.burstDelay += 20;
                    }
                }
            } else if (!isReloading()) {
                EntityReloadTracker.addTracker(mob, HumanoidArm.RIGHT);
                this.burstCounter = 0;
                this.burstDelay = 0;
            }
        } else {
            if (this.seeTime < 0) {
                this.burstCounter = 0;
                this.burstDelay = 0;
            }
        }
    }

    private void fireAtTarget(LivingEntity target) {
        if (this.gunItem == null) return;

        var yawOffset = calculateInaccuracyOffset();
        var pitchOffset = calculateInaccuracyOffset();

        var targetPos = target.position().add(0, target.getEyeHeight() * 0.7, 0);
        var shooterPos = this.mob.getEyePosition();
        var direction = targetPos.subtract(shooterPos).normalize();

        direction = applyAccuracyDeviation(direction, yawOffset, pitchOffset);

        var yaw = (float)(Math.atan2(direction.z, direction.x) * (180 / Math.PI)) - 90;
        var pitch = -(float)(Math.asin(direction.y) * (180 / Math.PI));

        performRangedAttack(yaw, pitch, yawOffset, pitchOffset);

        this.mob.swing(InteractionHand.MAIN_HAND);
    }

    private void performRangedAttack(float yaw, float pitch, float yawOffset, float pitchOffset) {
        var message = new C2SMessageShoot(
                mob.getId(),
                yaw,
                pitch,
                yawOffset,
                pitchOffset,
                InteractionHand.MAIN_HAND,
                WeaponMode.PRIMARY
        );

        ServerPlayHandler.handleShoot(message, mob);
    }

    private float calculateInaccuracyOffset() {
        var accuracyFactor = 1.0f - this.currentAccuracy;
        var randomDeviation = (this.mob.getRandom().nextFloat() - 0.5f) * 2 * MAX_ACCURACY_DEVIATION;
        var difficultyMultiplier = getDifficultyMultiplier();

        return randomDeviation * accuracyFactor * difficultyMultiplier;
    }

    private Vec3 applyAccuracyDeviation(Vec3 direction, float yawOffset, float pitchOffset) {
        double yaw = Math.atan2(direction.z, direction.x);
        double pitch = Math.asin(direction.y);

        yaw += Math.toRadians(yawOffset);
        pitch += Math.toRadians(pitchOffset);
        pitch = Math.max(Math.min(pitch, Math.PI/2 - 0.01), -Math.PI/2 + 0.01);

        double x = Math.cos(pitch) * Math.cos(yaw);
        double y = Math.sin(pitch);
        double z = Math.cos(pitch) * Math.sin(yaw);

        return new Vec3(x, y, z);
    }

    public static void shoot(LivingEntity shooter, InteractionHand hand) {
        var message = new C2SMessageShoot(
                shooter.getId(),
                shooter.getViewYRot(1),
                shooter.getViewXRot(1),
                0, 0,
                hand,
                WeaponMode.PRIMARY
        );

        ServerPlayHandler.handleShoot(message, shooter);
    }
}