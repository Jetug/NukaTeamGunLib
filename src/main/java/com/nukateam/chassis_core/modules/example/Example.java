package com.nukateam.chassis_core.modules.example;

import com.nukateam.chassis_core.modules.example.common.registery.ChassisArmorItems;
import com.nukateam.chassis_core.modules.example.common.registery.ContainerRegistry;
import com.nukateam.chassis_core.modules.example.common.registery.EntityTypes;
import net.neoforged.bus.api.IEventBus;

public class Example {


    public static void init(IEventBus modEventBus) {
        ContainerRegistry.register(modEventBus);
        EntityTypes.register(modEventBus);
        ChassisArmorItems.register(modEventBus);
    }
}
