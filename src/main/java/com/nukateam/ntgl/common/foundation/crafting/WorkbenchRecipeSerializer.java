package com.nukateam.ntgl.common.foundation.crafting;

import com.google.common.collect.ImmutableList;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.ShapedRecipe;

import javax.annotation.Nullable;

/**
 * Author: MrCrayfish
 */
public class WorkbenchRecipeSerializer implements RecipeSerializer<WorkbenchRecipe> {

    public static final MapCodec<WorkbenchRecipe> CODEC =
            RecordCodecBuilder.mapCodec(instance -> instance.group(
                    ItemStack.CODEC.fieldOf("result").forGetter(WorkbenchRecipe::getItem),
                    WorkbenchIngredient.CODEC.listOf().fieldOf("materials").forGetter(WorkbenchRecipe::getMaterials)
            ).apply(instance, WorkbenchRecipe::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, WorkbenchRecipe> STREAM_CODEC =
            StreamCodec.composite(
                    ItemStack.STREAM_CODEC,
                    WorkbenchRecipe::getItem,
                    WorkbenchIngredient.STREAM_CODEC.apply(ByteBufCodecs.list()),
                    WorkbenchRecipe::getMaterials,
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