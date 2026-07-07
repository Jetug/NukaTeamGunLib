package com.nukateam.ntgl.common.foundation.event;

import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.Event;
import net.neoforged.bus.api.ICancellableEvent;

public class WorkbenchCraftEvent extends Event implements ICancellableEvent {
    private final Player player;
    private final ItemStack outputItem;
    private Component rejectionMessage;

    public WorkbenchCraftEvent(Player player, ItemStack outputItem) {
        this.player = player;
        this.outputItem = outputItem;
    }

    public Player getPlayer() {
        return player;
    }

    public ItemStack getOutputItem() {
        return outputItem;
    }

    public Component getRejectionMessage() {
        return rejectionMessage;
    }

    public void setRejectionMessage(Component rejectionMessage) {
        this.rejectionMessage = rejectionMessage;
    }
}
