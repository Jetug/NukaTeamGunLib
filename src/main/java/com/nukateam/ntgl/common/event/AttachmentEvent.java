package com.nukateam.ntgl.common.event;

import com.nukateam.ntgl.common.data.WeaponData;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.Event;
import net.neoforged.bus.api.ICancellableEvent;

public class AttachmentEvent extends Event  implements ICancellableEvent{
    private final WeaponData weaponData;
    private final AbstractContainerMenu containerMenu;

    public static class SlotUpdateEvent extends AttachmentEvent implements ICancellableEvent {
        private final ItemStack oldStack;
        private final ItemStack newStack;

        public SlotUpdateEvent(AbstractContainerMenu containerMenu, WeaponData weaponData, ItemStack oldStack, ItemStack newStack) {
            super(weaponData, containerMenu);
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
        public ContainerUpdateEvent(AbstractContainerMenu containerMenu, WeaponData weaponData) {
            super(weaponData, containerMenu);
        }
    }

    public AttachmentEvent(WeaponData weaponData, AbstractContainerMenu containerMenu) {
        this.weaponData = weaponData;
        this.containerMenu = containerMenu;
    }

    public WeaponData getGunData() {
        return weaponData;
    }
}
