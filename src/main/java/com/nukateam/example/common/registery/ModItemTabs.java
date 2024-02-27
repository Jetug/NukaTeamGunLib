package com.nukateam.example.common.registery;

import com.nukateam.gunscore.common.foundation.ModGuns;
import com.nukateam.gunscore.common.foundation.enchantment.EnchantmentTypes;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;

public class ModItemTabs {
    public static final CreativeModeTab WEAPONS = new CreativeModeTab("nuka_equip") {
        @Override
        public ItemStack makeIcon() {
            return new ItemStack(ModGuns.ROUND10MM.get());
        }
    }.setEnchantmentCategories(EnchantmentTypes.GUN, EnchantmentTypes.SEMI_AUTO_GUN);

}
