package com.nukateam.chassis_core.common.foundation.registery;

import com.nukateam.chassis_core.ChassisCore;
import com.nukateam.chassis_core.common.foundation.container.menu.DynamicChassisMenu;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.flag.FeatureFlagSet;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.network.IContainerFactory;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.minecraft.core.registries.Registries;
import net.neoforged.neoforge.registries.DeferredHolder;

public class ContainerRegistry {
    public static final DeferredRegister<MenuType<?>> CONTAINERS = DeferredRegister
            .create(Registries.MENU, ChassisCore.MOD_ID);

    public static final DeferredHolder<MenuType<?>, MenuType<DynamicChassisMenu>> CHASSIS_MENU
            = registerMenuType("chassis_menu", DynamicChassisMenu::new);

    private static <T extends AbstractContainerMenu> DeferredHolder<MenuType<?>, MenuType<T>> registerMenuType(String name, IContainerFactory<T> factory) {
        return CONTAINERS.register(name, () ->
                new MenuType<>(factory, FeatureFlagSet.of())
        );

    }

    public static void register(IEventBus eventBus) {
        CONTAINERS.register(eventBus);
    }
}
