package com.nukateam.chassis_core.common.util.helpers;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

import static com.nukateam.chassis_core.common.data.constants.NBT.SLOT_TAG;

public class InventoryHelper {
    public static ListTag serializeInventory(@NotNull SimpleContainer inventory) {
        var nbtTags = new ListTag();

        for (int slotId = 0; slotId < inventory.getContainerSize(); ++slotId) {
            var itemStack = inventory.getItem(slotId);
            var compoundNBT = new CompoundTag();
            compoundNBT.putByte(SLOT_TAG, (byte) slotId);
            itemStack.save(compoundNBT);
            nbtTags.add(compoundNBT);
        }

        return nbtTags;
    }

    public static void deserializeInventory(SimpleContainer inventory, ListTag nbtTags) {
        for (Tag nbt : nbtTags) {
            var compoundNBT = (CompoundTag) nbt;
            int slotId = compoundNBT.getByte(SLOT_TAG) & 255;
            inventory.setItem(slotId, ItemStack.of(compoundNBT));
        }
    }
}
