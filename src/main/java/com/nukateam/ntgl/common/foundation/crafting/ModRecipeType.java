package com.nukateam.ntgl.common.foundation.crafting;

import com.nukateam.ntgl.Ntgl;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

/**
 * Author: MrCrayfish
 */

public class ModRecipeType {
    public static final DeferredRegister<RecipeType<?>> REGISTER = DeferredRegister.create(ForgeRegistries.RECIPE_TYPES, Ntgl.MOD_ID);

    public static final RegistryObject<RecipeType<WorkbenchRecipe>> WORKBENCH = create("workbench");

    private static <T extends Recipe<?>> RegistryObject<RecipeType<T>> create(String name) {
        return REGISTER.register(name, () -> new RecipeType<>() {
            @Override
            public String toString() {
                return name;
            }
        });
    }
}
