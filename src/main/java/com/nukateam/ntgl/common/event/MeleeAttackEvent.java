package com.nukateam.ntgl.common.event;

import com.nukateam.ntgl.common.data.WeaponData;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.neoforge.event.entity.living.LivingEvent;
import net.neoforged.bus.api.Cancelable;

import java.util.ArrayList;

public class MeleeAttackEvent extends LivingEvent {
    private final WeaponData data;
    private final InteractionHand hand;
    private final ArrayList<LivingEntity> targets;

    public MeleeAttackEvent(LivingEntity entity, WeaponData data, InteractionHand hand, ArrayList<LivingEntity> targets) {
        super(entity);
        this.data = data;
        this.hand = hand;
        this.targets = targets;
    }

    public WeaponData getData() {
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
        public Pre(LivingEntity entity, WeaponData stack, InteractionHand hand, ArrayList<LivingEntity> targets) {
            super(entity, stack, hand, targets);
        }
    }

    public static class Post extends MeleeAttackEvent {
        public Post(LivingEntity entity, WeaponData stack, InteractionHand hand, ArrayList<LivingEntity> targets) {
            super(entity, stack, hand, targets);
        }
    }
}
