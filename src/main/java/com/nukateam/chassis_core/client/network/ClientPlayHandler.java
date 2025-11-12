package com.nukateam.chassis_core.client.network;

import com.nukateam.chassis_core.common.network.managers.NetworkChassisManager;
import com.nukateam.chassis_core.common.network.managers.NetworkEquipmentManager;
import com.nukateam.chassis_core.common.network.packet.S2CMessageUpdateChassisConfig;
import com.nukateam.chassis_core.common.network.packet.S2CMessageUpdateEquipmentConfig;

public class ClientPlayHandler {
    public static void handleUpdateChassis(S2CMessageUpdateChassisConfig message) {
        NetworkChassisManager.updateRegisteredConfig(message.getRegisteredConfig());
    }

    public static void handleUpdateEquipment(S2CMessageUpdateEquipmentConfig message) {
        NetworkEquipmentManager.updateRegisteredConfig(message.getRegisteredConfig());
    }
}
