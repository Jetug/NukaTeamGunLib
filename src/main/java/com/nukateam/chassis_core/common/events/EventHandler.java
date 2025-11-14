package com.nukateam.chassis_core.common.events;

import com.nukateam.chassis_core.Global;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;

@EventBusSubscriber
public class EventHandler {
    @SubscribeEvent
    public static void onTick(ClientTickEvent.Post event) {
        Global.CLIENT_TIMER.tick();
    }
}