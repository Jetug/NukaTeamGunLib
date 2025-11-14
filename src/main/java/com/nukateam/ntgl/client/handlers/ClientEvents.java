package com.nukateam.ntgl.client.handlers;

import com.nukateam.ntgl.*;
import com.nukateam.ntgl.client.model.GunIconModels;
import com.nukateam.ntgl.client.tooltip.*;
import com.nukateam.ntgl.client.render.hud.*;
import com.nukateam.ntgl.client.tooltip.ItemsTooltipData;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.*;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ModelEvent;
import net.neoforged.neoforge.client.event.RegisterClientTooltipComponentFactoriesEvent;
import net.neoforged.neoforge.client.event.RegisterGuiLayersEvent;
import net.neoforged.neoforge.client.gui.VanillaGuiLayers;

@EventBusSubscriber(modid = Ntgl.MOD_ID, value = Dist.CLIENT)
public class ClientEvents {
    @SubscribeEvent
    public static void onRegisterTooltip(RegisterClientTooltipComponentFactoriesEvent event) {
        event.register(ItemsTooltipData.class, ItemsClientTooltipComponent::new);
    }

    @SubscribeEvent
    public static void registerModels(ModelEvent.RegisterGeometryLoaders event) {
        var key = ResourceLocation.fromNamespaceAndPath(Ntgl.MOD_ID, "gun_icon_loader");
        event.register(key, GunIconModels.Loader.INSTANCE);
    }

    @SubscribeEvent
    public static void registerOverlay(final RegisterGuiLayersEvent event) {
        event.registerBelow(VanillaGuiLayers.HOTBAR,
                ResourceLocation.fromNamespaceAndPath(Ntgl.MOD_ID, "ammo"),
                WeaponHud::render);

        event.registerBelow(VanillaGuiLayers.HOTBAR,
                ResourceLocation.fromNamespaceAndPath(Ntgl.MOD_ID, "debug"),
                DebugHud::render);

        event.registerBelow(VanillaGuiLayers.HOTBAR,
                ResourceLocation.fromNamespaceAndPath(Ntgl.MOD_ID, "scope"),
                ScopeHud::render);
    }

}
