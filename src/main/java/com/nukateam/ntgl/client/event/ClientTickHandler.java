
package com.nukateam.ntgl.client.event;

import com.nukateam.ntgl.Ntgl;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.fml.common.Mod;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

@Mod.EventBusSubscriber(modid = Ntgl.MOD_ID)
public class ClientTickHandler {
    private static final Map<Object, Consumer<TickEvent>> clientTickers = new HashMap<>();
    private static final Map<Object, Consumer<TickEvent>> tickers = new HashMap<>();

    public static void addTicker(Object object, Consumer<TickEvent> onTick){
        tickers.put(object, onTick);
    }

    public static void addClientTicker(Object object, Consumer<TickEvent> onTick){
        clientTickers.put(object, onTick);
    }

    @SubscribeEvent()
    public static void clientTick(TickEvent event) {
        tickers.forEach((k, v) -> {
            v.accept(event);
        });
        if(event.side == LogicalSide.CLIENT) {
            clientTickers.forEach((k, v) -> {
                v.accept(event);
            });
        }
    }
}
