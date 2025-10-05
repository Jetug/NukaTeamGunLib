package com.nukateam.chassis_core.modules.example.common.registery;

import com.nukateam.chassis_core.common.foundation.ChassisArmorMaterial;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;

public class ChassisArmorMaterials {
    public static ChassisArmorMaterial EXAMPLE = new ChassisArmorMaterial(
            "t45", 145, new int[]{3, 8, 6, 6}, 3, 9,
            SoundEvents.ARMOR_EQUIP_IRON, 0.0F,
            () -> Ingredient.of(Items.IRON_INGOT));
}
