package com.nukateam.chassis_core.modules.example.client.events;

import com.nukateam.chassis_core.modules.example.client.screen.ExampleChassisScreen;
import com.nukateam.chassis_core.modules.example.client.screen.ExampleChassisStationScreen;
import com.nukateam.chassis_core.modules.example.common.registery.ContainerRegistry;
import net.minecraft.client.gui.screens.MenuScreens;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;

@EventBusSubscriber( value = Dist.CLIENT)
public class ModScreensClass {
    @SubscribeEvent
    public static void clientLoad(FMLClientSetupEvent event) {
        event.enqueueWork(() -> {
            MenuScreens.register(ContainerRegistry.EXAMPLE_CHASSIS_MENU.get(), ExampleChassisScreen::new);
            MenuScreens.register(ContainerRegistry.EXAMPLE_STATION_MENU.get(), ExampleChassisStationScreen::new);
        });
    }
}
