package com.nukateam.ntgl.modules.packs;

import com.nukateam.ntgl.Ntgl;
import net.minecraftforge.eventbus.api.IEventBus;
import org.slf4j.Logger;

public class GunPackModule {
    public static final String MOD_ID = Ntgl.MOD_ID;
    public static final Logger LOGGER = Ntgl.LOGGER;

    public static void init(IEventBus eventBus) {
//        GeneratedGunItems.register(eventBus);
//        WeaponConfigExtractor.findGuns();
        NTGLPackManager.scanPacks();
    }

    public static void createItems(IEventBus eventBus) {
    }
}
