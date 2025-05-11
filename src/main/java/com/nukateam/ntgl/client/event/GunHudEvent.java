package com.nukateam.ntgl.client.event;

import com.nukateam.ntgl.client.render.hud.GunHudCache;
import net.minecraft.world.InteractionHand;
import net.minecraftforge.eventbus.api.Cancelable;
import net.minecraftforge.eventbus.api.Event;

@Cancelable
public class GunHudEvent extends Event {
    private final InteractionHand hand;
    private final GunHudCache cache;

    public GunHudEvent(InteractionHand hand, GunHudCache cache) {

        this.hand = hand;
        this.cache = cache;
    }

    public InteractionHand getHand() {
        return hand;
    }

    public GunHudCache getCache() {
        return cache;
    }
}
