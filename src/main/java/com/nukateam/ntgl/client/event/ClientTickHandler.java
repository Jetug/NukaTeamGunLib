
package com.nukateam.ntgl.client.event;

import com.nukateam.geo.render.ItemAnimator;
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
    private static final Map<ItemAnimator, Runnable> tickingAnimators = new HashMap();
    private static final Map<Object, Consumer<TickEvent>> tickers = new HashMap();

    public ClientTickHandler() {
    }

    public static void addTicker(ItemAnimator animator, Runnable onTick) {
        tickingAnimators.put(animator, onTick);
    }

    public static void addTicker(Object object, Consumer<TickEvent> onTick) {
        tickers.put(object, onTick);
    }

    @SubscribeEvent
    public static void clientTick(TickEvent.ClientTickEvent event) {
        if (event.phase == TickEvent.Phase.START) {
            tickingAnimators.forEach((k, v) -> v.run());
        }

        tickers.forEach((k, v) -> v.accept(event));
    }
}