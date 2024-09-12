package com.nukateam.ntgl.common.data.interfaces;

import net.minecraft.world.entity.monster.RangedAttackMob;
import net.minecraft.world.item.ItemStack;

public interface IGunUser extends RangedAttackMob {
    ItemStack getGun();
}
