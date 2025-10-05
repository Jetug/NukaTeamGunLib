package com.nukateam.chassis_core.modules.example.common.container;

import com.nukateam.chassis_core.common.data.holders.ChassisPart;
import com.nukateam.chassis_core.common.foundation.container.menu.ChassisMenu;
import com.nukateam.chassis_core.common.foundation.entity.Chassis;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;

import static com.nukateam.chassis_core.common.data.constants.Gui.*;
import static com.nukateam.chassis_core.modules.example.common.registery.ContainerRegistry.EXAMPLE_STATION_MENU;

public class ExampleChassisStationMenu extends ChassisMenu {
    public static final int SIZE = 10;
    private static final int INVENTORY_POS_Y = 105;

    public ExampleChassisStationMenu(int i, Inventory playerInventory, FriendlyByteBuf buf) {
        this(i, new SimpleContainer(SIZE), playerInventory, null);
    }

    public ExampleChassisStationMenu(int containerId, Container container, Inventory playerInventory, Chassis entity) {
        super(EXAMPLE_STATION_MENU.get(), containerId, container, playerInventory, entity, INVENTORY_POS_Y);
        createSlot(ChassisPart.BODY_FRAME, FRAME_BODY_SLOT_POS);
        createSlot(ChassisPart.LEFT_ARM_FRAME, FRAME_LEFT_ARM_SLOT_POS);
        createSlot(ChassisPart.RIGHT_ARM_FRAME, FRAME_RIGHT_ARM_SLOT_POS);
        createSlot(ChassisPart.LEFT_LEG_FRAME, FRAME_LEFT_LEG_SLOT_POS);
        createSlot(ChassisPart.RIGHT_LEG_FRAME, FRAME_RIGHT_LEG_SLOT_POS);
        createSlot(ChassisPart.ENGINE, ENGINE_SLOT_POS2);
        createSlot(ChassisPart.BACK, BACK_SLOT_POS);
        createSlot(ChassisPart.COOLING, COOLING_SLOT_POS);
        createSlot(ChassisPart.LEFT_HAND, LEFT_HAND_SLOT_POS);
        createSlot(ChassisPart.RIGHT_HAND, RIGHT_HAND_SLOT_POS);
    }

    @Override
    protected Integer getId(ChassisPart chassisPart) {
        return chassis.getPartId(chassisPart);
    }
}
