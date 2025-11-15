package com.nukateam.ntgl.common.foundation.crafting;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.nukateam.ntgl.common.foundation.init.ModRecipeSerializers;
import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.AdvancementRewards;
import net.minecraft.advancements.CriterionTriggerInstance;
import net.minecraft.advancements.critereon.RecipeUnlockedTrigger;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.ItemLike;
import net.neoforged.neoforge.common.crafting.CraftingHelper;
import net.minecraft.core.registries.Registries;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;
import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.AdvancementRequirements;
import net.minecraft.advancements.AdvancementRewards;
import net.minecraft.advancements.Criterion;
import net.minecraft.advancements.critereon.RecipeUnlockedTrigger;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.data.recipes.RecipeBuilder;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.ItemLike;
import net.neoforged.neoforge.common.conditions.ICondition;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class WorkbenchRecipeBuilder implements RecipeBuilder {
    @Nullable
    private final RecipeCategory category;
    private final Item result;
    private final int count;
    private final List<WorkbenchIngredient> ingredients;
    private final Map<String, Criterion<?>> criteria = new LinkedHashMap<>();
    private final List<ICondition> conditions = new ArrayList<>();

    private WorkbenchRecipeBuilder(@Nullable RecipeCategory category, ItemLike item, int count) {
        this.category = category;
        this.result = item.asItem();
        this.count = count;
        this.ingredients = new ArrayList<>();
    }

    public static WorkbenchRecipeBuilder crafting(ItemLike item) {
        return new WorkbenchRecipeBuilder(null, item, 1);
    }

    public static WorkbenchRecipeBuilder crafting(ItemLike item, int count) {
        return new WorkbenchRecipeBuilder(null, item, count);
    }

    public static WorkbenchRecipeBuilder crafting(@Nullable RecipeCategory category, ItemLike item) {
        return new WorkbenchRecipeBuilder(category, item, 1);
    }

    public static WorkbenchRecipeBuilder crafting(@Nullable RecipeCategory category, ItemLike item, int count) {
        return new WorkbenchRecipeBuilder(category, item, count);
    }

    public WorkbenchRecipeBuilder addIngredient(ItemLike item, int count) {
        this.ingredients.add(WorkbenchIngredient.of(item, count));
        return this;
    }

    public WorkbenchRecipeBuilder addIngredient(WorkbenchIngredient ingredient) {
        this.ingredients.add(ingredient);
        return this;
    }

    public WorkbenchRecipeBuilder addCondition(ICondition condition) {
        this.conditions.add(condition);
        return this;
    }

    @Override
    public WorkbenchRecipeBuilder unlockedBy(String name, Criterion<?> criterion) {
        this.criteria.put(name, criterion);
        return this;
    }

    @Override
    public WorkbenchRecipeBuilder group(@Nullable String group) {
        // Если нужна группа, можно добавить поле
        return this;
    }

    @Override
    public Item getResult() {
        return this.result;
    }

    @Override
    public void save(RecipeOutput output, ResourceLocation id) {
        this.validate(id);

        Advancement.Builder advancementBuilder = Advancement.Builder.advancement()
                .parent(ResourceLocation.parse("recipes/root"))
                .addCriterion("has_the_recipe", RecipeUnlockedTrigger.unlocked(id))
                .rewards(AdvancementRewards.Builder.recipe(id))
                .requirements(AdvancementRequirements.Strategy.OR);

        // Добавляем все критерии
        this.criteria.forEach(advancementBuilder::addCriterion);

        output.accept(
                new Result(
                        id,
                        this.result,
                        this.count,
                        this.ingredients,
                        this.conditions,
                        advancementBuilder
                ),
                advancementBuilder.build(id.withPrefix("recipes/"))
        );
    }

    /**
     * Makes sure that this recipe is valid and obtainable.
     */
    private void validate(ResourceLocation id) {
        if (this.criteria.isEmpty()) {
            throw new IllegalStateException("No way of obtaining recipe " + id);
        }
    }

    public static class Result implements RecipeOutput.Result {
        private final ResourceLocation id;
        private final Item item;
        private final int count;
        private final List<WorkbenchIngredient> ingredients;
        private final List<ICondition> conditions;

        public Result(ResourceLocation id, Item item, int count, List<WorkbenchIngredient> ingredients, List<ICondition> conditions, Advancement.Builder advancement) {
            this.id = id;
            this.item = item;
            this.count = count;
            this.ingredients = ingredients;
            this.conditions = conditions;
        }

        @Override
        public void serializeRecipeData(JsonObject json) {
            // Сериализация условий
            if (!this.conditions.isEmpty()) {
                JsonArray conditionsArray = new JsonArray();
                this.conditions.forEach(condition -> conditionsArray.add(ICondition.serialize(condition)));
                json.add("neoforge:conditions", conditionsArray);
            }

            // Сериализация ингредиентов
            JsonArray materials = new JsonArray();
            this.ingredients.forEach(ingredient -> materials.add(ingredient.toJson()));
            json.add("materials", materials);

            // Сериализация результата
            JsonObject resultObject = new JsonObject();
            resultObject.addProperty("item", BuiltInRegistries.ITEM.getKey(this.item).toString());
            if (this.count > 1) {
                resultObject.addProperty("count", this.count);
            }
            json.add("result", resultObject);
        }

        @Override
        public ResourceLocation id() {
            return this.id;
        }

        @Override
        public RecipeSerializer<?> type() {
            return ModRecipeSerializers.WORKBENCH.get();
        }

        @Override
        public JsonObject serializeAdvancement() {
            return null; // Обрабатывается автоматически системой данных
        }

        @Override
        public ResourceLocation advancementId() {
            return null; // Обрабатывается автоматически системой данных
        }
    }
}