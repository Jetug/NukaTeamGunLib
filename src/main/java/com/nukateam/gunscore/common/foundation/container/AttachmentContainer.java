package com.nukateam.gunscore.common.foundation.container;

import com.nukateam.gunscore.common.base.gun.Gun;
import com.nukateam.gunscore.common.foundation.container.slot.AttachmentSlot;
import com.nukateam.gunscore.common.foundation.init.ModContainers;
import com.nukateam.gunscore.common.foundation.item.AttachmentItem;
import com.nukateam.gunscore.common.foundation.item.attachment.IAttachment;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

/**
 * Author: MrCrayfish
 */
public class AttachmentContainer extends AbstractContainerMenu {
    private ItemStack weapon;
    private Container playerInventory;
    private Container weaponInventory = new SimpleContainer(IAttachment.Type.values().length) {
        @Override
        public void setChanged() {
            super.setChanged();
            AttachmentContainer.this.slotsChanged(this);
        }
    };
    private boolean loaded = false;

    public AttachmentContainer(int windowId, Inventory playerInventory, ItemStack stack) {
        this(windowId, playerInventory);
        ItemStack[] attachments = new ItemStack[IAttachment.Type.values().length];
        for (int i = 0; i < attachments.length; i++) {
            attachments[i] = Gun.getAttachment(IAttachment.Type.values()[i], stack);
        }
        for (int i = 0; i < attachments.length; i++) {
            this.weaponInventory.setItem(i, attachments[i]);
        }
        this.loaded = true;
    }

    public AttachmentContainer(int windowId, Inventory playerInventory) {
        super(ModContainers.ATTACHMENTS.get(), windowId);
        this.weapon = playerInventory.getSelected();
        this.playerInventory = playerInventory;

        for (int i = 0; i < IAttachment.Type.values().length; i++) {
            this.addSlot(new AttachmentSlot(this, this.weaponInventory, this.weapon, IAttachment.Type.values()[i], playerInventory.player, i, 8, 17 + i * 18));
        }

        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 9; j++) {
                this.addSlot(new Slot(playerInventory, j + i * 9 + 9, 8 + j * 18, 102 + i * 18));
            }
        }

        for (int i = 0; i < 9; i++) {
            if (i == playerInventory.selected) {
                this.addSlot(new Slot(playerInventory, i, 8 + i * 18, 160) {
                    @Override
                    public boolean mayPickup(Player playerIn) {
                        return false;
                    }
                });
            } else {
                this.addSlot(new Slot(playerInventory, i, 8 + i * 18, 160));
            }
        }
    }

    public boolean isLoaded() {
        return this.loaded;
    }

    @Override
    public boolean stillValid(Player playerIn) {
        return true;
    }

    @Override
    public void slotsChanged(Container inventoryIn) {
        var attachments = new CompoundTag();

        for (int i = 0; i < this.getWeaponInventory().getContainerSize(); i++) {
            var itemStack = this.getSlot(i).getItem();
            if (itemStack.getItem() instanceof IAttachment attachment
                    && itemStack.getItem() instanceof AttachmentItem) {
                var tagKey = attachment.getType().getTagKey();
                attachments.put(tagKey, itemStack.save(new CompoundTag()));
//                //Jetug
//                StackUtils.setAttachment(itemStack, tagKey, attachmentItem.getName());
            }
        }

        var tag = this.weapon.getOrCreateTag();
        tag.put("Attachments", attachments);
        super.broadcastChanges();
    }

    @Override
    public ItemStack quickMoveStack(Player playerIn, int index) {
        ItemStack copyStack = ItemStack.EMPTY;
        Slot slot = this.slots.get(index);
        if (slot != null && slot.hasItem()) {
            ItemStack slotStack = slot.getItem();
            copyStack = slotStack.copy();
            if (index < this.weaponInventory.getContainerSize()) {
                if (!this.moveItemStackTo(slotStack, this.weaponInventory.getContainerSize(), this.slots.size(), true)) {
                    return ItemStack.EMPTY;
                }
            } else if (!this.moveItemStackTo(slotStack, 0, this.weaponInventory.getContainerSize(), false)) {
                return ItemStack.EMPTY;
            }

            if (slotStack.isEmpty()) {
                slot.set(ItemStack.EMPTY);
            } else {
                slot.setChanged();
            }
        }

        return copyStack;
    }

    public Container getPlayerInventory() {
        return this.playerInventory;
    }

    public Container getWeaponInventory() {
        return this.weaponInventory;
    }
}
