package com.nukateam.ntgl.client.util.handler;

import net.minecraft.world.InteractionHand;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import java.util.HashMap;
import java.util.Map;

public class ClientEquipHandler {
    private final Map<InteractionHand, Integer> equipTicks = new HashMap<>(Map.of(
            InteractionHand.MAIN_HAND, 0,
            InteractionHand.OFF_HAND, 0));

    private static ClientEquipHandler instance;

    public static ClientEquipHandler get() {
        if (instance == null) {
            instance = new ClientEquipHandler();
        }
        return instance;
    }

    private ClientEquipHandler(){}

    @SubscribeEvent
    public void onPostClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END)
            return;
        tickHand(InteractionHand.MAIN_HAND);
        tickHand(InteractionHand.OFF_HAND);
    }

    private void tickHand(InteractionHand hand) {
        var ticks = equipTicks.get(hand);
        if(ticks > 0){
            equipTicks.put(hand, ticks - 1);
        }
    }

    public boolean isEquiping(InteractionHand hand) {
        var ticks = equipTicks.get(hand);
        return ticks > 0;
    }

    public void setEquiping(InteractionHand hand, int time) {
        equipTicks.put(hand, time);
    }
}
