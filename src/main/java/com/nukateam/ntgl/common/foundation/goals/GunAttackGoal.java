package com.nukateam.ntgl.common.foundation.goals;

import com.nukateam.ntgl.common.base.config.Gun;
import com.nukateam.ntgl.common.network.ServerPlayHandler;
import com.nukateam.ntgl.common.base.utils.EntityReloadTracker;
import com.nukateam.ntgl.common.data.interfaces.IGunUser;
import com.nukateam.ntgl.common.foundation.init.ModSyncedDataKeys;
import com.nukateam.ntgl.common.foundation.item.GunItem;
import com.nukateam.ntgl.common.network.message.C2SMessageShoot;
import net.minecraft.util.TimeUtil;
import net.minecraft.util.valueproviders.UniformInt;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.monster.RangedAttackMob;

import java.util.EnumSet;

public class GunAttackGoal<T extends PathfinderMob & RangedAttackMob & IGunUser> extends Goal {
    public static final UniformInt PATHFINDING_DELAY_RANGE = TimeUtil.rangeOfSeconds(1, 2);
    private final T mob;
    private final double speedModifier;
    private final float attackRadiusSqr;
    private int seeTime;
    private int attackDelay;
    private int updatePathDelay;

    public GunAttackGoal(T pMob, double pSpeedModifier, float pAttackRadius) {
        this.mob = pMob;
        this.speedModifier = pSpeedModifier;
        this.attackRadiusSqr = pAttackRadius * pAttackRadius;
        this.setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
    }

    /**
     * Returns whether execution should begin. You can also read and cache any state necessary for execution in this
     * method as well.
     */
    public boolean canUse() {
        return this.isValidTarget() && this.isHoldingGun();
    }

    private boolean isHoldingGun() {
        return this.mob.isHolding(is -> is.getItem() instanceof GunItem);
    }

    /**
     * Returns whether an in-progress EntityAIBase should continue executing
     */
    public boolean canContinueToUse() {
        return this.isValidTarget() && (this.canUse() || !this.mob.getNavigation().isDone()) && this.isHoldingGun();
    }

    private boolean isValidTarget() {
        return this.mob.getTarget() != null && this.mob.getTarget().isAlive();
    }

    /**
     * Reset the task's internal state. Called when this task is interrupted by another one
     */
    public void stop() {
        super.stop();
        this.mob.setAggressive(false);
        this.mob.setTarget(null);
        this.seeTime = 0;
        if (this.mob.isUsingItem()) {
            this.mob.stopUsingItem();
        }
        setReloading(false);
    }

    private void setReloading(boolean value) {
        ModSyncedDataKeys.RELOADING_RIGHT.setValue(mob, value);
    }

    public boolean requiresUpdateEveryTick() {
        return true;
    }

    /**
     * Keep ticking a continuous task that has already been started
     */
    public void tick() {
        var target = this.mob.getTarget();
        if (target == null) return;

        var hasLineOfSight = this.mob.getSensing().hasLineOfSight(target);
        var flag1 = this.seeTime > 0;

        if (hasLineOfSight != flag1) this.seeTime = 0;

        if (hasLineOfSight)
            ++this.seeTime;
        else --this.seeTime;

        var distance = this.mob.distanceToSqr(target);
        var flag2 = (distance > (double) this.attackRadiusSqr || this.seeTime < 5) && this.attackDelay == 0;
        if (flag2) {
            --this.updatePathDelay;
            if (this.updatePathDelay <= 0) {
                this.mob.getNavigation().moveTo(target, this.canRun() ? this.speedModifier : this.speedModifier * 0.5D);
                this.updatePathDelay = PATHFINDING_DELAY_RANGE.sample(this.mob.getRandom());
            }
        } else {
            this.updatePathDelay = 0;
            this.mob.getNavigation().stop();
        }

        this.mob.getLookControl().setLookAt(target, 30.0F, 30.0F);

        if (Gun.hasAmmo(mob.getGun())) {
            mob.performRangedAttack(target, 1);
        }
        else if(!EntityReloadTracker.isReloading(mob)) {
            EntityReloadTracker.addTracker(mob, HumanoidArm.RIGHT);
        }
//
//        else if (reloadTimer == -1) {
//            setReloading(true);
//            reloadTimer = gun.getGun().getGeneral().getReloadTime();
//        }
//        else if (reloadTimer > 0) {
//            reloadTimer = Math.max(reloadTimer - 1, 0);
//        }
//        else if (reloadTimer == 0) {
//            Gun.fillAmmo(gunStack);
//            setReloading(false);
//            reloadTimer = -1;
//        }
    }

    private boolean canRun() {
        return !EntityReloadTracker.isReloading(mob);
    }

    public void performRangedAttack() {
        var msg = new C2SMessageShoot(mob.getId(),
                mob.getViewYRot(1),
                mob.getViewXRot(1),
                0, 0, true);

        ServerPlayHandler.handleShoot(msg, mob);
    }

    public static void shoot(LivingEntity shooter, boolean isRightHand){
        var msg = new C2SMessageShoot(shooter.getId(),
                shooter.getViewYRot(1),
                shooter.getViewXRot(1),
                0, 0, isRightHand);

        ServerPlayHandler.handleShoot(msg, shooter);
    }
}
