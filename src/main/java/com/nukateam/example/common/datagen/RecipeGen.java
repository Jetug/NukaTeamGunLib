package com.nukateam.example.common.datagen;

import com.nukateam.example.common.registery.ExampleWeapons;
import com.nukateam.ntgl.modules.crafting.recipe.WorkbenchIngredient;
import com.nukateam.ntgl.modules.crafting.recipe.WorkbenchRecipeBuilder;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.data.recipes.RecipeProvider;
import net.neoforged.neoforge.common.Tags;

import java.util.concurrent.CompletableFuture;

public class RecipeGen extends RecipeProvider {
    public RecipeGen(PackOutput output, CompletableFuture<HolderLookup.Provider>registries) {
        super(output, registries);
    }


    @Override
    protected void buildRecipes(RecipeOutput output) {
//        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModBlocks.WORKBENCH.get())
//                .pattern("CCC")
//                .pattern("III")
//                .pattern("I I")
//                .define('C', Blocks.LIGHT_GRAY_CONCRETE)
//                .define('I', Tags.Items.INGOTS_IRON)
//                .unlockedBy("has_concrete", has(Blocks.LIGHT_GRAY_CONCRETE))
//                .unlockedBy("has_iron", has(Tags.Items.INGOTS_IRON))
//                .save(consumer);
//
//        // Guns
        WorkbenchRecipeBuilder.crafting(ExampleWeapons.PISTOL10MM.get())
                .addIngredient(WorkbenchIngredient.of(Tags.Items.INGOTS_IRON, 14))
                .addCriterion("has_iron_ingot", has(Tags.Items.INGOTS_IRON))
                .build(output);

//        WorkbenchRecipeBuilder.crafting(RecipeCategory.COMBAT, ExampleWeapons.SHOTGUN.get())
//                .addIngredient(WorkbenchIngredient.of(Tags.Items.INGOTS_IRON, 24))
//                .addCriterion("has_iron_ingot", has(Tags.Items.INGOTS_IRON))
//                .build(consumer);
//        WorkbenchRecipeBuilder.crafting(RecipeCategory.COMBAT, ExampleWeapons.POWDERGUN.get())
//                .addIngredient(WorkbenchIngredient.of(Tags.Items.INGOTS_IRON, 24))
//                .addCriterion("has_iron_ingot", has(Tags.Items.INGOTS_IRON))
//                .build(consumer);
//        WorkbenchRecipeBuilder.crafting(RecipeCategory.COMBAT, ExampleWeapons.CLASSIC10MM.get())
//                .addIngredient(WorkbenchIngredient.of(Tags.Items.INGOTS_IRON, 32))
//                .addCriterion("has_iron_ingot", has(Tags.Items.INGOTS_IRON))
//                .build(consumer);
//        WorkbenchRecipeBuilder.crafting(RecipeCategory.COMBAT, ExampleWeapons.FATMAN.get())
//                .addIngredient(WorkbenchIngredient.of(Tags.Items.INGOTS_IRON, 44))
//                .addIngredient(Items.REDSTONE, 4)
//                .addIngredient(WorkbenchIngredient.of(Tags.Items.DYES_RED, 1))
//                .addCriterion("has_iron_ingot", has(Tags.Items.INGOTS_IRON))
//                .addCriterion("has_redstone", has(Items.REDSTONE))
//                .build(consumer);
//        WorkbenchRecipeBuilder.crafting(RecipeCategory.COMBAT, ExampleWeapons.MINIGUN.get())
//                .addIngredient(WorkbenchIngredient.of(Tags.Items.INGOTS_IRON, 38))
//                .addCriterion("has_iron_ingot", has(Tags.Items.INGOTS_IRON))
//                .build(consumer);
//        WorkbenchRecipeBuilder.crafting(RecipeCategory.COMBAT, ExampleWeapons.FLAMER.get())
//                .addIngredient(WorkbenchIngredient.of(Tags.Items.INGOTS_IRON, 28))
//                .addCriterion("has_iron_ingot", has(Tags.Items.INGOTS_IRON))
//                .build(consumer);
//        WorkbenchRecipeBuilder.crafting(RecipeCategory.COMBAT, ExampleWeapons.SCOUT10MM.get())
//                .addIngredient(WorkbenchIngredient.of(Tags.Items.INGOTS_IRON, 20))
//                .addCriterion("has_iron_ingot", has(Tags.Items.INGOTS_IRON))
//                .build(consumer);
//        WorkbenchRecipeBuilder.crafting(RecipeCategory.COMBAT, ExampleWeapons.PIPE_REVOLVER.get())
//                .addIngredient(WorkbenchIngredient.of(Tags.Items.INGOTS_IRON, 36))
//                .addCriterion("has_iron_ingot", has(Tags.Items.INGOTS_IRON))
//                .build(consumer);
//
//        // Projectile
//        WorkbenchRecipeBuilder.crafting(RecipeCategory.COMBAT, ExampleWeapons.ROUND10MM.get(), 64)
//                .addIngredient(WorkbenchIngredient.of(Items.COPPER_INGOT, 4))
//                .addIngredient(WorkbenchIngredient.of(Tags.Items.GUNPOWDER, 1))
//                .addCriterion("has_copper_ingot", has(Items.COPPER_INGOT))
//                .addCriterion("has_gunpowder", has(Tags.Items.GUNPOWDER))
//                .build(consumer);
//        WorkbenchRecipeBuilder.crafting(RecipeCategory.COMBAT, ExampleWeapons.ROUND5MM.get(), 32)
//                .addIngredient(WorkbenchIngredient.of(Items.COPPER_INGOT, 4))
//                .addIngredient(WorkbenchIngredient.of(Tags.Items.GUNPOWDER, 1))
//                .addCriterion("has_copper_ingot", has(Items.COPPER_INGOT))
//                .addCriterion("has_gunpowder", has(Tags.Items.GUNPOWDER))
//                .build(consumer);
//        WorkbenchRecipeBuilder.crafting(RecipeCategory.COMBAT, ExampleWeapons.ROUND44.get(), 48)
//                .addIngredient(WorkbenchIngredient.of(Items.COPPER_INGOT, 4))
//                .addIngredient(WorkbenchIngredient.of(Tags.Items.NUGGETS_GOLD, 1))
//                .addIngredient(WorkbenchIngredient.of(Tags.Items.GUNPOWDER, 1))
//                .addCriterion("has_copper_ingot", has(Items.COPPER_INGOT))
//                .addCriterion("has_gold_nugget", has(Tags.Items.NUGGETS_GOLD))
//                .addCriterion("has_gunpowder", has(Tags.Items.GUNPOWDER))
//                .build(consumer);
//        WorkbenchRecipeBuilder.crafting(RecipeCategory.COMBAT, ExampleWeapons.MININUKE.get())
//                .addIngredient(WorkbenchIngredient.of(Tags.Items.NUGGETS_IRON, 2))
//                .addIngredient(WorkbenchIngredient.of(Tags.Items.GUNPOWDER, 4))
//                .addCriterion("has_iron_ingot", has(Tags.Items.NUGGETS_IRON))
//                .addCriterion("has_gunpowder", has(Tags.Items.GUNPOWDER))
//                .build(consumer);
//        WorkbenchRecipeBuilder.crafting(RecipeCategory.COMBAT, ExampleWeapons.GRENADE.get(), 2)
//                .addIngredient(WorkbenchIngredient.of(Tags.Items.NUGGETS_IRON, 1))
//                .addIngredient(WorkbenchIngredient.of(Tags.Items.GUNPOWDER, 4))
//                .addCriterion("has_iron_ingot", has(Tags.Items.NUGGETS_IRON))
//                .addCriterion("has_gunpowder", has(Tags.Items.GUNPOWDER))
//                .build(consumer);
//        WorkbenchRecipeBuilder.crafting(RecipeCategory.COMBAT, ExampleWeapons.STUN_GRENADE.get(), 2)
//                .addIngredient(WorkbenchIngredient.of(Tags.Items.NUGGETS_IRON, 1))
//                .addIngredient(WorkbenchIngredient.of(Tags.Items.GUNPOWDER, 2))
//                .addIngredient(WorkbenchIngredient.of(Tags.Items.DUSTS_GLOWSTONE, 4))
//                .addCriterion("has_iron_ingot", has(Tags.Items.NUGGETS_IRON))
//                .addCriterion("has_gunpowder", has(Tags.Items.GUNPOWDER))
//                .addCriterion("has_glowstone", has(Tags.Items.DUSTS_GLOWSTONE))
//                .build(consumer);
//
//        // Scope Attachments
//        WorkbenchRecipeBuilder.crafting(RecipeCategory.COMBAT, ExampleWeapons.HOLOGRAPHIC_SIGHT.get())
//                .addIngredient(WorkbenchIngredient.of(Tags.Items.NUGGETS_IRON, 2))
//                .addIngredient(WorkbenchIngredient.of(Tags.Items.GEMS_AMETHYST, 1))
//                .addIngredient(WorkbenchIngredient.of(Tags.Items.DUSTS_REDSTONE, 2))
//                .addCriterion("has_iron_ingot", has(Tags.Items.NUGGETS_IRON))
//                .addCriterion("has_amethyst", has(Tags.Items.GEMS_AMETHYST))
//                .addCriterion("has_redstone", has(Tags.Items.DUSTS_REDSTONE))
//                .build(consumer);
//        WorkbenchRecipeBuilder.crafting(RecipeCategory.COMBAT, ExampleWeapons.COLLIMATOR_SIGHT.get())
//                .addIngredient(WorkbenchIngredient.of(Tags.Items.NUGGETS_IRON, 4))
//                .addIngredient(WorkbenchIngredient.of(Tags.Items.GEMS_AMETHYST, 1))
//                .addIngredient(WorkbenchIngredient.of(Tags.Items.DUSTS_REDSTONE, 4))
//                .addCriterion("has_iron_ingot", has(Tags.Items.NUGGETS_IRON))
//                .addCriterion("has_amethyst", has(Tags.Items.GEMS_AMETHYST))
//                .addCriterion("has_redstone", has(Tags.Items.DUSTS_REDSTONE))
//                .build(consumer);
//
//        // Barrel Attachments
//        WorkbenchRecipeBuilder.crafting(RecipeCategory.COMBAT, ExampleWeapons.SILENCER.get())
//                .addIngredient(WorkbenchIngredient.of(Tags.Items.NUGGETS_IRON, 4))
//                .addIngredient(WorkbenchIngredient.of(Items.SPONGE, 1))
//                .addCriterion("has_iron_ingot", has(Tags.Items.NUGGETS_IRON))
//                .build(consumer);
//
//        // Stock Attachments
//        WorkbenchRecipeBuilder.crafting(RecipeCategory.COMBAT, ExampleWeapons.LIGHT_STOCK.get())
//                .addIngredient(WorkbenchIngredient.of(Tags.Items.NUGGETS_IRON, 6))
//                .addIngredient(WorkbenchIngredient.of(Items.GRAY_WOOL, 1))
//                .addCriterion("has_iron_ingot", has(Tags.Items.NUGGETS_IRON))
//                .addCriterion("has_gray_wool", has(Items.GRAY_WOOL))
//                .build(consumer);
//        WorkbenchRecipeBuilder.crafting(RecipeCategory.COMBAT, ExampleWeapons.TACTICAL_STOCK.get())
//                .addIngredient(WorkbenchIngredient.of(Tags.Items.NUGGETS_IRON, 8))
//                .addIngredient(WorkbenchIngredient.of(Items.GRAY_WOOL, 1))
//                .addCriterion("has_iron_ingot", has(Tags.Items.NUGGETS_IRON))
//                .addCriterion("has_gray_wool", has(Items.GRAY_WOOL))
//                .build(consumer);
//        WorkbenchRecipeBuilder.crafting(RecipeCategory.COMBAT, ExampleWeapons.WEIGHTED_STOCK.get())
//                .addIngredient(WorkbenchIngredient.of(Tags.Items.NUGGETS_IRON, 12))
//                .addIngredient(WorkbenchIngredient.of(Items.GRAY_WOOL, 1))
//                .addCriterion("has_iron_ingot", has(Tags.Items.NUGGETS_IRON))
//                .addCriterion("has_gray_wool", has(Items.GRAY_WOOL))
//                .build(consumer);
//
//        // Under Barrel Attachments
//        WorkbenchRecipeBuilder.crafting(RecipeCategory.COMBAT, ExampleWeapons.LIGHT_GRIP.get())
//                .addIngredient(WorkbenchIngredient.of(Tags.Items.NUGGETS_IRON, 4))
//                .addIngredient(WorkbenchIngredient.of(Items.GRAY_WOOL, 1))
//                .addCriterion("has_iron_ingot", has(Tags.Items.NUGGETS_IRON))
//                .addCriterion("has_gray_wool", has(Items.GRAY_WOOL))
//                .build(consumer);
//        WorkbenchRecipeBuilder.crafting(RecipeCategory.COMBAT, ExampleWeapons.SPECIALISED_GRIP.get())
//                .addIngredient(WorkbenchIngredient.of(Tags.Items.NUGGETS_IRON, 8))
//                .addIngredient(WorkbenchIngredient.of(Items.GRAY_WOOL, 1))
//                .addCriterion("has_iron_ingot", has(Tags.Items.NUGGETS_IRON))
//                .addCriterion("has_gray_wool", has(Items.GRAY_WOOL))
//                .build(consumer);
    }
}