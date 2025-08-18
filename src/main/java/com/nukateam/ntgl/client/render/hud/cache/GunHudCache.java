package com.nukateam.ntgl.client.render.hud.cache;

import com.nukateam.ntgl.common.data.config.AmmoConfig;
import com.nukateam.ntgl.common.data.config.Fuel;
import com.nukateam.ntgl.common.data.holders.AmmoHolder;
import com.nukateam.ntgl.common.data.holders.AmmoType;
import com.nukateam.ntgl.common.data.holders.CounterType;
import com.nukateam.ntgl.common.data.holders.FireMode;
import net.minecraft.world.InteractionHand;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;

public class GunHudCache extends HudCache {
    public FireMode fireMode = FireMode.SEMI_AUTO;
    public HashMap<AmmoHolder, Fuel> fuels = new HashMap<>();

    public GunHudCache(InteractionHand hand){
        super(hand);
    }
}
