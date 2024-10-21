package com.nukateam.ntgl.common.foundation.goals;

import com.nukateam.ntgl.common.data.interfaces.IGunUser;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.monster.RangedAttackMob;

public class EntityAIRangedAttack<T extends PathfinderMob & RangedAttackMob & IGunUser> extends Goal {
    /**
     * The entity the AI instance has been applied to
     */
    private final T mob;
    /**
     * The entity (as a RangedAttackMob) the AI instance has been applied to.
     */
    private final IGunUser rangedAttackEntityHost;
    private LivingEntity attackTarget;
    /**
     * A decrementing tick that spawns a ranged attack once this value reaches 0. It is then set back to the
     * maxRangedAttackTime.
     */
    private int rangedAttackTime;
    private double entityMoveSpeed;
    private int ticksTargetSeen;
    private int attackTimeVariance;
    /**
     * The maximum time the AI has to wait before peforming another ranged attack.
     */
    private int maxRangedAttackTime;
    private float attackRange;
    private float attackRange_2;

    //GUN HANDLING:
    private int maxBurstCount; //Total number of shots in burst.
    private int burstCount; //shots left in current burst.
    private int shotDelay; //delay between shots in burst.


//    public EntityAIRangedAttack(IRangedAttackMob p_i1649_1_, double p_i1649_2_, int p_i1649_4_, float p_i1649_5_)
//    {
//        this(p_i1649_1_, p_i1649_2_, p_i1649_4_, p_i1649_4_, p_i1649_5_);
//    }

    public EntityAIRangedAttack(IRangedAttackMob shooter, double moveSpeed, int attackTimeVariance, int attackTime, float attackRange, int maxBurstCount, int shotDelay) {
        this.rangedAttackTime = -1;

        if (!(shooter instanceof EntityLivingBase)) {
            throw new IllegalArgumentException("ArrowAttackGoal requires Mob implements RangedAttackMob");
        } else {
            this.rangedAttackEntityHost = shooter;
            this.mob = (EntityLiving) shooter;
            this.entityMoveSpeed = moveSpeed;
            this.attackTimeVariance = attackTimeVariance;
            this.maxRangedAttackTime = attackTime;
            this.attackRange = attackRange;
            this.attackRange_2 = attackRange * attackRange;
            this.setMutexBits(3);

            this.maxBurstCount = maxBurstCount;
            this.burstCount = maxBurstCount;
            this.shotDelay = shotDelay;
        }
    }

    /**
     * Returns whether the EntityAIBase should begin execution.
     */
    @Override
    public boolean canUse() {
        var target = this.mob.getTarget();

        if (target == null) {
            return false;
        } else {
            this.attackTarget = target;
            return true;
        }
    }

    /**
     * Returns whether an in-progress EntityAIBase should continue executing
     */
    public boolean continueExecuting() {
        return this.isValidTarget() && (this.canUse() || !this.mob.getNavigation().isDone()) && this.isHoldingGun();
    }

    /**
     * Resets the task
     */
    public void resetTask() {
        this.attackTarget = null;
        this.ticksTargetSeen = 0;
        this.rangedAttackTime = -1;
    }

    /**
     * Updates the task
     */
    @Override
    public void tick() {
        double distance = this.mob.distanceToSqr(this.attackTarget);
        boolean targetInSight = this.mob.getSensing().hasLineOfSight(attackTarget);

        if (targetInSight)
            ++this.ticksTargetSeen;
        else this.ticksTargetSeen = 0;

        if (distance <= (double) this.attackRange_2 && this.ticksTargetSeen >= 20) {
            this.mob.getNavigation().clearPath();
        } else {
            this.mob.getNavigation().tryMoveToEntityLiving(this.attackTarget, this.entityMoveSpeed);
        }

        this.mob.getLookHelper().setLookPositionWithEntity(this.attackTarget, 30.0F, 55.0F);
        float f;

        if (--this.rangedAttackTime == 0) {
            if (distance > (double) this.attackRange_2 || !targetInSight) {
                return;
            }

            f = MathHelper.sqrt(distance) / this.attackRange;

            float f1 = f;

            if (f < 0.1F) {
                f1 = 0.1F;
            }

            if (f1 > 1.0F) {
                f1 = 1.0F;
            }

            this.rangedAttackEntityHost.attackEntityWithRangedAttack(this.attackTarget, f1);

            if (maxBurstCount > 0) burstCount--;
            if (burstCount > 0) {
                this.rangedAttackTime = shotDelay;
            } else {
                burstCount = maxBurstCount;
                this.rangedAttackTime = MathHelper.floor(f * (float) (this.maxRangedAttackTime - this.attackTimeVariance) + (float) this.attackTimeVariance);
            }
        } else if (this.rangedAttackTime < 0) {
            f = MathHelper.sqrt(distance) / this.attackRange;
            this.rangedAttackTime = MathHelper.floor(f * (float) (this.maxRangedAttackTime - this.attackTimeVariance) + (float) this.attackTimeVariance);
        }
    }

    private boolean isValidTarget() {
        return this.mob.getTarget() != null && this.mob.getTarget().isAlive();
    }
}