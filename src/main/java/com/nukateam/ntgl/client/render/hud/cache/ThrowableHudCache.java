package com.nukateam.ntgl.client.render.hud.cache;

import com.nukateam.ntgl.common.data.holders.AmmoType;
import com.nukateam.ntgl.common.data.holders.ThrowMode;
import net.minecraft.world.InteractionHand;

public class ThrowableHudCache extends HudCache {
    public ThrowMode throwMode = ThrowMode.SAFE;
    public AmmoType ammoType = AmmoType.EXPLOSIVE;

    public ThrowableHudCache(InteractionHand hand){
        super(hand);
    }
}
