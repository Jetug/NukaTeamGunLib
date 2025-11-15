package com.nukateam.chassis_core.common.foundation.container.menu;

import com.nukateam.chassis_core.ChassisCore;
import com.nukateam.chassis_core.common.data.holders.ChassisPart;
import com.nukateam.chassis_core.common.foundation.container.slot.EquipmentSlot;
import com.nukateam.chassis_core.common.foundation.entity.Chassis;
import com.nukateam.chassis_core.common.util.Pos2I;
import com.nukateam.ntgl.Ntgl;
import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

import javax.annotation.Nullable;

import static com.nukateam.chassis_core.common.data.constants.Gui.*;
import static java.lang.System.out;

public abstract class ChassisMenu extends AbstractContainerMenu {
    private static final int SLOT_SIZE = 18;
    private static final int INVENTORY_POS_X = 8;
    private static final int HOTBAR_POS_X = INVENTORY_POS_X;
    private static final int INVENTORY_ROW_SIZE = 9;

    protected final int inventoryPosY;
    protected final int hotbarPosY;
    protected final Container container;
    protected final Chassis chassis;
    protected int size;

    public ChassisMenu(MenuType<?> pMenuType, int containerId, Inventory playerInventory, FriendlyByteBuf buf, int inventoryPosY) {
        this(pMenuType, containerId,
                new SimpleContainer(256),
                playerInventory,
                (Chassis) Minecraft.getInstance().level.getEntity(buf.readInt()),
                inventoryPosY);

//        super(pMenuType, containerId);
//        this.chassis = (Chassis) Minecraft.getInstance().level.getEntity(buf.readInt());
//        this.container = new SimpleContainer(256);
//        this.container.startOpen(playerInventory.player);
//        this.inventoryPosY = inventoryPosY;
//        this.hotbarPosY = inventoryPosY + 58;
//        this.size = container.getContainerSize();
//        addPlayerInventory(playerInventory);
//        addPlayerHotbar(playerInventory);
    }

    public ChassisMenu(MenuType<?> pMenuType, int containerId, Container container, Inventory playerInventory,
                       Chassis entity, int inventoryPosY) {
        super(pMenuType, containerId);
        this.chassis = entity;
        this.container = container;
        this.container.startOpen(playerInventory.player);
        this.inventoryPosY = inventoryPosY;
        this.hotbarPosY = inventoryPosY + 58;
        this.size = container.getContainerSize();
        addPlayerInventory(playerInventory);
        addPlayerHotbar(playerInventory);
    }

    @Override
    public boolean stillValid(Player playerIn) {
        return this.container.stillValid(playerIn) && this.chassis.isAlive() && this.chassis.distanceTo(playerIn) < 8.0F;
    }

    @Override
    public ItemStack quickMoveStack(Player playerIn, int index) {
        try {
            var sourceSlot = slots.get(index);
            if (!sourceSlot.hasItem()) return ItemStack.EMPTY;
            var sourceStack = sourceSlot.getItem();
            var copyOfSourceStack = sourceStack.copy();

            if (index < VANILLA_FIRST_SLOT_INDEX + VANILLA_SLOT_COUNT) {
                if (!moveItemStackTo(sourceStack, INVENTORY_FIRST_SLOT_INDEX,
                        INVENTORY_FIRST_SLOT_INDEX + size, false))
                    return ItemStack.EMPTY;
            } else if (index < INVENTORY_FIRST_SLOT_INDEX + size) {
                if (!moveItemStackTo(sourceStack, VANILLA_FIRST_SLOT_INDEX, VANILLA_FIRST_SLOT_INDEX + VANILLA_SLOT_COUNT, false))
                    return ItemStack.EMPTY;
            } else {
                out.println("Invalid slotIndex:" + index);
                return ItemStack.EMPTY;
            }

            if (sourceStack.getCount() == 0) sourceSlot.set(ItemStack.EMPTY);
            else sourceSlot.setChanged();
            sourceSlot.onTake(playerIn, sourceStack);
            return copyOfSourceStack;
        } catch (Exception ignored) {
            return ItemStack.EMPTY;
        }
    }

    @Override
    public void removed(Player playerIn) {
        super.removed(playerIn);
        container.stopOpen(playerIn);
    }

    public Chassis getChassis() {
        return chassis;
    }

    @Nullable
    protected Integer getId(ChassisPart chassisPart) {
        return chassis.getPartId(chassisPart);
    }

    protected void createSlot(ChassisPart chassisPart, Pos2I pos) {
        try {
            var id = getId(chassisPart);
            if(id != null)
                this.addSlot(new EquipmentSlot(chassisPart, container, getId(chassisPart), pos.x, pos.y));
        } catch (Exception e) {
            Ntgl.LOGGER.error(e.getMessage(), e);
        }
    }

    private void addPlayerInventory(Inventory playerInventory) {
        for (int row = 0; row < 3; ++row) {
            for (int slot = 0; slot < INVENTORY_ROW_SIZE; ++slot) {
                this.addSlot(new Slot(playerInventory,
                        slot + row * INVENTORY_ROW_SIZE + INVENTORY_ROW_SIZE,
                        INVENTORY_POS_X + slot * SLOT_SIZE,
                        inventoryPosY + row * SLOT_SIZE));
            }
        }
    }

    private void addPlayerHotbar(Inventory playerInventory) {
        for (int slot = 0; slot < 9; ++slot) {
            this.addSlot(new Slot(playerInventory, slot, HOTBAR_POS_X + slot * SLOT_SIZE, hotbarPosY));
        }
    }

    private static @Nullable Chassis getChassis(FriendlyByteBuf buf) {
        return (Chassis) Minecraft.getInstance().level.getEntity(buf.readInt());
    }
}