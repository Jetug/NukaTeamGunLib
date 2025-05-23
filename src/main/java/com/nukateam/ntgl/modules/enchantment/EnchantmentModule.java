package com.nukateam.ntgl.modules.enchantment;

import com.nukateam.ntgl.modules.gunpack.data.GunRegisterer;
import com.nukateam.ntgl.modules.gunpack.regestry.ModBlocks;
import com.nukateam.ntgl.modules.gunpack.resource.NTGLPackManager;
import net.minecraftforge.eventbus.api.IEventBus;

public class EnchantmentModule {
    public static void init(IEventBus eventBus) {
        ModEnchantments.REGISTER.register(eventBus);
    }
}
