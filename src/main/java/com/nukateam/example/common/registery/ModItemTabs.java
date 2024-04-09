package com.nukateam.example.common.registery;

import com.nukateam.ntgl.Ntgl;
import com.nukateam.ntgl.common.foundation.enchantment.EnchantmentTypes;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;

public class ModItemTabs {
    public static final DeferredRegister<CreativeModeTab> TABS = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, Ntgl.MOD_ID);
}
