package com.nukateam.ntgl.modules.gunpack.regestry;

import com.nukateam.example.common.registery.ModGuns;
import com.nukateam.ntgl.Ntgl;
import com.nukateam.ntgl.common.util.helpers.RegistrationHelper;
import com.nukateam.ntgl.modules.gunpack.GunPackModule;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

import javax.annotation.Nullable;

import static net.minecraft.world.item.CreativeModeTab.Output;
import static net.minecraft.world.item.CreativeModeTab.builder;

public class GunTabs {
    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS =
            DeferredRegister.create(Registries.CREATIVE_MODE_TAB, GunPackModule.MOD_ID);

    @Nullable
    public static final RegistryObject<CreativeModeTab> ITEMS = createNtglTab();

    private static RegistryObject<CreativeModeTab> createNtglTab() {
        return CREATIVE_MODE_TABS.register("guns",
                () -> builder().icon(() -> new ItemStack(ModGuns.ROUND10MM.get()))
                        .title(Component.translatable("itemGroup.ntgl.guns"))
                        .displayItems((params, output) -> registerItems(output))
                        .build());


    }

    private static void registerItems(Output output) {
        for (var entry : ModGuns.ITEMS.getEntries()) {
            RegistrationHelper.registerGunOrDefault(output, entry.get());
        }
    }

    public static void register(IEventBus eventBus) {
        CREATIVE_MODE_TABS.register(eventBus);
    }
}
