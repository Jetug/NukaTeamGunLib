package com.nukateam.ntgl.client.render.hud;

import com.nukateam.ntgl.common.data.holders.AmmoType;
import com.nukateam.ntgl.common.data.holders.FireMode;
import com.nukateam.ntgl.common.data.holders.GrenadeMode;
import net.minecraft.world.InteractionHand;

public class ThrowableHudCache {
    public final InteractionHand hand;
    public long checkAmmoTimestamp = -1L;
    public int inventoryAmmoCount = 0;
    public int ammoCount = 0;
    public GrenadeMode mode = GrenadeMode.SAFE;

    public ThrowableHudCache(InteractionHand hand){
        this.hand = hand;
    }
}
