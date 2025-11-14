package com.nukateam.ntgl.client.event;

import com.nukateam.ntgl.client.render.hud.WeaponHud;
import com.nukateam.ntgl.client.render.hud.cache.GunHudCache;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.world.InteractionHand;
import net.neoforged.bus.api.Event;
import net.neoforged.bus.api.ICancellableEvent;

public class GunHudEvent extends Event implements ICancellableEvent {
    private final InteractionHand hand;
    private final GuiGraphics graphics;
    private final GunHudCache cache;
    private final GunHudEvent.Phase phase;


    public GunHudEvent(InteractionHand hand, GuiGraphics graphics, GunHudCache cache, Phase phase) {
        this.hand = hand;
        this.graphics = graphics;
        this.cache = cache;
        this.phase = phase;
    }

    public InteractionHand getHand() {
        return hand;
    }

    public GunHudCache getCache() {
        return cache;
    }

    public GuiGraphics getGraphics() {
        return graphics;
    }

    public Phase getRenderPhase() {
        return phase;
    }

    public enum Phase {
        START, END;
    }
}
