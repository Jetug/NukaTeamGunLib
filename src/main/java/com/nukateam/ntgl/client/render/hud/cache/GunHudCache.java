package com.nukateam.ntgl.client.render.hud.cache;

import com.nukateam.ntgl.common.data.config.AmmoConfig;
import com.nukateam.ntgl.common.data.config.Fuel;
import com.nukateam.ntgl.common.data.holders.*;
import net.minecraft.world.InteractionHand;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;

public class GunHudCache{
    public FireMode fireMode = FireMode.SEMI_AUTO;
    public HashMap<AmmoHolder, Fuel> fuels = new HashMap<>();
    public final InteractionHand hand;
    public long checkAmmoTimestamp = -1L;
    public int inventoryAmmoCount = 0;
    public int maxAmmoCount = 0;
    public int ammoCount = 0;
    public AmmoConfig ammoConfig = new AmmoConfig();
    public ThrowMode throwMode = ThrowMode.SAFE;

    public GunHudCache(InteractionHand hand){
        this.hand = hand;
    }
}
