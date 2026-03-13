package com.nukateam.ntgl.common.event;

import com.nukateam.ntgl.common.data.WeaponData;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.ICancellableEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.entity.living.LivingEvent;
/**
 * <p>Fired when a player reloads a gun.</p>
 *
 * @author Ocelot
 */
public class GunReloadEvent extends LivingEvent {
    private final WeaponData data;
    private final InteractionHand hand;

    public GunReloadEvent(WeaponData data, InteractionHand hand) {
        super(data.wielder);
        this.data = data;
        this.hand = hand;
    }

    /**
     * @return The stack the player was holding when reloading the gun
     */
    public WeaponData getData() {
        return data;
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
    public static class Pre extends GunReloadEvent implements ICancellableEvent {
        public Pre(WeaponData data, InteractionHand hand) {
            super(data, hand);
        }
    }

    /**
     * <p>Fired after a player has started reloading a gun.</p>
     *
     * @author Ocelot
     */
    public static class Post extends GunReloadEvent {
        public Post(WeaponData data, InteractionHand hand) {
            super(data, hand);
        }
    }
}
