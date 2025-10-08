package com.nukateam.ntgl.client.handlers;

import com.nukateam.ntgl.*;
import com.nukateam.ntgl.client.model.GunIconModels;
import com.nukateam.ntgl.client.tooltip.*;
import com.nukateam.ntgl.client.render.hud.*;
import com.nukateam.ntgl.client.tooltip.ItemsTooltipData;
import net.minecraftforge.api.distmarker.*;
import net.minecraftforge.client.event.*;
import net.minecraftforge.eventbus.api.*;
import net.minecraftforge.fml.common.*;

@Mod.EventBusSubscriber(modid = Ntgl.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class ClientEvents {
    @SubscribeEvent
    public static void onRegisterTooltip(RegisterClientTooltipComponentFactoriesEvent event) {
        event.register(ItemsTooltipData.class, ItemsClientTooltipComponent::new);
    }

    @SubscribeEvent
    public static void registerModels(ModelEvent.RegisterGeometryLoaders event) {
        event.register("gun_icon_loader", GunIconModels.Loader.INSTANCE);
    }

     @SubscribeEvent
     public static void registerHud(RegisterGuiOverlaysEvent event){
         event.registerAboveAll("ammo", GunHud.AMMO_HUD);
//         event.registerAboveAll("throwable", ThrowableHud.AMMO_HUD);
         event.registerAboveAll("debug", DebugHud.DEBUG_HUD);
         event.registerBelowAll("scope", ScopeHud.SCOPE_HUD);
     }
}
