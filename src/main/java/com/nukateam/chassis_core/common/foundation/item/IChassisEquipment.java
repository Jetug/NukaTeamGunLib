package com.nukateam.chassis_core.common.foundation.item;

import com.nukateam.chassis_core.common.config.EquipmentConfig;
import com.nukateam.chassis_core.common.network.managers.ConfigSupplier;

public interface IChassisEquipment {
    EquipmentConfig getConfig();

    void setConfig(ConfigSupplier<EquipmentConfig> config);
}
