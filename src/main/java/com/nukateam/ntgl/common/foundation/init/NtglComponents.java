package com.nukateam.ntgl.common.foundation.init;

import com.mojang.serialization.Codec;
import com.nukateam.ntgl.Ntgl;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

import javax.annotation.Nullable;

public class NtglComponents {
    public static final DeferredRegister.DataComponents REGISTER =
            DeferredRegister.createDataComponents(Registries.DATA_COMPONENT_TYPE, Ntgl.MOD_ID);

    public static final DeferredHolder<DataComponentType<?>, DataComponentType<CompoundTag>> WEAPON_COMPONENT =
            REGISTER.registerComponentType(
                    "weapon_component",
                    builder -> builder
                            .persistent(CompoundTag.CODEC)
                            .networkSynchronized(ByteBufCodecs.COMPOUND_TAG)
            );

    public static final DeferredHolder<DataComponentType<?>, DataComponentType<CompoundTag>> CHASSIS_COMPONENT =
            REGISTER.registerComponentType(
                    "chassis_component",
                    builder -> builder
                            .persistent(CompoundTag.CODEC)
                            .networkSynchronized(ByteBufCodecs.COMPOUND_TAG)
            );

    public static CompoundTag getWeaponTag(ItemStack stack) {
        return stack.getOrDefault(WEAPON_COMPONENT.get(), new CompoundTag());
    }

    public static @Nullable CompoundTag setWeaponTag(ItemStack stack, CompoundTag tag) {
        return stack.set(WEAPON_COMPONENT.get(), tag);
    }

    public static CompoundTag getChassisTag(ItemStack stack) {
        return stack.getOrDefault(CHASSIS_COMPONENT.get(), new CompoundTag());
    }

    public static @Nullable CompoundTag setChassisTag(ItemStack stack, CompoundTag tag) {
        return stack.set(CHASSIS_COMPONENT.get(), tag);
    }


}