package com.nukateam.chassis_core.client.events;

import com.nukateam.chassis_core.common.foundation.registery.ContainerRegistry;
import net.minecraft.client.gui.screens.MenuScreens;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import com.nukateam.chassis_core.client.gui.screen.*;

@EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class ModScreensEvent {
    @SubscribeEvent
    public static void clientLoad(FMLClientSetupEvent event) {
        event.enqueueWork(() -> {
            MenuScreens.register(ContainerRegistry.CHASSIS_MENU.get(), DynamicChassisScreen::new);
        });
    }
}
