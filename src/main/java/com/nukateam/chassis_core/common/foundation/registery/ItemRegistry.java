package com.nukateam.chassis_core.common.foundation.registery;

import com.nukateam.chassis_core.ChassisCore;
import net.minecraft.world.item.Item;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.minecraft.core.registries.Registries;

import java.util.function.Supplier;

public class ItemRegistry {
    public static final DeferredRegister<Item> ITEMS =
            DeferredRegister.create(Registries.ITEM, ChassisCore.MOD_ID);

    public static void register(IEventBus eventBus) {
        ITEMS.register(eventBus);
    }

    private static <I extends Item> I registerItem(final String name, final Supplier<? extends I> sup) {
        return ITEMS.register(name, sup).get();
    }
}
