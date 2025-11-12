package com.nukateam.ntgl.modules.gunpack;

import com.nukateam.ntgl.Ntgl;
import com.nukateam.ntgl.modules.gunpack.data.GunRegisterer;
import com.nukateam.ntgl.modules.gunpack.regestry.ModBlocks;
import com.nukateam.ntgl.modules.gunpack.resource.NTGLPackManager;
import net.neoforged.bus.api.IEventBus;
import org.slf4j.Logger;

public class GunPackModule {
    public static final String MOD_ID = Ntgl.MOD_ID;
    public static final Logger LOGGER = Ntgl.LOGGER;

    public static void init(IEventBus eventBus) {
        GunRegisterer.init(eventBus);
        NTGLPackManager.scanPacks();
        ModBlocks.register(eventBus);
    }
}
