package com.nukateam.ntgl.modules.datapack.handlers;

import com.nukateam.ntgl.Ntgl;
import com.nukateam.ntgl.common.network.PacketHandler;
import com.nukateam.ntgl.common.network.message.chassis.S2CMessageUpdateChassisConfig;
import com.nukateam.ntgl.common.network.message.chassis.S2CMessageUpdateEquipmentConfig;
import com.nukateam.ntgl.common.network.message.weapon.S2CMessageUpdateAmmo;
import com.nukateam.ntgl.common.network.message.weapon.S2CMessageUpdateAttachments;
import com.nukateam.ntgl.common.network.message.weapon.S2CMessageUpdateWeapons;
import com.nukateam.ntgl.common.network.message.weapon.S2CMessageUpdateAmmo;
import com.nukateam.ntgl.common.network.message.weapon.S2CMessageUpdateAttachments;
import com.nukateam.ntgl.common.network.message.weapon.S2CMessageUpdateProjectiles;
import com.nukateam.ntgl.common.network.message.weapon.S2CMessageUpdateWeapons;
import com.nukateam.ntgl.modules.datapack.managers.*;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.event.AddReloadListenerEvent;
import net.neoforged.neoforge.event.OnDatapackSyncEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.server.ServerStoppedEvent;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;

@EventBusSubscriber(modid = Ntgl.MOD_ID)
public class NetworkManagerHandler {
    @SubscribeEvent
    public static void onServerStopped(ServerStoppedEvent event) {
        NetworkWeaponManager.onServerStopped();
        NetworkAmmoManager.onServerStopped();
        NetworkProjectileManager.onServerStopped();
        NetworkAttachmentManager.onServerStopped();
        NetworkChassisManager.stop();
        NetworkEquipmentManager.stop();
    }

    @SubscribeEvent
    public static void addReloadListenerEvent(AddReloadListenerEvent event) {
        NetworkWeaponManager.register(event);
        NetworkAmmoManager.register(event);
        NetworkProjectileManager.register(event);
        NetworkAttachmentManager.register(event);
        NetworkChassisManager.register(event);
        NetworkEquipmentManager.register(event);
    }

    @SubscribeEvent
    public static void onPlayerLoggedIn(PlayerEvent.PlayerLoggedInEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            PacketHandler.getPlayChannel().sendToPlayer(() -> player, new S2CMessageUpdateWeapons());
            PacketHandler.getPlayChannel().sendToPlayer(() -> player, new S2CMessageUpdateAmmo());
            PacketHandler.getPlayChannel().sendToPlayer(() -> player, new S2CMessageUpdateProjectiles());
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
            PacketHandler.getPlayChannel().sendToAll(new S2CMessageUpdateProjectiles());
            PacketHandler.getPlayChannel().sendToAll(new S2CMessageUpdateAttachments());
            PacketHandler.getPlayChannel().sendToAll(new S2CMessageUpdateChassisConfig());
            PacketHandler.getPlayChannel().sendToAll(new S2CMessageUpdateEquipmentConfig());
        }
    }
}
