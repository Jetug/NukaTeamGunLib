package com.nukateam.chassis_core.common.util.helpers;

import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.item.ItemStack;
import org.checkerframework.checker.units.qual.C;
import org.jetbrains.annotations.NotNull;

import static com.nukateam.chassis_core.common.data.constants.NBT.*;

public class InventoryHelper {
    public static ListTag serializeInventory(HolderLookup.Provider lookupProvider, @NotNull SimpleContainer inventory) {
        var nbtTags = new ListTag();

        for (int slotId = 0; slotId < inventory.getContainerSize(); ++slotId) {
            var itemStack = inventory.getItem(slotId);
            var compoundNBT = new CompoundTag();
            compoundNBT.putByte(SLOT_TAG, (byte) slotId);
            if(!itemStack.isEmpty()) {
                var stackTag = itemStack.save(lookupProvider, new CompoundTag());
                compoundNBT.put(SLOT_STACK, stackTag);
            }
            nbtTags.add(compoundNBT);
        }

        return nbtTags;
    }

    public static void deserializeInventory(HolderLookup.Provider lookupProvider, SimpleContainer inventory, ListTag nbtTags) {
        for (Tag nbt : nbtTags) {
            var compoundNBT = (CompoundTag) nbt;
            int slotId = compoundNBT.getByte(SLOT_TAG) & 255;

            if (slotId >= inventory.getContainerSize()) continue;

            if (compoundNBT.contains(SLOT_STACK)) {
                var stackTag = compoundNBT.getCompound(SLOT_STACK);
                var stack = ItemStack.parseOptional(lookupProvider, stackTag);
                inventory.setItem(slotId, stack);
            } else {
                inventory.setItem(slotId, ItemStack.EMPTY);
            }
        }
    }
}
