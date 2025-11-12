package com.nukateam.chassis_core.common.foundation.container.menu;

import com.nukateam.chassis_core.common.data.holders.ChassisPart;
import com.nukateam.chassis_core.common.foundation.container.menu.ChassisMenu;
import com.nukateam.chassis_core.common.foundation.entity.Chassis;
import com.nukateam.chassis_core.common.util.Pos2I;
import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Inventory;
import org.jetbrains.annotations.Nullable;

import static com.nukateam.chassis_core.common.foundation.registery.ContainerRegistry.CHASSIS_MENU;

public class DynamicChassisMenu extends ChassisMenu {
    public static final int SIZE = 6;
    private static final int INVENTORY_POS_Y = 84;

    public DynamicChassisMenu(int i, Inventory playerInventory, FriendlyByteBuf buf) {
        super(CHASSIS_MENU.get(), i, playerInventory, buf, INVENTORY_POS_Y);

        createSlot(ChassisPart.HELMET, new Pos2I(82, 11));
        createSlot(ChassisPart.BODY_ARMOR, new Pos2I(82, 32));
        createSlot(ChassisPart.RIGHT_ARM_ARMOR, new Pos2I(61, 26));
        createSlot(ChassisPart.LEFT_ARM_ARMOR, new Pos2I(103, 26));
        createSlot(ChassisPart.RIGHT_LEG_ARMOR, new Pos2I(69, 54));
        createSlot(ChassisPart.LEFT_LEG_ARMOR, new Pos2I(95, 54));
    }

    public DynamicChassisMenu(int containerId, Container container, Inventory playerInventory, Chassis entity) {
        super(CHASSIS_MENU.get(), containerId, container, playerInventory, entity, INVENTORY_POS_Y);
        createSlot(ChassisPart.HELMET, new Pos2I(82, 11));
        createSlot(ChassisPart.BODY_ARMOR, new Pos2I(82, 32));
        createSlot(ChassisPart.RIGHT_ARM_ARMOR, new Pos2I(61, 26));
        createSlot(ChassisPart.LEFT_ARM_ARMOR, new Pos2I(103, 26));
        createSlot(ChassisPart.RIGHT_LEG_ARMOR, new Pos2I(69, 54));
        createSlot(ChassisPart.LEFT_LEG_ARMOR, new Pos2I(95, 54));
    }

    private static @Nullable Chassis getChassis(FriendlyByteBuf buf) {
        return (Chassis) Minecraft.getInstance().level.getEntity(buf.readInt());
    }
}