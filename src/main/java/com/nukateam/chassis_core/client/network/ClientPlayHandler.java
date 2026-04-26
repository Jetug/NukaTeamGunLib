package com.nukateam.chassis_core.client.network;

import com.nukateam.ntgl.modules.datapack.managers.NetworkChassisManager;
import com.nukateam.ntgl.modules.datapack.managers.NetworkEquipmentManager;
import com.nukateam.ntgl.common.network.message.chassis.S2CMessageUpdateChassisConfig;
import com.nukateam.ntgl.common.network.message.chassis.S2CMessageUpdateEquipmentConfig;

public class ClientPlayHandler {
    public static void handleUpdateChassis(S2CMessageUpdateChassisConfig message) {
        NetworkChassisManager.updateRegisteredConfig(message.getRegisteredConfig());
    }

    public static void handleUpdateEquipment(S2CMessageUpdateEquipmentConfig message) {
        NetworkEquipmentManager.updateRegisteredConfig(message.getRegisteredConfig());
    }
}
