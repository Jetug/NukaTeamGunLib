package com.nukateam.example.common.foundation.items.guns;

import com.nukateam.gunscore.common.foundation.item.GunItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;

public class ShotGun extends GunItem {
    public ShotGun(Properties properties) {
        super(properties);
    }

    @Override
    public boolean canApplyAtEnchantingTable(ItemStack stack, Enchantment enchantment) {
        return false;
    }

    @Override
    public boolean isFoil(ItemStack p_41453_) {
        return false;
    }
}
