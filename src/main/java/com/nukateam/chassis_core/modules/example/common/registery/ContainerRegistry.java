package com.nukateam.chassis_core.modules.example.common.registery;

import com.nukateam.chassis_core.ChassisCore;
import com.nukateam.chassis_core.modules.example.common.container.ExampleChassisMenu;
import com.nukateam.chassis_core.modules.example.common.container.ExampleChassisStationMenu;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraftforge.common.extensions.IForgeMenuType;
import net.neoforged.common.extensions.IForgeMenuType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.network.IContainerFactory;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.Registries;
import net.neoforged.neoforge.registries.DeferredHolder;

public class ContainerRegistry {
    public static final DeferredRegister<MenuType<?>> CONTAINERS = DeferredRegister
            .create(Registries.MENU, ChassisCore.MOD_ID);

    public static final DeferredHolder<MenuType<ExampleChassisMenu>> EXAMPLE_CHASSIS_MENU
            = registerMenuType("example_chassis_menu", ExampleChassisMenu::new);

    public static final DeferredHolder<MenuType<ExampleChassisStationMenu>> EXAMPLE_STATION_MENU
            = registerMenuType("example_chassis_station_menu", ExampleChassisStationMenu::new);

    private static <T extends AbstractContainerMenu> DeferredHolder<MenuType<T>> registerMenuType(String name, IContainerFactory<T> factory) {
        return CONTAINERS.register(name, () -> IForgeMenuType.create(factory));
    }

    public static void register(IEventBus eventBus) {
        CONTAINERS.register(eventBus);
    }
}
