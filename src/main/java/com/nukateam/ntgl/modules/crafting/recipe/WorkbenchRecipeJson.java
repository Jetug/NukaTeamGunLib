package com.nukateam.ntgl.modules.crafting.recipe;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.ItemLike;

public record WorkbenchRecipeJson(ResourceLocation id, JsonObject json) {
    public JsonObject toJson() {
        return json;
    }

    public static Builder builder(ItemLike result) {
        return new Builder(result);
    }

    public static class Builder {
        private final ItemLike result;
        private int count = 1;
        private final JsonArray materials = new JsonArray();

        Builder(ItemLike result) {
            this.result = result;
        }

        public Builder count(int count) {
            this.count = count;
            return this;
        }

        public Builder ingredient(ItemLike item, int amount) {
            var object = new JsonObject();
            object.addProperty("item", BuiltInRegistries.ITEM.getKey(item.asItem()).toString());
            object.addProperty("count", amount);

            materials.add(object);

            return this;
        }

        public Builder ingredient(TagKey<Item> tag, int amount) {
            var object = new JsonObject();
            object.addProperty("tag", tag.location().toString());
            object.addProperty("count", amount);
            materials.add(object);

            return this;
        }

        public WorkbenchRecipeJson build() {
            var root = new JsonObject();
            root.addProperty("type", "ntgl:workbench");
            root.add("materials", materials);
            var resultJson = new JsonObject();
            resultJson.addProperty("item", BuiltInRegistries.ITEM.getKey(result.asItem()).toString());

            if (count > 1) resultJson.addProperty("count", count);

            root.add("result", resultJson);
            return new WorkbenchRecipeJson(BuiltInRegistries.ITEM.getKey(result.asItem()), root);
        }
    }
}