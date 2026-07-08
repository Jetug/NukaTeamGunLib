package com.nukateam.ntgl.modules.crafting;

import com.nukateam.ntgl.modules.crafting.registry.ModRecipeSerializers;
import com.nukateam.ntgl.modules.crafting.registry.ModRecipeTypes;
import net.neoforged.bus.api.IEventBus;

public class CraftingModule {
    public static void init(IEventBus eventBus) {
        ModRecipeSerializers.REGISTER.register(eventBus);
        ModRecipeTypes.REGISTER.register(eventBus);
    }
}
