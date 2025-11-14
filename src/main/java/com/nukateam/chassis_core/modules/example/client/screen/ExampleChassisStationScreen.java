package com.nukateam.chassis_core.modules.example.client.screen;

import com.nukateam.chassis_core.ChassisCore;
import com.nukateam.chassis_core.client.gui.screen.ArmorStationScreen;
import com.nukateam.chassis_core.modules.example.common.container.ExampleChassisStationMenu;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

public class ExampleChassisStationScreen extends ArmorStationScreen<ExampleChassisStationMenu> {
    public ExampleChassisStationScreen(ExampleChassisStationMenu menu, Inventory pPlayerInventory, Component pTitle) {
        super(menu, pPlayerInventory, pTitle, ResourceLocation.tryBuild(ChassisCore.MOD_ID, "textures/screens/example_chassis/example_chassis_station_gui.png"));
    }
}
