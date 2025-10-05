package com.nukateam.chassis_core.modules.example.common.container;

import com.nukateam.chassis_core.common.data.holders.ChassisPart;
import com.nukateam.chassis_core.common.foundation.container.menu.ChassisMenu;
import com.nukateam.chassis_core.common.foundation.entity.Chassis;
import com.nukateam.chassis_core.common.util.Pos2I;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;

import static com.nukateam.chassis_core.modules.example.common.registery.ContainerRegistry.*;

public class ExampleChassisMenu extends ChassisMenu {
    public static final int SIZE = 7;
    private static final int INVENTORY_POS_Y = 84;

    public ExampleChassisMenu(int i, Inventory playerInventory, FriendlyByteBuf buf) {
        this(i, new SimpleContainer(SIZE), playerInventory, null);
    }

    public ExampleChassisMenu(int containerId, Container container, Inventory playerInventory, Chassis entity) {
        super(EXAMPLE_CHASSIS_MENU.get(), containerId, container, playerInventory, entity, INVENTORY_POS_Y);
        createSlot(ChassisPart.HELMET, new Pos2I(82, 11));
        createSlot(ChassisPart.BODY_ARMOR, new Pos2I(82, 32));
        createSlot(ChassisPart.RIGHT_ARM_ARMOR, new Pos2I(61, 26));
        createSlot(ChassisPart.LEFT_ARM_ARMOR, new Pos2I(103, 26));
        createSlot(ChassisPart.RIGHT_LEG_ARMOR, new Pos2I(69, 54));
        createSlot(ChassisPart.LEFT_LEG_ARMOR, new Pos2I(95, 54));
    }
}