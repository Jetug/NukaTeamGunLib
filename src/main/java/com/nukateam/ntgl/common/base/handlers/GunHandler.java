package com.nukateam.ntgl.common.base.handlers;

import com.nukateam.ntgl.common.data.config.gun.Gun;
import com.nukateam.ntgl.common.util.util.GunData;
import com.nukateam.ntgl.common.util.util.GunModifierHelper;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;

public class GunHandler {
    public void handleAmmoAfterShoot(GunData data) {
        var ammoPerShot = GunModifierHelper.getAmmoPerShot(data);
        var remainingAmmo =  Math.max(0, Gun.getAmmo(data.gun) - ammoPerShot);
        Gun.setAmmo(data.gun, remainingAmmo);
    }
}
