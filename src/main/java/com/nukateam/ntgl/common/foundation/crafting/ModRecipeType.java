package com.nukateam.ntgl.common.foundation.crafting;

import com.nukateam.ntgl.Ntgl;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeType;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.Registries;
import net.neoforged.neoforge.registries.DeferredHolder;

/**
 * Author: MrCrayfish
 */

public class ModRecipeType {
    public static final DeferredRegister<RecipeType<?>> REGISTER = DeferredRegister.create(ForgeRegistries.RECIPE_TYPES, Ntgl.MOD_ID);

    public static final DeferredHolder<RecipeType<WorkbenchRecipe>> WORKBENCH = create("workbench");

    private static <T extends Recipe<?>> DeferredHolder<RecipeType<T>> create(String name) {
        return REGISTER.register(name, () -> new RecipeType<>() {
            @Override
            public String toString() {
                return name;
            }
        });
    }
}
