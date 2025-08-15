package com.nukateam.ntgl.common.util.helpers.context;

import com.nukateam.ntgl.common.data.holders.AmmoHolder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;

public interface IAmmoContext {
    ItemStack stack();
    void shrink(int amount, AmmoHolder ammoHolder, LivingEntity entity);
}
