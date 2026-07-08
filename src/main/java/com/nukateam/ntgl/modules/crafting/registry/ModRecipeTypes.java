package com.nukateam.ntgl.modules.crafting.registry;

import com.nukateam.ntgl.Ntgl;
import com.nukateam.ntgl.modules.crafting.recipe.WorkbenchRecipe;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeType;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

/**
 * Author: MrCrayfish
 */

public class ModRecipeTypes {
    public static final DeferredRegister<RecipeType<?>> REGISTER = DeferredRegister.create(Registries.RECIPE_TYPE, Ntgl.MOD_ID);

    public static final DeferredHolder<RecipeType<?>, RecipeType<WorkbenchRecipe>> WORKBENCH = create("workbench");

    private static <T extends Recipe<?>> DeferredHolder<RecipeType<?>, RecipeType<T>> create(String name) {
        return REGISTER.register(name, () -> new RecipeType<>() {
            @Override
            public String toString() {
                return name;
            }
        });
    }
}
