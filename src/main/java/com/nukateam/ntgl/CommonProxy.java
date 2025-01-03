package com.nukateam.ntgl;

import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.HashMap;

import static com.nukateam.ntgl.ClientProxy.damageTypes;

@Mod.EventBusSubscriber(modid = Ntgl.MOD_ID)
public class CommonProxy {
    @SubscribeEvent
    public static void onServerTick(TickEvent.ServerTickEvent event) {
        if (event.phase == TickEvent.Phase.START) {
            var buffMap = new HashMap<>(damageTypes);

            buffMap.forEach((key, value) -> {
                if (value.ticks <= 0) {
                    damageTypes.remove(key);
                }
                value.ticks--;
            });
        }
    }
}
