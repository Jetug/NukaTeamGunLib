package com.nukateam.chassis_core.modules.example.client.events;

import com.nukateam.chassis_core.modules.example.client.screen.ExampleChassisScreen;
import com.nukateam.chassis_core.modules.example.client.screen.ExampleChassisStationScreen;
import com.nukateam.chassis_core.modules.example.common.registery.ContainerRegistry;
import com.nukateam.ntgl.Ntgl;
import net.minecraft.client.gui.screens.MenuScreens;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;

@EventBusSubscriber(modid = Ntgl.MOD_ID, value = Dist.CLIENT)
public class ModScreensClass {
    @SubscribeEvent
    public static void onRegisterMenuScreens(RegisterMenuScreensEvent event) {
        event.register(ContainerRegistry.EXAMPLE_CHASSIS_MENU.get(), ExampleChassisScreen::new);
        event.register(ContainerRegistry.EXAMPLE_STATION_MENU.get(), ExampleChassisStationScreen::new);
    }
}
