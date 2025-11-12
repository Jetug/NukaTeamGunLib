package com.nukateam.ntgl.modules.datapack.handlers;

import com.nukateam.ntgl.Ntgl;
import com.nukateam.ntgl.common.network.PacketHandler;
import com.nukateam.ntgl.common.network.message.*;
import com.nukateam.ntgl.modules.datapack.managers.*;
import net.minecraftforge.event.AddReloadListenerEvent;
import net.minecraftforge.event.OnDatapackSyncEvent;
import net.minecraftforge.event.server.ServerStoppedEvent;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;

@EventBusSubscriber(modid = Ntgl.MOD_ID)
public class NetworkManagerHandler {
    @SubscribeEvent
    public static void onServerStopped(ServerStoppedEvent event) {
        NetworkWeaponManager.onServerStopped();
        NetworkAmmoManager.onServerStopped();
        NetworkAttachmentManager.onServerStopped();
    }

    @SubscribeEvent
    public static void addReloadListenerEvent(AddReloadListenerEvent event) {
        NetworkWeaponManager.register(event);
        NetworkAmmoManager.register(event);
        NetworkAttachmentManager.register(event);
    }

    @SubscribeEvent
    public static void onDatapackSync(OnDatapackSyncEvent event) {
        if (event.getPlayer() == null) {
            PacketHandler.getPlayChannel().sendToAll(new S2CMessageUpdateWeapons());
            PacketHandler.getPlayChannel().sendToAll(new S2CMessageUpdateAmmo());
            PacketHandler.getPlayChannel().sendToAll(new S2CMessageUpdateAttachments());
        }
    }
}
