package com.nukateam.ntgl.client.render.hud.cache;

import com.nukateam.ntgl.common.data.holders.FireMode;
import net.minecraft.world.InteractionHand;

public class GunHudCache extends HudCache {
    public FireMode fireMode = FireMode.SEMI_AUTO;

    public GunHudCache(InteractionHand hand){
        super(hand);
    }
}
