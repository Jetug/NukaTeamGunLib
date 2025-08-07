package com.nukateam.ntgl.client.event;

import com.nukateam.ntgl.client.render.hud.GunHud;
import com.nukateam.ntgl.client.render.hud.cache.GunHudCache;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.world.InteractionHand;
import net.minecraftforge.eventbus.api.Cancelable;
import net.minecraftforge.eventbus.api.Event;

@Cancelable
public class GunHudEvent extends Event {
    private final GunHud gunHud;
    private final InteractionHand hand;
    private final GuiGraphics graphics;
    private final GunHudCache cache;
    private final GunHudEvent.Phase phase;


    public GunHudEvent(GunHud gunHud, InteractionHand hand, GuiGraphics graphics, GunHudCache cache, Phase phase) {
        this.gunHud = gunHud;
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

    public GunHud getGunHud() {
        return gunHud;
    }

    public enum Phase {
        START, END;
    }
}
