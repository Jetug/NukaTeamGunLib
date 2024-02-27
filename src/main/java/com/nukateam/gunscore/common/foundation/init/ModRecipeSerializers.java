package com.nukateam.gunscore.common.foundation.init;

import com.nukateam.gunscore.common.foundation.crafting.DyeItemRecipe;
import com.nukateam.gunscore.common.foundation.crafting.WorkbenchRecipeSerializer;
import com.nukateam.gunscore.GunMod;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.SimpleRecipeSerializer;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

/**
 * Author: MrCrayfish
 */
public class ModRecipeSerializers {
    public static final DeferredRegister<RecipeSerializer<?>> REGISTER = DeferredRegister.create(ForgeRegistries.RECIPE_SERIALIZERS, GunMod.MOD_ID);

    public static final RegistryObject<SimpleRecipeSerializer<DyeItemRecipe>> DYE_ITEM = REGISTER.register("dye_item", () -> new SimpleRecipeSerializer<>(DyeItemRecipe::new));
    public static final RegistryObject<WorkbenchRecipeSerializer> WORKBENCH = REGISTER.register("workbench", WorkbenchRecipeSerializer::new);
}
