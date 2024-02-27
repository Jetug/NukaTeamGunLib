package com.nukateam.gunscore.common.foundation.crafting;

import net.minecraft.world.item.crafting.RecipeType;

/**
 * Author: MrCrayfish
 */
public class ModRecipeType {
    public static final RecipeType<WorkbenchRecipe> WORKBENCH = RecipeType.register("gunscore:workbench");

    // Does nothing but trigger loading of static fields
    public static void init() {}
}
