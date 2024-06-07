package com.nukateam.ntgl.common.event;

import com.nukateam.ntgl.common.helpers.PlayerHelper;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.eventbus.api.Cancelable;

/**
 * <p>Fired when a player shoots a gun.</p>
 *
 * @author Ocelot
 */
public class GunFireEvent extends LivingEvent {
    private final ItemStack stack;
    private final HumanoidArm arm;

    public GunFireEvent(LivingEntity entity, ItemStack stack, HumanoidArm arm) {
        super(entity);
        this.stack = stack;
        this.arm = arm;
    }

    /**
     * @return The stack the player was holding when firing the gun
     */
    public ItemStack getStack() {
        return stack;
    }

    /**
     * @return The stack the player was holding when firing the gun
     */
    public HumanoidArm getArm() {
        return arm;
    }

    public InteractionHand getHand() {
        return PlayerHelper.convertHand(arm);
    }

    /**
     * @return Whether or not this event was fired on the client side
     */
    public boolean isClient() {
        return this.getEntity().getCommandSenderWorld().isClientSide();
    }

    /**
     * <p>Fired when a player is about to shoot a bullet.</p>
     *
     * @author Ocelot
     */
    @Cancelable
    public static class Pre extends GunFireEvent {
        public Pre(LivingEntity entity, ItemStack stack, HumanoidArm hand) {
            super(entity, stack, hand);
        }
    }

    /**
     * <p>Fired after a player has shot a bullet.</p>
     *
     * @author Ocelot
     */
    public static class Post extends GunFireEvent {
        public Post(LivingEntity entity, ItemStack stack, HumanoidArm hand) {
            super(entity, stack, hand);
        }
    }
}
