package com.nukateam.ntgl.client.render.hud.cache;

import com.nukateam.ntgl.common.data.holders.AmmoHolder;
import com.nukateam.ntgl.common.data.holders.CounterType;
import com.nukateam.ntgl.common.data.holders.FireMode;
import net.minecraft.world.InteractionHand;

import java.util.Set;

public class GunHudCache extends HudCache {
    public FireMode fireMode = FireMode.SEMI_AUTO;
    public CounterType counterType = CounterType.NUMBER;
    public Set<AmmoHolder> fuels;

    public GunHudCache(InteractionHand hand){
        super(hand);
    }
}
