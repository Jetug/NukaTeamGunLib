package com.nukateam.ntgl.client.render.hud.cache;

import com.nukateam.ntgl.common.data.config.AmmoConfig;
import net.minecraft.world.InteractionHand;

public class HudCache {
    public final InteractionHand hand;
    public long checkAmmoTimestamp = -1L;
    public int inventoryAmmoCount = 0;
    public int maxAmmoCount = 0;
    public int ammoCount = 0;
    public AmmoConfig ammoConfig = new AmmoConfig();

    public HudCache(InteractionHand hand){
        this.hand = hand;
    }
}
