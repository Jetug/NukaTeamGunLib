package com.nukateam.ntgl.common.data.util;

import net.minecraft.world.item.ItemStack;

import java.util.concurrent.atomic.AtomicInteger;

import static com.nukateam.ntgl.common.data.util.GunModifierHelper.getGun;

public class ModifiedGunProperties {
    public static int getMaxAmmo(ItemStack weapon) {
        var gun = getGun(weapon);
        return GunModifierHelper.getMaxAmmo(weapon, gun.getGeneral().getMaxAmmo(weapon));
    }
}
