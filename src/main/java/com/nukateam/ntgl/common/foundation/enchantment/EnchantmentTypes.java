package com.nukateam.ntgl.common.foundation.enchantment;

import com.nukateam.ntgl.common.foundation.item.GunItem;
import com.nukateam.ntgl.GunMod;
import net.minecraft.world.item.enchantment.EnchantmentCategory;

/**
 * Author: MrCrayfish
 */
public class EnchantmentTypes {
    public static final EnchantmentCategory GUN = EnchantmentCategory.create(GunMod.MOD_ID + ":gun", item -> item instanceof GunItem);
    public static final EnchantmentCategory SEMI_AUTO_GUN = EnchantmentCategory.create(GunMod.MOD_ID + ":semi_auto_gun", item -> item instanceof GunItem && !((GunItem) item).getGun().getGeneral().isAuto());
}
