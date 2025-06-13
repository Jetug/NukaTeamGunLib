package com.nukateam.ntgl.client.handlers;

import com.nukateam.ntgl.*;
import com.nukateam.ntgl.client.render.hud.*;
import net.minecraftforge.api.distmarker.*;
import net.minecraftforge.client.event.*;
import net.minecraftforge.eventbus.api.*;
import net.minecraftforge.fml.common.*;

@Mod.EventBusSubscriber(modid = Ntgl.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class ClientEvents {
     @SubscribeEvent
     public static void registerHud(RegisterGuiOverlaysEvent event){
         event.registerAboveAll("projectile", GunHud.AMMO_HUD);
         event.registerAboveAll("debug", DebugHud.DEBUG_HUD);
         event.registerBelowAll("scope", ScopeHud.SCOPE_HUD);
     }
}
