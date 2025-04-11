package com.nukateam.ntgl.client.render.hud;

import com.nukateam.ntgl.common.base.holders.AmmoType;
import com.nukateam.ntgl.common.base.holders.FireMode;
import net.minecraft.world.InteractionHand;

public class GunHudCache {
    public final InteractionHand hand;
    public long checkAmmoTimestamp = -1L;
    public int inventoryAmmoCount = 0;
    public int maxAmmoCount = 0;
    public int ammoCount = 0;
    public AmmoType ammoType = AmmoType.STANDARD;
    public FireMode fireMode = FireMode.SEMI_AUTO;

    public GunHudCache(InteractionHand hand){
        this.hand = hand;
    }
}
