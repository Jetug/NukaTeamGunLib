package com.nukateam.ntgl.client.util.handler;

import com.nukateam.ntgl.common.foundation.item.interfaces.IWeapon;

public final class ShootingData {
    public int fireTimer;
    public IWeapon gun;

    public ShootingData(int fireTimer, IWeapon gun) {
        this.fireTimer = fireTimer;
        this.gun = gun;
    }
}
