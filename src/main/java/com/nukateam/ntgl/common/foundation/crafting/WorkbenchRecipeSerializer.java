package com.nukateam.ntgl.common.foundation.crafting;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeSerializer;

/**
 * Author: MrCrayfish
 */
public class WorkbenchRecipeSerializer implements RecipeSerializer<WorkbenchRecipe> {

    public static final MapCodec<WorkbenchRecipe> CODEC =
            RecordCodecBuilder.mapCodec(instance -> instance.group(
                    ItemStack.CODEC.fieldOf("result").forGetter(WorkbenchRecipe::result),
                    WorkbenchIngredient.CODEC.listOf().fieldOf("materials").forGetter(WorkbenchRecipe::materials)
            ).apply(instance, WorkbenchRecipe::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, WorkbenchRecipe> STREAM_CODEC =
            StreamCodec.composite(
                    ItemStack.STREAM_CODEC,
                    WorkbenchRecipe::result,
                    WorkbenchIngredient.STREAM_CODEC.apply(ByteBufCodecs.list()),
                    WorkbenchRecipe::materials,
                    WorkbenchRecipe::new
            );

    @Override
    public MapCodec<WorkbenchRecipe> codec() {
        return CODEC;
    }

    @Override
    public StreamCodec<RegistryFriendlyByteBuf, WorkbenchRecipe> streamCodec() {
        return STREAM_CODEC;
    }
}