package com.nukateam.ntgl;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;

import java.util.HashMap;

import static com.nukateam.ntgl.ClientProxy.damageTypes;

@EventBusSubscriber(modid = Ntgl.MOD_ID, value = Dist.CLIENT)
public class CommonProxy {
    @SubscribeEvent
    public static void onServerTick(ClientTickEvent.Pre event) {
        var buffMap = new HashMap<>(damageTypes);

        buffMap.forEach((key, value) -> {
                if (value.ticks <= 0) {
                    damageTypes.remove(key);
                }
                value.ticks--;
        });
    }
}
