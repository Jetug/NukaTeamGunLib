package com.nukateam.ntgl.common.event;

import com.nukateam.ntgl.common.data.GunData;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.eventbus.api.Cancelable;

import java.util.ArrayList;

public class MeleeAttackEvent extends LivingEvent {
    private final GunData data;
    private final InteractionHand hand;
    private final ArrayList<LivingEntity> targets;

    public MeleeAttackEvent(LivingEntity entity, GunData data, InteractionHand hand, ArrayList<LivingEntity> targets) {
        super(entity);
        this.data = data;
        this.hand = hand;
        this.targets = targets;
    }

    public GunData getData() {
        return data;
    }

    public InteractionHand getHand() {
        return hand;
    }

    public ArrayList<LivingEntity> getTargets() {
        return targets;
    }

    /**
     * @return Whether this event was fired on the client side
     */
    public boolean isClient() {
        return this.getEntity().getCommandSenderWorld().isClientSide();
    }

    @Cancelable
    public static class Pre extends MeleeAttackEvent {
        public Pre(LivingEntity entity, GunData stack, InteractionHand hand, ArrayList<LivingEntity> targets) {
            super(entity, stack, hand, targets);
        }
    }

    public static class Post extends MeleeAttackEvent {
        public Post(LivingEntity entity, GunData stack, InteractionHand hand, ArrayList<LivingEntity> targets) {
            super(entity, stack, hand, targets);
        }
    }
}
