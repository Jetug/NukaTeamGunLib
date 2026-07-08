package com.nukateam.ntgl.modules.crafting.recipe;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.ItemLike;

/**
 * Author: MrCrayfish
 */
public record WorkbenchIngredient(
        Ingredient ingredient,
        int count
) {

    public static final Codec<WorkbenchIngredient> CODEC =
            RecordCodecBuilder.create(instance -> instance.group(
                    Ingredient.CODEC.fieldOf("ingredient").forGetter(WorkbenchIngredient::ingredient),
                    Codec.INT.optionalFieldOf("count", 1).forGetter(WorkbenchIngredient::count)
            ).apply(instance, WorkbenchIngredient::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, WorkbenchIngredient> STREAM_CODEC =
            StreamCodec.composite(
                    Ingredient.CONTENTS_STREAM_CODEC,
                    WorkbenchIngredient::ingredient,
                    ByteBufCodecs.VAR_INT,
                    WorkbenchIngredient::count,
                    WorkbenchIngredient::new
            );

    public static WorkbenchIngredient of(ItemLike item, int count) {
        return new WorkbenchIngredient(Ingredient.of(item), count);
    }

    public static WorkbenchIngredient of(ItemStack stack, int count) {
        return new WorkbenchIngredient(Ingredient.of(stack), count);
    }

    public static WorkbenchIngredient of(TagKey<Item> tag, int count) {
        return new WorkbenchIngredient(Ingredient.of(tag), count);
    }
}