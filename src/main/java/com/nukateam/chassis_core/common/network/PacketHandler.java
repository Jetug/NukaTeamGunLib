package com.nukateam.chassis_core.common.network;

import com.nukateam.chassis_core.ChassisCore;
import com.nukateam.chassis_core.common.network.managers.*;
import com.nukateam.chassis_core.common.network.packet.*;
import com.mrcrayfish.framework.api.FrameworkAPI;
import com.mrcrayfish.framework.api.network.FrameworkNetwork;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

public class PacketHandler {
    private static FrameworkNetwork PLAY_CHANNEL;

    public static FrameworkNetwork getPlayChannel() {
        return PLAY_CHANNEL;
    }

    public static void register() {
        PLAY_CHANNEL = FrameworkAPI.createNetworkBuilder(ResourceLocation.tryBuild(ChassisCore.MOD_ID, "cc_play"), 1)
                .registerPlayMessage("C2SActionPacket", C2SActionPacket.class, C2SActionPacket.STREAM_CODEC, C2SActionPacket::handle, PacketFlow.CLIENTBOUND)
                .registerPlayMessage("C2SGenericPacket", C2SGenericPacket.class, C2SGenericPacket.STREAM_CODEC, C2SGenericPacket::handle, PacketFlow.CLIENTBOUND)
                .registerPlayMessage("S2CInventoryPacket", S2CInventoryPacket.class, S2CInventoryPacket.STREAM_CODEC, S2CInventoryPacket::handle, PacketFlow.CLIENTBOUND)
                .registerPlayMessage("C2SActionPacket", S2CMessageUpdateChassisConfig.class, S2CMessageUpdateChassisConfig.STREAM_CODEC, S2CMessageUpdateChassisConfig::handle, PacketFlow.CLIENTBOUND)
                .registerPlayMessage("UpdateEquipmentConfig", S2CMessageUpdateEquipmentConfig.class, S2CMessageUpdateEquipmentConfig.STREAM_CODEC, S2CMessageUpdateEquipmentConfig::handle, PacketFlow.CLIENTBOUND)

                .build();

        FrameworkAPI.registerLoginData(ResourceLocation.tryBuild(ChassisCore.MOD_ID, "network_chassis_manager"), NetworkChassisManager.LoginData::new);
        FrameworkAPI.registerLoginData(ResourceLocation.tryBuild(ChassisCore.MOD_ID, "network_equipment_manager"), NetworkEquipmentManager.LoginData::new);
    }

    @SubscribeEvent
    public static void registerPayloadHandlers(RegisterPayloadHandlersEvent event) {
        PayloadRegistrar registrar = event.registrar(ChassisCore.MOD_ID);

//        registrar.playToClient(
//                ResourceLocation.fromNamespaceAndPath(ChassisCore.MOD_ID, "network_chassis_manager"),
//                FriendlyByteBuf::readResourceLocation, // или ваш формат данных
//                (payload, context) -> NetworkChassisManager.LoginDataHandler.handle(payload, context)
//        );

        registrar.playToClient(
                ResourceLocation.fromNamespaceAndPath(ChassisCore.MOD_ID, "network_equipment_manager"),
                FriendlyByteBuf::readResourceLocation, // или ваш формат данных
                (payload, context) -> NetworkEquipmentManager.LoginDataHandler.handle(payload, context)
        );
    }
}
