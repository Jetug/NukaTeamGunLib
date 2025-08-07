package com.nukateam.ntgl.client.render.hud.cache;

import com.nukateam.ntgl.common.data.holders.AmmoType;
import net.minecraft.world.InteractionHand;

public class HudCache {
    public final InteractionHand hand;
    public long checkAmmoTimestamp = -1L;
    public int inventoryAmmoCount = 0;
    public int maxAmmoCount = 0;
    public int ammoCount = 0;
    public AmmoType ammoType = AmmoType.STANDARD;

    public HudCache(InteractionHand hand){
        this.hand = hand;
    }
}
