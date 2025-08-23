package com.nukateam.ntgl.common.event;

import com.nukateam.ntgl.common.data.GunData;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.eventbus.api.Cancelable;
import net.minecraftforge.eventbus.api.Event;

@Cancelable
public class AttachmentEvent extends Event {
    private final GunData gunData;
    private final AbstractContainerMenu containerMenu;

    @Cancelable
    public static class SlotUpdateEvent extends AttachmentEvent{
        private final ItemStack oldStack;
        private final ItemStack newStack;

        public SlotUpdateEvent(AbstractContainerMenu containerMenu, GunData gunData, ItemStack oldStack, ItemStack newStack) {
            super(gunData, containerMenu);
            this.oldStack = oldStack;
            this.newStack = newStack;
        }

        public ItemStack getOldStack() {
            return oldStack;
        }

        public ItemStack getNewStack() {
            return newStack;
        }
    }

    public static class ContainerUpdateEvent extends AttachmentEvent{
        public ContainerUpdateEvent(AbstractContainerMenu containerMenu, GunData gunData) {
            super(gunData, containerMenu);
        }
    }

    public AttachmentEvent(GunData gunData, AbstractContainerMenu containerMenu) {
        this.gunData = gunData;
        this.containerMenu = containerMenu;
    }

    public GunData getGunData() {
        return gunData;
    }
}
