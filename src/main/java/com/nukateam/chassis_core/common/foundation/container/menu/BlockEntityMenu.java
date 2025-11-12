package com.nukateam.chassis_core.common.foundation.container.menu;

import com.nukateam.chassis_core.common.foundation.container.menu.MenuBase;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;

public class BlockEntityMenu extends MenuBase {
    protected final BlockEntity blockEntity;
    protected final Level level;
    private final Block block;

    public BlockEntityMenu(MenuType<?> pMenuType, int containerId, Container container, Inventory playerInventory,
                           BlockEntity entity, int size, int inventoryPosY, Block block) {
        super(pMenuType, containerId, container, playerInventory, inventoryPosY);
        this.blockEntity = entity;
        this.level = playerInventory.player.level();
        this.block = block;
    }

    @Override
    public boolean stillValid(Player player) {
        return stillValid(ContainerLevelAccess.create(level, blockEntity.getBlockPos()),
                player, block);
    }
}
