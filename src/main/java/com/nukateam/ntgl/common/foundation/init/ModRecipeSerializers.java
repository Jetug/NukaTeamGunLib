package com.nukateam.ntgl.common.foundation.init;

import com.nukateam.ntgl.Ntgl;
import com.nukateam.ntgl.common.foundation.crafting.WorkbenchRecipeSerializer;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

/**
 * Author: MrCrayfish
 */
public class ModRecipeSerializers {
    public static final DeferredRegister<RecipeSerializer<?>> REGISTER = DeferredRegister.create(Registries.RECIPE_SERIALIZER, Ntgl.MOD_ID);

    public static final DeferredHolder<RecipeSerializer<?>, WorkbenchRecipeSerializer> WORKBENCH = REGISTER.register("workbench", WorkbenchRecipeSerializer::new);
}
