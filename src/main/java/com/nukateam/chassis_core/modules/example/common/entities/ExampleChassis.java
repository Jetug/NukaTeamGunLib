package com.nukateam.chassis_core.modules.example.common.entities;

import com.nukateam.chassis_core.ChassisCore;
import com.nukateam.chassis_core.client.animators.HandAnimator;
import com.nukateam.chassis_core.common.foundation.entity.WearableChassis;
import com.nukateam.chassis_core.modules.example.common.container.ExampleChassisStationMenu;
import mod.azure.azurelib.core.animation.AnimatableManager;
import mod.azure.azurelib.core.animation.AnimationController;
import mod.azure.azurelib.core.animation.RawAnimation;
import mod.azure.azurelib.core.object.PlayState;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.level.Level;

import static com.nukateam.chassis_core.modules.example.common.ArmorChassisAnimation.*;
import static mod.azure.azurelib.core.animation.Animation.LoopType.LOOP;
import static mod.azure.azurelib.core.animation.Animation.LoopType.PLAY_ONCE;
import static mod.azure.azurelib.core.animation.RawAnimation.begin;

public class ExampleChassis extends WearableChassis {
    public static final ResourceLocation ICON
            = new ResourceLocation(ChassisCore.MOD_ID, "textures/items/power_armor_frame.png");
    public static final ExampleChassisHand HAND = new ExampleChassisHand();

    public RawAnimation currentAnimation = null;

    public ExampleChassis(EntityType<? extends WearableChassis> type, Level worldIn) {
        super(type, worldIn);
    }

    @Override
    public boolean hurt(DamageSource damageSource, float damage) {
        if (damageSource.is(DamageTypes.FALL)) {
            damageArmor(damageSource, damage);
            return false;
        } else return super.hurt(damageSource, damage);
    }

    @Override
    public boolean causeFallDamage(float height, float pMultiplier, DamageSource damageSource) {
        var damage = calculateFallDamage(height, pMultiplier);
        if (damage >= 0)
            hurt(damageSource, damage);
        return false;
    }

    @Override
    public ResourceLocation getIcon() {
        return ICON;
    }

    @Override
    public HandAnimator getHandEntity() {
        return HAND;
    }

//    @Override
//    public MenuProvider getMenuProvider() {
//        return new MenuProvider() {
//            @Override
//            public AbstractContainerMenu createMenu(int id, Inventory menu, Player player) {
//                return new ExampleChassisMenu(id, inventory, menu, ExampleChassis.this);
//            }
//
//            @Override
//            public Component getDisplayName() {
//                return ExampleChassis.this.getDisplayName();
//            }
//        };
//    }

    @Override
    protected MenuProvider getStantionMenuProvider() {
        return new MenuProvider() {
            @Override
            public AbstractContainerMenu createMenu(int id, Inventory menu, Player player) {
                return new ExampleChassisStationMenu(id, inventory, menu, ExampleChassis.this);
            }

            @Override
            public Component getDisplayName() {
                return ExampleChassis.this.getDisplayName();
            }
        };
    }

    public Boolean isWalking() {
        return speedometer.getSpeed() > 0;
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllerRegistrar) {
        controllerRegistrar.add(new AnimationController<>(this, "arm_controller", 0, animateArms()));
        controllerRegistrar.add(new AnimationController<>(this, "leg_controller", 0, animateLegs()));
    }

    private AnimationController.AnimationStateHandler<ExampleChassis> animateArms() {
        return event -> {
            var controller = event.getController();
            controller.setAnimationSpeed(1);
            RawAnimation animation = null;

            var passenger = getControllingPassenger();

            if (passenger != null) {
                if (passenger.attackAnim > 0) {
                    controller.setAnimationSpeed(2.0D);
                    animation = begin().then(HIT, PLAY_ONCE);
                } else if (hurtTime > 0) {
                    animation = begin().then(HURT, PLAY_ONCE);
                } else if (isWalking()) {
                    controller.setAnimationSpeed(speedometer.getSpeed() * 4.0D);
                    animation = begin().then(WALK_ARMS, LOOP);
                } else animation = begin().then(IDLE, LOOP);
            }

            currentAnimation = animation;
            return animation != null ? event.setAndContinue(animation) : PlayState.STOP;
        };
    }

    private AnimationController.AnimationStateHandler<ExampleChassis> animateLegs() {
        return event -> {
            var controller = event.getController();
            controller.setAnimationSpeed(1);
            RawAnimation animation;

            var passenger = getControllingPassenger();
            if (passenger == null) return PlayState.STOP;

            if (this.isWalking()) {
                if (passenger.isShiftKeyDown()) {
                    animation = begin().then(SNEAK_WALK, LOOP);
                } else {
                    animation = begin().then(WALK_LEGS, LOOP);
                    controller.setAnimationSpeed(speedometer.getSpeed() * 4.0D);
                }
            } else if (passenger.isShiftKeyDown()) {
                animation = begin().then(SNEAK_END, LOOP);
            } else {
                return PlayState.STOP;
            }
            return event.setAndContinue(animation);
        };
    }
}
