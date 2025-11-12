package com.nukateam.chassis_core.common.foundation.item;

import com.nukateam.chassis_core.common.data.holders.ChassisPart;
import com.nukateam.chassis_core.common.foundation.ChassisArmorMaterial;
import com.nukateam.chassis_core.common.foundation.item.ChassisEquipment;

public class ChassisArmor extends ChassisEquipment {
    public final ChassisArmorMaterial material;

    public ChassisArmor(Properties pProperties, ChassisArmorMaterial material, ChassisPart part) {
        super(pProperties.durability(material.getDurabilityForSlot(part)), part);
        this.material = material;
    }

    public ChassisArmorMaterial getMaterial() {
        return material;
    }
}
