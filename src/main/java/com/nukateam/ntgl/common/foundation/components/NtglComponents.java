package com.nukateam.ntgl.common.foundation.components;

import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;

import javax.annotation.Nullable;

public class NtglComponents {
    public static final DataComponentType<CompoundTag> WEAPON_COMPONENT =
            DataComponentType.<CompoundTag>builder()
                    .persistent(CompoundTag.CODEC)
                    .networkSynchronized(ByteBufCodecs.COMPOUND_TAG)
                    .build();

    public static @Nullable CompoundTag getWeaponTag(ItemStack stack) {
        return stack.get(WEAPON_COMPONENT);
    }

    public static @Nullable CompoundTag setWeaponTag(ItemStack stack, CompoundTag tag) {
        return stack.set(WEAPON_COMPONENT, tag);
    }
}