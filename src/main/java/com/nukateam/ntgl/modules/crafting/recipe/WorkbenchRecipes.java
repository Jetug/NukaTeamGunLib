package com.nukateam.ntgl.modules.crafting.recipe;

import com.nukateam.ntgl.modules.crafting.registry.ModRecipeTypes;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.level.Level;

import javax.annotation.Nullable;
import java.util.List;

/**
 * Author: Jetug
 */
public class WorkbenchRecipes {
    public static boolean isEmpty(Level level) {
        return level.getRecipeManager()
                .getAllRecipesFor(ModRecipeTypes.WORKBENCH.get())
                .isEmpty();
    }

    public static List<RecipeHolder<WorkbenchRecipe>> getAllHolders(Level level) {
        return level.getRecipeManager()
                .getAllRecipesFor(ModRecipeTypes.WORKBENCH.get());
    }

    public static List<WorkbenchRecipe> getAll(Level level) {
        return level.getRecipeManager()
                .getAllRecipesFor(ModRecipeTypes.WORKBENCH.get())
                .stream()
                .map(RecipeHolder::value)
                .toList();
    }

    @Nullable
    public static RecipeHolder<WorkbenchRecipe> getRecipeById(Level level, ResourceLocation id) {
        return level.getRecipeManager()
                .getAllRecipesFor(ModRecipeTypes.WORKBENCH.get())
                .stream()
                .filter(holder -> holder.id().equals(id))
                .findFirst()
                .orElse(null);
    }
}
