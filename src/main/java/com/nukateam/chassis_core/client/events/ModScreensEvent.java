package com.nukateam.chassis_core.client.events;

import com.nukateam.chassis_core.common.foundation.registery.ContainerRegistry;
import net.minecraft.client.gui.screens.MenuScreens;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import com.nukateam.chassis_core.client.gui.screen.*;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;

@EventBusSubscriber( value = Dist.CLIENT)
public class ModScreensEvent {
    @SubscribeEvent
    public static void clientLoad(RegisterMenuScreensEvent event) {
        event.register(ContainerRegistry.CHASSIS_MENU.get(), DynamicChassisScreen::new);
    }
}
