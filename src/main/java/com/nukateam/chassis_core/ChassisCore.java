package com.nukateam.chassis_core;

import com.nukateam.chassis_core.common.foundation.registery.ContainerRegistry;
import com.nukateam.chassis_core.common.foundation.registery.ItemRegistry;
import com.nukateam.chassis_core.modules.example.Example;
import com.nukateam.ntgl.Ntgl;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import org.slf4j.Logger;

public class ChassisCore {
    public static final String MOD_ID = Ntgl.MOD_ID;
    public static final Logger LOGGER = Ntgl.LOGGER;

    public ChassisCore(IEventBus MOD_EVENT_BUS) {
        ItemRegistry.register(MOD_EVENT_BUS);
        ContainerRegistry.register(MOD_EVENT_BUS);
        Example.init(MOD_EVENT_BUS);
    }
}