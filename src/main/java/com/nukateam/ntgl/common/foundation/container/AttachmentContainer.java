package com.nukateam.ntgl.common.foundation.container;

import com.nukateam.ntgl.common.data.config.gun.Gun;
import com.nukateam.ntgl.common.foundation.container.slot.AttachmentSlot;
import com.nukateam.ntgl.common.foundation.init.ModContainers;
import com.nukateam.ntgl.common.foundation.item.attachment.AttachmentItemBase;
import com.nukateam.ntgl.common.data.attachment.IAttachment;
import com.nukateam.ntgl.common.util.data.Pos2I;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;

import static com.nukateam.ntgl.client.render.screen.AttachmentScreen.ATTACHMENT_Y;
import static com.nukateam.ntgl.client.render.screen.AttachmentScreen.SLOT_SIZE;
import static com.nukateam.ntgl.common.util.util.GunModifierHelper.getGunAttachments;

/**
 * Author: MrCrayfish
 */
public class AttachmentContainer extends AbstractContainerMenu {
    public static final int SCREEN_OFFSET_Y = 132;
    public static final Pos2I INVENTORY_OFFSET = new Pos2I(18, SCREEN_OFFSET_Y);
    public static final Pos2I HOTBAR_OFFSET = new Pos2I(18,SCREEN_OFFSET_Y + 58);
    private ItemStack weapon;
    private Container playerInventory;
    private Container weaponInventory;
    private boolean loaded = false;

    public AttachmentContainer(int windowId, Inventory playerInventory, ItemStack stack) {
        this(windowId, playerInventory);
        var attachments = getGunAttachments(stack);
        var attachmentItems = new ArrayList<ItemStack>();

        for (var att : attachments.keySet()) {
            attachmentItems.add(Gun.getAttachmentItem(att, stack));
        }
        for (int i = 0; i < attachmentItems.size(); i++) {
            this.weaponInventory.setItem(i, attachmentItems.get(i));
        }
        this.loaded = true;
    }

    public AttachmentContainer(int windowId, Inventory playerInventory) {
        super(ModContainers.ATTACHMENTS.get(), windowId);
        this.weapon = playerInventory.getSelected();
        this.playerInventory = playerInventory;
        var attachments = getGunAttachments(weapon);

        weaponInventory = new SimpleContainer(attachments.size()){
            @Override
            public void setChanged() {
                super.setChanged();
                AttachmentContainer.this.slotsChanged(this);
            }
        };

        var id = 0;
        for (var att : attachments.keySet()) {
            this.addSlot(new AttachmentSlot(this, this.weaponInventory, this.weapon, att, playerInventory.player, id, 7 + id * SLOT_SIZE + 1, ATTACHMENT_Y + 1));
            id++;
        }

        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 9; j++) {
                this.addSlot(new Slot(playerInventory, j + i * 9 + 9, 8 + j * INVENTORY_OFFSET.x, INVENTORY_OFFSET.y + i * 18));
            }
        }

        for (int i = 0; i < 9; i++) {
            if (i == playerInventory.selected) {
                this.addSlot(new Slot(playerInventory, i, 8 + i * HOTBAR_OFFSET.x, HOTBAR_OFFSET.y) {
                    @Override
                    public boolean mayPickup(Player playerIn) {
                        return false;
                    }
                });
            } else {
                this.addSlot(new Slot(playerInventory, i, 8 + i * HOTBAR_OFFSET.x, HOTBAR_OFFSET.y));
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
        var attachmentsTag = new CompoundTag();

        for (int i = 0; i < this.getWeaponInventory().getContainerSize(); i++) {
            var itemStack = this.getSlot(i).getItem();
            if (itemStack.getItem() instanceof IAttachment attachment
                    && itemStack.getItem() instanceof AttachmentItemBase) {
                var tagKey = attachment.getType();
                attachmentsTag.put(tagKey.toString(), itemStack.save(new CompoundTag()));
            }
        }

        var tag = this.weapon.getOrCreateTag();
        tag.put("Attachments", attachmentsTag);
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
