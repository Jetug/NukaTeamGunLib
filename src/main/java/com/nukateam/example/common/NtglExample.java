package com.nukateam.example.common;

import com.nukateam.example.common.registery.ExampleBlocks;
import com.nukateam.ntgl.modules.crafting.recipe.WorkbenchRecipeProvider;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.data.event.GatherDataEvent;

public class NtglExample {
    public NtglExample(IEventBus eventBus){
        eventBus.addListener(this::onGatherData);
        ExampleBlocks.register(eventBus);
    }

    private void onGatherData(GatherDataEvent event) {
        var generator = event.getGenerator();
        var output = generator.getPackOutput();
//        var lookupProvider = event.getLookupProvider();
//        generator.addProvider(event.includeServer(), new RecipeGen(output, lookupProvider));

        generator.addProvider(
                event.includeServer(),
                new WorkbenchRecipeProvider(output)
        );
    }
}
