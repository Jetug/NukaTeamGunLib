package com.nukateam.ntgl.client.render.hud.cache;

import com.nukateam.ntgl.common.data.config.weapon.AmmoConfig;
import com.nukateam.ntgl.common.data.config.weapon.Fuel;
import com.nukateam.ntgl.common.data.holders.*;
import net.minecraft.world.InteractionHand;

import java.util.*;

public class GunHudCache{
    public FireMode fireMode = FireMode.SEMI_AUTO;
    public HashMap<AmmoHolder, Fuel> fuels = new HashMap<>();
    public final InteractionHand hand;
    public long checkAmmoTimestamp = -1L;

    public ThrowMode throwMode = ThrowMode.SAFE;
    public boolean fireModeKey = false;
    public boolean ammoTypeKey = false;
    public boolean isThrowable = false;
    public LinkedHashMap<WeaponMode, ModeInfo> weaponModes = new LinkedHashMap<>(Map.of());

    public GunHudCache(InteractionHand hand){
        this.hand = hand;
    }

    public static class ModeInfo{
        public WeaponAction action;
        public int inventoryAmmoCount = 0;
        public int maxAmmoCount = 0;
        public int ammoCount = 0;
        public AmmoConfig ammoConfig = new AmmoConfig();
    }
}
