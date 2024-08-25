package com.nukateam.example.common.registery;

import com.nukateam.ntgl.Ntgl;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

import static net.minecraft.world.item.CreativeModeTab.*;

public class ModItemTabs {
    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS =
            DeferredRegister.create(Registries.CREATIVE_MODE_TAB, Ntgl.MOD_ID);

    public static final RegistryObject<CreativeModeTab> ITEMS = CREATIVE_MODE_TABS.register("ntgl_items",
            () -> builder().icon(() -> new ItemStack(ModGuns.ROUND10MM.get()))
                    .title(Component.translatable("itemGroup.ntgl"))
                    .displayItems((params, output) -> registerItems(output))
                    .build());

    private static void registerItems(Output output) {
        if(Ntgl.isDebugging()) {
            for (var entry : ModGuns.ITEMS.getEntries()) {
                output.accept(entry.get());
            }
        }
    }

    public static void register(IEventBus eventBus) {
        CREATIVE_MODE_TABS.register(eventBus);
    }
}
