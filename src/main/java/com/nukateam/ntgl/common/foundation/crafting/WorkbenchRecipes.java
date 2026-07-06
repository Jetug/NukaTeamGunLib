package com.nukateam.ntgl.common.foundation.crafting;

import net.minecraft.core.NonNullList;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.level.Level;

import javax.annotation.Nullable;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Author: MrCrayfish
 */
public class WorkbenchRecipes {
    public static boolean isEmpty(Level level) {
        return level.getRecipeManager()
                .getAllRecipesFor(ModRecipeTypes.WORKBENCH.get())
                .isEmpty();
    }

    public static List<WorkbenchRecipe> getAll(Level level) {
        return (List<WorkbenchRecipe>)level.getRecipeManager()
                .getAllRecipesFor(ModRecipeTypes.WORKBENCH.get())
                .stream()
                .map(RecipeHolder::value)
                .toList();
    }

    @Nullable
    public static WorkbenchRecipe getRecipeById(Level level, ResourceLocation id) {
        return (WorkbenchRecipe)level.getRecipeManager()
                .getAllRecipesFor(ModRecipeTypes.WORKBENCH.get())
                .stream()
                .filter(holder -> holder.id().equals(id))
                .map(RecipeHolder::value)
                .findFirst()
                .orElse(null);
    }
}
