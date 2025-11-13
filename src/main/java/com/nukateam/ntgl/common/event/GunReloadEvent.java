package com.nukateam.ntgl.common.event;

import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.bus.api.Cancelable;

/**
 * <p>Fired when a player reloads a gun.</p>
 *
 * @author Ocelot
 */
public class GunReloadEvent extends PlayerEvent {
    private final ItemStack stack;
    private final InteractionHand hand;

    public GunReloadEvent(Player player, ItemStack stack, InteractionHand hand) {
        super(player);
        this.stack = stack;
        this.hand = hand;
    }

    /**
     * @return The stack the player was holding when reloading the gun
     */
    public ItemStack getStack() {
        return stack;
    }

    public InteractionHand getHand() {
        return hand;
    }

    /**
     * @return Whether or not this event was fired on the client side
     */
    public boolean isClient() {
        return this.getEntity().getCommandSenderWorld().isClientSide();
    }

    /**
     * <p>Fired when a player is about to reload a gun.</p>
     *
     * @author Ocelot
     */
    @Cancelable
    public static class Pre extends GunReloadEvent {
        public Pre(Player player, ItemStack stack, InteractionHand hand) {
            super(player, stack, hand);
        }
    }

    /**
     * <p>Fired after a player has started reloading a gun.</p>
     *
     * @author Ocelot
     */
    public static class Post extends GunReloadEvent {
        public Post(Player player, ItemStack stack, InteractionHand hand) {
            super(player, stack, hand);
        }
    }
}
