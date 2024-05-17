package com.nukateam.ntgl.client.data.handler;

import com.nukateam.ntgl.common.foundation.item.GunItem;
import net.minecraft.world.item.ItemStack;

import java.util.Objects;

public final class ShootingData {
    public int fireTimer;
    public GunItem gun;

    public ShootingData(int fireTimer, GunItem gun) {
        this.fireTimer = fireTimer;
        this.gun = gun;
    }
}
