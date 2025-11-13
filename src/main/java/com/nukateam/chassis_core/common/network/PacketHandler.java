package com.nukateam.chassis_core.common.network;

import com.nukateam.chassis_core.ChassisCore;
import com.nukateam.chassis_core.common.network.packet.*;
import com.mrcrayfish.framework.api.FrameworkAPI;
import com.mrcrayfish.framework.api.network.FrameworkNetwork;
import com.mrcrayfish.framework.api.network.MessageDirection;
import net.minecraft.resources.ResourceLocation;

public class PacketHandler {
    private static FrameworkNetwork PLAY_CHANNEL;

    public static FrameworkNetwork getPlayChannel() {
        return PLAY_CHANNEL;
    }

    public static void register() {
        PLAY_CHANNEL = FrameworkAPI.createNetworkBuilder(ResourceLocation.tryBuild(ChassisCore.MOD_ID, "cc_play"), 1)
                .registerPlayMessage(C2SActionPacket.class, MessageDirection.PLAY_SERVER_BOUND)
                .registerPlayMessage(C2SGenericPacket.class, MessageDirection.PLAY_SERVER_BOUND)

                .registerPlayMessage(S2CInventoryPacket.class, MessageDirection.PLAY_CLIENT_BOUND)
                .registerPlayMessage(S2CMessageUpdateChassisConfig.class, MessageDirection.PLAY_CLIENT_BOUND)
                .registerPlayMessage(S2CMessageUpdateEquipmentConfig.class, MessageDirection.PLAY_CLIENT_BOUND)
                .build();
    }
}
