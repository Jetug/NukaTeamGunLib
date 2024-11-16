package com.nukateam.ntgl.client.util.handler;

import com.nukateam.ntgl.common.foundation.item.GunItem;

public final class ShootingData {
    public int fireTimer;
    public GunItem gun;

    public ShootingData(int fireTimer, GunItem gun) {
        this.fireTimer = fireTimer;
        this.gun = gun;
    }
}
