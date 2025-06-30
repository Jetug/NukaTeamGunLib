package com.nukateam.ntgl.modules.datapack.handlers;

import com.jetug.chassis_core.ChassisCore;
import com.jetug.chassis_core.common.network.PacketHandler;
import com.nukateam.ntgl.common.network.message.S2CMessageUpdateAmmo;
import com.nukateam.ntgl.common.network.message.S2CMessageUpdateAttachments;
import com.nukateam.ntgl.common.network.message.S2CMessageUpdateGuns;
import com.nukateam.ntgl.modules.datapack.managers.NetworkAmmoManager;
import com.nukateam.ntgl.modules.datapack.managers.NetworkAttachmentManager;
import com.nukateam.ntgl.modules.datapack.managers.NetworkGunManager;
import net.minecraftforge.event.AddReloadListenerEvent;
import net.minecraftforge.event.OnDatapackSyncEvent;
import net.minecraftforge.event.server.ServerStoppedEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = ChassisCore.MOD_ID)
public class NetworkManagerHandler {
    @SubscribeEvent
    public static void onServerStopped(ServerStoppedEvent event) {
        NetworkGunManager.onServerStopped();
        NetworkAmmoManager.onServerStopped();
        NetworkAttachmentManager.onServerStopped();
    }

    @SubscribeEvent
    public static void addReloadListenerEvent(AddReloadListenerEvent event) {
        NetworkGunManager.register(event);
        NetworkAmmoManager.register(event);
        NetworkAttachmentManager.register(event);
    }

    @SubscribeEvent
    public static void onDatapackSync(OnDatapackSyncEvent event) {
        if (event.getPlayer() == null) {
            PacketHandler.getPlayChannel().sendToAll(new S2CMessageUpdateGuns());
            PacketHandler.getPlayChannel().sendToAll(new S2CMessageUpdateAmmo());
            PacketHandler.getPlayChannel().sendToAll(new S2CMessageUpdateAttachments());
        }
    }
}
