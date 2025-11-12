package com.nukateam.chassis_core.common.network.managers;

import com.nukateam.chassis_core.ChassisCore;
import com.nukateam.chassis_core.common.network.PacketHandler;
import com.nukateam.chassis_core.common.network.packet.S2CMessageUpdateChassisConfig;
import com.nukateam.chassis_core.common.network.packet.S2CMessageUpdateEquipmentConfig;
import net.minecraftforge.event.AddReloadListenerEvent;
import net.minecraftforge.event.OnDatapackSyncEvent;
import net.minecraftforge.event.server.ServerStoppedEvent;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;

@EventBusSubscriber(modid = ChassisCore.MOD_ID)
public class DatapackHandler {
    @SubscribeEvent
    public static void onServerStopped(ServerStoppedEvent event) {
        NetworkChassisManager.stop();
        NetworkEquipmentManager.stop();
    }

    @SubscribeEvent
    public static void addReloadListenerEvent(AddReloadListenerEvent event) {
        NetworkChassisManager.register(event);
        NetworkEquipmentManager.register(event);
    }

    @SubscribeEvent
    public static void onDatapackSync(OnDatapackSyncEvent event) {
        if (event.getPlayer() == null) {
            PacketHandler.getPlayChannel().sendToAll(new S2CMessageUpdateChassisConfig());
            PacketHandler.getPlayChannel().sendToAll(new S2CMessageUpdateEquipmentConfig());
        }
    }
}
