package com.nukateam.chassis_core.modules.example.common.registery;

import com.nukateam.chassis_core.ChassisCore;
import com.nukateam.chassis_core.common.data.holders.ChassisPart;
import com.nukateam.chassis_core.common.foundation.item.ChassisArmor;
import com.nukateam.chassis_core.common.foundation.item.ChassisItem;
import com.nukateam.chassis_core.modules.example.common.entities.ExampleChassis;
import net.minecraft.world.item.Item;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.minecraft.core.registries.Registries;
import net.neoforged.neoforge.registries.DeferredHolder;

import static com.nukateam.chassis_core.modules.example.common.registery.ChassisArmorMaterials.EXAMPLE;


public class ChassisArmorItems {
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(Registries.ITEM, ChassisCore.MOD_ID);

    public static final DeferredHolder<Item> FRAME_ITEM = ITEMS.register("frame_item", () ->
            new ChassisItem<>(new Item.Properties().fireResistant(), EntityTypes.EXAMPLE_CHASSIS, ExampleChassis::new)
    );

    public static final DeferredHolder<Item> EXAMPLE_HELMET = ITEMS.register("t45_helmet", () ->
            new ChassisArmor(new Item.Properties(), EXAMPLE, ChassisPart.HELMET));

    public static final DeferredHolder<Item> EXAMPLE_BODY = ITEMS.register("t45_body", () ->
            new ChassisArmor(new Item.Properties(), EXAMPLE, ChassisPart.BODY_ARMOR));

    public static final DeferredHolder<Item> EXAMPLE_RIGHT_ARM = ITEMS.register("t45_right_arm", () ->
            new ChassisArmor(new Item.Properties(), EXAMPLE, ChassisPart.RIGHT_ARM_ARMOR));

    public static final DeferredHolder<Item> EXAMPLE_LEFT_ARM = ITEMS.register("t45_left_arm", () ->
            new ChassisArmor(new Item.Properties(), EXAMPLE, ChassisPart.LEFT_ARM_ARMOR));

    public static final DeferredHolder<Item> EXAMPLE_RIGHT_LEG = ITEMS.register("t45_right_leg", () ->
            new ChassisArmor(new Item.Properties(), EXAMPLE, ChassisPart.RIGHT_LEG_ARMOR));

    public static final DeferredHolder<Item> EXAMPLE_LEFT_LEG = ITEMS.register("t45_left_leg", () ->
            new ChassisArmor(new Item.Properties(), EXAMPLE, ChassisPart.LEFT_LEG_ARMOR));


    public static void register(IEventBus eventBus) {
        ITEMS.register(eventBus);
    }
}
