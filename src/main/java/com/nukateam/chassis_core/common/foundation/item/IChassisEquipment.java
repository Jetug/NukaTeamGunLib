package com.nukateam.chassis_core.common.foundation.item;

import com.nukateam.chassis_core.common.config.ChassisConfig;
import com.nukateam.chassis_core.common.config.EquipmentConfig;
import com.nukateam.ntgl.common.data.config.weapon.WeaponConfig;
import com.nukateam.ntgl.common.foundation.item.interfaces.IConfigConsumer;
import com.nukateam.ntgl.common.util.interfaces.IConfigProvider;
import com.nukateam.ntgl.modules.datapack.ConfigSupplier;

public interface IChassisEquipment extends IConfigConsumer<EquipmentConfig>, IConfigProvider<EquipmentConfig> {
}
