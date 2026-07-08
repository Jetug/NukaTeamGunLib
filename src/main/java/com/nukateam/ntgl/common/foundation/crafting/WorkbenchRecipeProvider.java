package com.nukateam.ntgl.common.foundation.crafting;

import com.nukateam.example.common.registery.ExampleWeapons;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataProvider;
import net.minecraft.data.PackOutput;
import net.neoforged.neoforge.common.Tags;

import java.util.ArrayList;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

public class WorkbenchRecipeProvider implements DataProvider {
    private final PackOutput.PathProvider pathProvider;

    public WorkbenchRecipeProvider(PackOutput output) {
        this.pathProvider = output.createPathProvider(PackOutput.Target.DATA_PACK, "recipes");
    }

    @Override
    public CompletableFuture<?> run(CachedOutput cache) {
        var futures = new ArrayList<CompletableFuture<?>>();

        addRecipes(recipe -> {
            var json = recipe.toJson();
            var path = pathProvider.json(recipe.id());
            futures.add(DataProvider.saveStable(cache, json, path));
        });

        return CompletableFuture.allOf(futures.toArray(CompletableFuture[]::new));
    }

    protected void addRecipes(Consumer<WorkbenchRecipeJson> consumer) {
        consumer.accept(WorkbenchRecipeJson.builder(ExampleWeapons.PISTOL10MM.get())
                .ingredient(Tags.Items.INGOTS_IRON, 14).build());
    }

    @Override
    public String getName() {
        return "Workbench Recipes";
    }
}