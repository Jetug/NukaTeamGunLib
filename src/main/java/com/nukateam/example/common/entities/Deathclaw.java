
package com.nukateam.example.common.entities;

import mod.azure.azurelib.animatable.GeoEntity;
import mod.azure.azurelib.core.animatable.instance.AnimatableInstanceCache;
import mod.azure.azurelib.core.animation.AnimationController;
import mod.azure.azurelib.util.AzureLibUtil;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.*;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraftforge.common.util.Lazy;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import static mod.azure.azurelib.core.animation.AnimatableManager.ControllerRegistrar;
import static mod.azure.azurelib.core.animation.Animation.LoopType.PLAY_ONCE;
import static mod.azure.azurelib.core.animation.RawAnimation.begin;
import static net.minecraft.network.syncher.SynchedEntityData.defineId;

public class Deathclaw extends PathfinderMob implements GeoEntity {
    public static final EntityDataAccessor<Boolean> IS_RUNNING =
            defineId(Deathclaw.class, EntityDataSerializers.BOOLEAN);
    public static final String SWIM = "swim";
    public static final String SWIM_START = "swim_start";
    public static final String SWIM_END = "swim_end";

    private final AnimatableInstanceCache cache = AzureLibUtil.createInstanceCache(this);
    private final boolean isServerSide = !level().isClientSide;

    private boolean startAttacking = false;
    private String[] attackAnims = new String[]{
            "attack_right",
            "attack_left",
            "attack_both"
    };
    private String attackAnimName;

    public Deathclaw(EntityType<? extends PathfinderMob> entityType, Level level) {
        super(entityType, level);
        getEntityData().set(IS_RUNNING, true);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, 200)
                .add(Attributes.ATTACK_DAMAGE, 10.0D)
                .add(Attributes.KNOCKBACK_RESISTANCE, 1.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.3)
                .add(Attributes.FOLLOW_RANGE, 48);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(8, new LookAtPlayerGoal(this, Player.class, 8.0F));
        this.goalSelector.addGoal(8, new RandomLookAroundGoal(this));
        this.goalSelector.addGoal(4, new MeleeAttackGoal(this, 1.5D, false));
        this.goalSelector.addGoal(1, new FloatGoal(this));
        this.goalSelector.addGoal(7, new WaterAvoidingRandomStrollGoal(this, 1.0D));
        this.targetSelector.addGoal(1, new HurtByTargetGoal(this, this.getClass()));
        this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, true));
        this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Raider.class, true));
        this.targetSelector.addGoal(3, new NearestAttackableTargetGoal<>(this, Animal.class, false));
    }

    @Nullable
    @Override
    public SpawnGroupData finalizeSpawn(ServerLevelAccessor pLevel, DifficultyInstance pDifficulty, MobSpawnType pReason,
                                        @Nullable SpawnGroupData pSpawnData, @Nullable CompoundTag pDataTag) {
        return super.finalizeSpawn(pLevel, pDifficulty, pReason, pSpawnData, pDataTag);
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        entityData.define(IS_RUNNING, true);
    }

    @Override
    public void setTarget(@Nullable LivingEntity pTarget) {
        super.setTarget(pTarget);

        if (isServerSide) {
            setIsRunning(pTarget != null);
        }
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return cache;
    }

    @Override
    public void registerControllers(ControllerRegistrar controllers) {
        controllers.add(new AnimationController<>(this, "controllerName", 0, animateArms()));
    }

    public void setIsRunning(boolean isRunning) {
        getEntityData().set(IS_RUNNING, isRunning);

        if (isRunning)
            setSpeed(2f);
        else setSpeed((float) getAttributeValue(Attributes.MOVEMENT_SPEED));
    }

    @NotNull
    private AnimationController.AnimationStateHandler<Deathclaw> animateArms() {
        return event -> {
            var controller = event.getController();
            var animation = begin();
            controller.setAnimationSpeed(1);

            if(isInWater()){
                animation.then(SWIM_START, PLAY_ONCE).thenLoop(SWIM);
            }
            else {
                var currentAnimation = controller.getCurrentAnimation();
                if(currentAnimation != null && currentAnimation.animation().name().equals(SWIM)){
                    animation.thenPlay(SWIM_END);
                }

                if (attackAnim > 0) {
                    if (!startAttacking) {
                        startAttacking = true;
                        attackAnimName = attackAnims[random.nextInt(0, attackAnims.length)];
                    }
//                    controller.setAnimationSpeed(2);
                    animation.thenLoop(attackAnimName);

                }
                else if (event.isMoving()) {
                    var isRunning = getEntityData().get(IS_RUNNING);
                    animation = isRunning ? animation.thenLoop("run") : animation.thenLoop("walk");
                } else {
                    animation.thenLoop("idle");
                }
            }
            return event.setAndContinue(animation);
        };
    }
}