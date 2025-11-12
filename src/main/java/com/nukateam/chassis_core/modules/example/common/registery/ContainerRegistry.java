package com.nukateam.chassis_core.modules.example.common.registery;

import com.nukateam.chassis_core.ChassisCore;
import com.nukateam.chassis_core.modules.example.common.container.ExampleChassisMenu;
import com.nukateam.chassis_core.modules.example.common.container.ExampleChassisStationMenu;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraftforge.common.extensions.IForgeMenuType;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.network.IContainerFactory;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ContainerRegistry {
    public static final DeferredRegister<MenuType<?>> CONTAINERS = DeferredRegister
            .create(ForgeRegistries.MENU_TYPES, ChassisCore.MOD_ID);

    public static final RegistryObject<MenuType<ExampleChassisMenu>> EXAMPLE_CHASSIS_MENU
            = registerMenuType("example_chassis_menu", ExampleChassisMenu::new);

    public static final RegistryObject<MenuType<ExampleChassisStationMenu>> EXAMPLE_STATION_MENU
            = registerMenuType("example_chassis_station_menu", ExampleChassisStationMenu::new);

    private static <T extends AbstractContainerMenu> RegistryObject<MenuType<T>> registerMenuType(String name, IContainerFactory<T> factory) {
        return CONTAINERS.register(name, () -> IForgeMenuType.create(factory));
    }

    public static void register(IEventBus eventBus) {
        CONTAINERS.register(eventBus);
    }
}
