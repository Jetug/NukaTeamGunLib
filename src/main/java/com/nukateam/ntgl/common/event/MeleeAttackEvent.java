package com.nukateam.ntgl.common.event;

import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.eventbus.api.Cancelable;

import java.util.ArrayList;

public class MeleeAttackEvent extends LivingEvent {
    private final ItemStack stack;
    private final InteractionHand arm;
    private final ArrayList<LivingEntity> targets;

    public MeleeAttackEvent(LivingEntity entity, ItemStack stack, InteractionHand arm, ArrayList<LivingEntity> targets) {
        super(entity);
        this.stack = stack;
        this.arm = arm;
        this.targets = targets;
    }

    public ItemStack getStack() {
        return stack;
    }


    public InteractionHand getHand() {
        return arm;
    }

    /**
     * @return Whether or not this event was fired on the client side
     */
    public boolean isClient() {
        return this.getEntity().getCommandSenderWorld().isClientSide();
    }

    @Cancelable
    public static class Pre extends MeleeAttackEvent {
        public Pre(LivingEntity entity, ItemStack stack, InteractionHand hand, ArrayList<LivingEntity> targets) {
            super(entity, stack, hand, targets);
        }
    }

    public static class Post extends MeleeAttackEvent {
        public Post(LivingEntity entity, ItemStack stack, InteractionHand hand, ArrayList<LivingEntity> targets) {
            super(entity, stack, hand, targets);
        }
    }
}
