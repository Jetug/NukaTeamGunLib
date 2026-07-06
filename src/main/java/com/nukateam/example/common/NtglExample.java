package com.nukateam.example.common;

import com.nukateam.example.common.datagen.RecipeGen;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.data.event.GatherDataEvent;

public class NtglExample {
    public NtglExample(IEventBus MOD_EVENT_BUS){
        MOD_EVENT_BUS.addListener(this::onGatherData);
    }

    private void onGatherData(GatherDataEvent event) {
        var generator = event.getGenerator();
        var output = generator.getPackOutput();
        var lookupProvider = event.getLookupProvider();
        generator.addProvider(event.includeServer(), new RecipeGen(output, lookupProvider));
    }
}
