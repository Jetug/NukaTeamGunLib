package com.nukateam.chassis_core.modules.example.common;

import com.nukateam.chassis_core.ChassisCore;
import com.nukateam.chassis_core.modules.example.common.entities.ExampleChassis;
import net.neoforged.neoforge.event.entity.EntityAttributeCreationEvent;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;

import static com.nukateam.chassis_core.modules.example.common.registery.EntityTypes.EXAMPLE_CHASSIS;

@EventBusSubscriber(modid = ChassisCore.MOD_ID)
public class ModEventBusEvents {
    @SubscribeEvent
    public static void registerAttributes(EntityAttributeCreationEvent event) {
        event.put(EXAMPLE_CHASSIS.get(), ExampleChassis.createAttributes().build());
    }
}