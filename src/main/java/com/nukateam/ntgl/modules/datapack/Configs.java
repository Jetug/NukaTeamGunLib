package com.nukateam.ntgl.modules.datapack;

import com.nukateam.chassis_core.common.config.ChassisConfig;
import com.nukateam.chassis_core.common.config.EquipmentConfig;
import com.nukateam.chassis_core.common.foundation.entity.Chassis;
import com.nukateam.chassis_core.common.foundation.item.IChassisEquipment;
import net.minecraft.world.entity.EntityType;

import java.util.HashMap;
import java.util.Map;

public class Configs {
    public static final Map<EntityType<Chassis>, ConfigSupplier<ChassisConfig>> CHASSIS_CONFIGS = new HashMap<>();

    public static final Map<IChassisEquipment, ConfigSupplier<EquipmentConfig>> EQUIPMENT_CONFIGS = new HashMap<>();
}
