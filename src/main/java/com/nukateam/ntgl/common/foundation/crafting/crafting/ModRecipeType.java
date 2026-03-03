package com.nukateam.ntgl.common.foundation.crafting.crafting;

import com.nukateam.ntgl.Ntgl;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeType;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

/**
 * Author: MrCrayfish
 */

public class ModRecipeType {
    public static final DeferredRegister<RecipeType<?>> REGISTER =
            DeferredRegister.create(Registries.RECIPE_TYPE, Ntgl.MOD_ID);


    public static final DeferredHolder<RecipeType<?>, RecipeType<Recipe<?>>> WORKBENCH = register("workbench");

    private static DeferredHolder<RecipeType<?>, RecipeType<Recipe<?>>> register(String name) {
        return REGISTER.register(name, () -> new RecipeType<>() {
            @Override
            public String toString() {
                return name;
            }
        });
    }
}
