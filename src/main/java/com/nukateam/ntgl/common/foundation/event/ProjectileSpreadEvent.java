package com.nukateam.ntgl.common.foundation.event;

import com.nukateam.ntgl.common.data.WeaponData;
import net.neoforged.bus.api.Event;

public class ProjectileSpreadEvent extends Event {
    private final WeaponData weaponData;
    private float spread;

    public ProjectileSpreadEvent(WeaponData weaponData, float spread) {
        this.weaponData = weaponData;
        this.spread = spread;
    }

    public WeaponData getWeaponData() {
        return weaponData;
    }

    public float getSpread() {
        return spread;
    }

    public void setSpread(float spread) {
        this.spread = spread;
    }
}
