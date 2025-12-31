package com.nukateam.ntgl.modules.datapack.handlers;

import com.nukateam.chassis_core.common.network.packet.S2CMessageUpdateChassisConfig;
import com.nukateam.chassis_core.common.network.packet.S2CMessageUpdateEquipmentConfig;
import com.nukateam.ntgl.Ntgl;
import com.nukateam.ntgl.common.network.PacketHandler;
import com.nukateam.ntgl.common.network.message.*;
import com.nukateam.ntgl.modules.datapack.managers.*;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.event.AddReloadListenerEvent;
import net.minecraftforge.event.OnDatapackSyncEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.server.ServerStoppedEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = Ntgl.MOD_ID)
public class NetworkManagerHandler {
    @SubscribeEvent
    public static void onServerStopped(ServerStoppedEvent event) {
        NetworkWeaponManager.onServerStopped();
        NetworkAmmoManager.onServerStopped();
        NetworkAttachmentManager.onServerStopped();
        NetworkChassisManager.stop();
        NetworkEquipmentManager.stop();
    }

    @SubscribeEvent
    public static void addReloadListenerEvent(AddReloadListenerEvent event) {
        NetworkWeaponManager.register(event);
        NetworkAmmoManager.register(event);
        NetworkAttachmentManager.register(event);
        NetworkChassisManager.register(event);
        NetworkEquipmentManager.register(event);
    }

    @SubscribeEvent
    public static void onPlayerLoggedIn(PlayerEvent.PlayerLoggedInEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            PacketHandler.getPlayChannel().sendToPlayer(() -> player, new S2CMessageUpdateWeapons());
            PacketHandler.getPlayChannel().sendToPlayer(() -> player, new S2CMessageUpdateAmmo());
            PacketHandler.getPlayChannel().sendToPlayer(() -> player, new S2CMessageUpdateAttachments());
            PacketHandler.getPlayChannel().sendToPlayer(() -> player, new S2CMessageUpdateChassisConfig());
            PacketHandler.getPlayChannel().sendToPlayer(() -> player, new S2CMessageUpdateEquipmentConfig());
        }
    }

    @SubscribeEvent
    public static void onDatapackSync(OnDatapackSyncEvent event) {
        if (event.getPlayer() == null) {
            PacketHandler.getPlayChannel().sendToAll(new S2CMessageUpdateWeapons());
            PacketHandler.getPlayChannel().sendToAll(new S2CMessageUpdateAmmo());
            PacketHandler.getPlayChannel().sendToAll(new S2CMessageUpdateAttachments());
            PacketHandler.getPlayChannel().sendToAll(new S2CMessageUpdateChassisConfig());
            PacketHandler.getPlayChannel().sendToAll(new S2CMessageUpdateEquipmentConfig());
        }
    }
}
