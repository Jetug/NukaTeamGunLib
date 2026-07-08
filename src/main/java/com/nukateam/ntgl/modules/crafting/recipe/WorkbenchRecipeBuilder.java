package com.nukateam.ntgl.modules.crafting.recipe;

import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.AdvancementRewards;
import net.minecraft.advancements.Criterion;
import net.minecraft.advancements.critereon.RecipeUnlockedTrigger;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;

import java.util.ArrayList;
import java.util.List;

public class WorkbenchRecipeBuilder {
    private final Item result;
    private final int count;
    private boolean hasAdvancementCriteria = false;

    private final List<WorkbenchIngredient> ingredients = new ArrayList<>();

    private final Advancement.Builder advancement = Advancement.Builder.advancement();

    private WorkbenchRecipeBuilder(ItemLike item, int count) {
        this.result = item.asItem();
        this.count = count;
    }

    public static WorkbenchRecipeBuilder crafting(ItemLike item) {
        return new WorkbenchRecipeBuilder(item, 1);
    }

    public static WorkbenchRecipeBuilder crafting(ItemLike item, int count) {
        return new WorkbenchRecipeBuilder(item, count);
    }

    // ---------------- ingredients ----------------

    public WorkbenchRecipeBuilder addIngredient(ItemLike item, int count) {
        this.ingredients.add(WorkbenchIngredient.of(item, count));
        return this;
    }

    public WorkbenchRecipeBuilder addIngredient(WorkbenchIngredient ingredient) {
        this.ingredients.add(ingredient);
        return this;
    }

    public WorkbenchRecipeBuilder addIngredient(TagKey<Item> tag, int count) {
        this.ingredients.add(WorkbenchIngredient.of(tag, count));
        return this;
    }

    // ---------------- advancement ----------------

    public WorkbenchRecipeBuilder addCriterion(String name, Criterion<?> trigger) {
        this.advancement.addCriterion(name, trigger);
        this.hasAdvancementCriteria = true;
        return this;
    }

    // ---------------- build ----------------

    public void build(RecipeOutput output) {
        var id = BuiltInRegistries.ITEM.getKey(this.result);

        validate(id);

        this.advancement
                .parent(ResourceLocation.parse("recipes/root"))
                .addCriterion("has_the_recipe",
                        RecipeUnlockedTrigger.unlocked(id))
                .rewards(AdvancementRewards.Builder.recipe(id));

        WorkbenchRecipe recipe = new WorkbenchRecipe(
                new ItemStack(result, count),
                List.copyOf(ingredients)
        );

        output.accept(id, recipe, advancement.build(id.withPrefix("recipes/")));
    }

    // ---------------- validation ----------------

    private void validate(ResourceLocation id) {
        if (this.ingredients.isEmpty()) {
            throw new IllegalStateException("Recipe " + id + " has no ingredients");
        }

        if (!this.hasAdvancementCriteria) {
            throw new IllegalStateException("No way of obtaining recipe " + id);
        }
    }
}