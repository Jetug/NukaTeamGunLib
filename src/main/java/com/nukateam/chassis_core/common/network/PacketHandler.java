package com.nukateam.chassis_core.common.network;

import com.mrcrayfish.framework.api.network.MessageContext;
import com.mrcrayfish.framework.api.network.message.PlayMessage;
import com.mrcrayfish.framework.network.message.IMessage;
import com.mrcrayfish.framework.platform.network.ForgeMessageContext;
import com.mrcrayfish.framework.platform.network.ForgeNetworkBuilder;
import com.nukateam.chassis_core.ChassisCore;
import com.nukateam.chassis_core.common.network.managers.*;
import com.nukateam.chassis_core.common.network.packet.*;
import com.mrcrayfish.framework.api.FrameworkAPI;
import com.mrcrayfish.framework.api.network.FrameworkNetwork;
import com.mrcrayfish.framework.api.network.MessageDirection;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.network.NetworkDirection;

import javax.annotation.Nullable;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Optional;

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
