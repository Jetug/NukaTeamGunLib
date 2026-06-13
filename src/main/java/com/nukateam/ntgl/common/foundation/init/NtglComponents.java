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

    public static final DeferredHolder<DataComponentType<?>, DataComponentType<CompoundTag>> CHASSIS_COMPONENT =
            REGISTER.registerComponentType(
                    "chassis_component",
                    builder -> builder
                            .persistent(CompoundTag.CODEC)
                            .networkSynchronized(ByteBufCodecs.COMPOUND_TAG)
            );

    public static final DeferredHolder<DataComponentType<?>, DataComponentType<CompoundTag>> WEAPON_COMPONENT =
            REGISTER.registerComponentType(
                    "weapon_component",
                    builder -> builder
                            .persistent(CompoundTag.CODEC)
                            .networkSynchronized(ByteBufCodecs.COMPOUND_TAG)
            );

    public static final DeferredHolder<DataComponentType<?>, DataComponentType<CompoundTag>> FUEL =
            REGISTER.registerComponentType(
                    "fuel",
                    builder -> builder
                            .persistent(CompoundTag.CODEC)
                            .networkSynchronized(ByteBufCodecs.COMPOUND_TAG)
            );

    public static final DeferredHolder<DataComponentType<?>, DataComponentType<CompoundTag>> ATTACHMENTS =
            REGISTER.registerComponentType(
                    "attachments",
                    builder -> builder
                            .persistent(CompoundTag.CODEC)
                            .networkSynchronized(ByteBufCodecs.COMPOUND_TAG)
            );

    public static final DeferredHolder<DataComponentType<?>, DataComponentType<Integer>> AMMO_COUNT =
            REGISTER.registerComponentType(
                    "ammo_count",
                    builder -> builder
                            .persistent(Codec.INT)
                            .networkSynchronized(ByteBufCodecs.INT)
            );

    public static final DeferredHolder<DataComponentType<?>, DataComponentType<Boolean>> IGNORE_AMMO =
            REGISTER.registerComponentType(
                    "ignore_ammo",
                    builder -> builder
                            .persistent(Codec.BOOL)
                            .networkSynchronized(ByteBufCodecs.BOOL)
            );

    public static final DeferredHolder<DataComponentType<?>, DataComponentType<String>> AMMO =
            REGISTER.registerComponentType(
                    "ammo",
                    builder -> builder
                            .persistent(Codec.STRING)
                            .networkSynchronized(ByteBufCodecs.STRING_UTF8)
            );

    public static final DeferredHolder<DataComponentType<?>, DataComponentType<String>> FIRE_MODE =
            REGISTER.registerComponentType(
                    "fire_mode",
                    builder -> builder
                            .persistent(Codec.STRING)
                            .networkSynchronized(ByteBufCodecs.STRING_UTF8)
            );

    public static final DeferredHolder<DataComponentType<?>, DataComponentType<String>> THROW_MODE =
            REGISTER.registerComponentType(
                    "throw_mode",
                    builder -> builder
                            .persistent(Codec.STRING)
                            .networkSynchronized(ByteBufCodecs.STRING_UTF8)
            );

    private static CompoundTag copyOrEmpty(@Nullable CompoundTag tag) {
        return tag == null ? new CompoundTag() : tag.copy();
    }

    public static CompoundTag getWeaponTag(ItemStack stack) {
        return copyOrEmpty(stack.get(WEAPON_COMPONENT.get()));
    }

    public static @Nullable CompoundTag setWeaponTag(ItemStack stack, CompoundTag tag) {
        return stack.set(WEAPON_COMPONENT.get(), copyOrEmpty(tag));
    }

    public static CompoundTag getChassisTag(ItemStack stack) {
        return copyOrEmpty(stack.get(CHASSIS_COMPONENT.get()));
    }

    public static @Nullable CompoundTag setChassisTag(ItemStack stack, CompoundTag tag) {
        return stack.set(CHASSIS_COMPONENT.get(), copyOrEmpty(tag));
    }

    public static CompoundTag getFuelTag(ItemStack stack) {
        return copyOrEmpty(stack.get(FUEL.get()));
    }

    public static @Nullable CompoundTag setFuelTag(ItemStack stack, CompoundTag tag) {
        return stack.set(FUEL.get(), copyOrEmpty(tag));
    }

    public static CompoundTag getAttachmentsTag(ItemStack stack) {
        return copyOrEmpty(stack.get(ATTACHMENTS.get()));
    }

    public static @Nullable CompoundTag setAttachmentsTag(ItemStack stack, CompoundTag tag) {
        return stack.set(ATTACHMENTS.get(), copyOrEmpty(tag));
    }
}
