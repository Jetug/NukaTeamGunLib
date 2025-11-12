package com.nukateam.example.common.registery;

import com.nukateam.ntgl.Ntgl;
import com.nukateam.ntgl.common.util.helpers.RegistrationHelper;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

import javax.annotation.Nullable;

import static net.minecraft.world.item.CreativeModeTab.*;

public class ModItemTabs {
    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS =
            DeferredRegister.create(Registries.CREATIVE_MODE_TAB, Ntgl.MOD_ID);

    @Nullable
    public static final RegistryObject<CreativeModeTab> ITEMS = createNtglTab();

    private static RegistryObject<CreativeModeTab> createNtglTab() {
        if(Ntgl.isDebugging()) {
            return CREATIVE_MODE_TABS.register("ntgl_items",
                    () -> builder().icon(() -> new ItemStack(ExampleWeapons.ROUND10MM.get()))
                            .title(Component.translatable("itemGroup.ntgl.example"))
                            .displayItems((params, output) -> registerItems(output))
                            .build());
        }
        return null;
    }

    private static void registerItems(Output output) {
        for (var entry : ExampleWeapons.ITEMS.getEntries()) {
            RegistrationHelper.registerGunOrDefault(output, entry.get());
        }
    }

    public static void register(IEventBus eventBus) {
        CREATIVE_MODE_TABS.register(eventBus);
    }
}
