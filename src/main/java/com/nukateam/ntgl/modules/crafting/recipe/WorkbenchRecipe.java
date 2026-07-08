package com.nukateam.ntgl.modules.crafting.recipe;

import com.nukateam.ntgl.modules.crafting.registry.ModRecipeSerializers;
import com.nukateam.ntgl.common.util.util.InventoryUtil;
import com.nukateam.ntgl.modules.crafting.registry.ModRecipeTypes;
import net.minecraft.core.HolderLookup;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;

import java.util.List;

public record WorkbenchRecipe(ItemStack result, List<WorkbenchIngredient> materials) implements Recipe<WorkbenchRecipeInput> {
    public WorkbenchRecipe(ItemStack result, List<WorkbenchIngredient> materials) {
        this.result = result;
        this.materials = List.copyOf(materials);
    }

    @Override
    public boolean matches(WorkbenchRecipeInput input, Level level) {
        var player = input.player();

        for (WorkbenchIngredient ingredient : materials) {
            if (!InventoryUtil.hasWorkstationIngredient(player, ingredient)) {
                return false;
            }
        }

        return true;
    }

    @Override
    public ItemStack assemble(WorkbenchRecipeInput input, HolderLookup.Provider access) {
        return result.copy();
    }

    @Override
    public ItemStack getResultItem(HolderLookup.Provider access) {
        return result.copy();
    }

    @Override
    public boolean canCraftInDimensions(int width, int height) {
        return true;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return ModRecipeSerializers.WORKBENCH.get();
    }

    @Override
    public RecipeType<?> getType() {
        return ModRecipeTypes.WORKBENCH.get();
    }

    public void consumeMaterials(Player player) {
        for (WorkbenchIngredient ingredient : materials) {
            InventoryUtil.removeWorkstationIngredient(player, ingredient);
        }
    }
}