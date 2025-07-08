package com.nukateam.ntgl.client.util.handler;

import com.nukateam.ntgl.common.foundation.item.WeaponItem;

public final class ShootingData {
    public int fireTimer;
    public WeaponItem gun;

    public ShootingData(int fireTimer, WeaponItem gun) {
        this.fireTimer = fireTimer;
        this.gun = gun;
    }
}
