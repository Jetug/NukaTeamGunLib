package com.nukateam.chassis_core.common.network;

import com.nukateam.chassis_core.common.data.enums.ActionType;
import com.nukateam.ntgl.common.network.PacketHandler;
import com.nukateam.ntgl.common.network.message.chassis.actions.Action;
import com.nukateam.ntgl.common.network.message.chassis.C2SGenericPacket;
import com.nukateam.ntgl.common.network.message.chassis.C2SActionPacket;

@SuppressWarnings("rawtypes")
public class PacketSender {
    public static void doServerAction(ActionType action) {
        PacketHandler.getPlayChannel().sendToServer(new C2SActionPacket(action));
    }

    public static void doServerAction(Action action, int entityId) {
        PacketHandler.getPlayChannel().sendToServer(new C2SGenericPacket(entityId, action));
    }
}
