package com.nukateam.chassis_core.common.network;

import com.nukateam.chassis_core.common.data.enums.ActionType;
import com.nukateam.chassis_core.common.network.actions.Action;
import com.nukateam.chassis_core.common.network.packet.C2SGenericPacket;
import com.nukateam.chassis_core.common.network.packet.C2SActionPacket;
import com.nukateam.ntgl.common.network.PacketHandler;

@SuppressWarnings("rawtypes")
public class PacketSender {
    public static void doServerAction(ActionType action) {
        PacketHandler.getPlayChannel().sendToServer(new C2SActionPacket(action));
    }

    public static void doServerAction(Action action, int entityId) {
        PacketHandler.getPlayChannel().sendToServer(new C2SGenericPacket(entityId, action));
    }
}
