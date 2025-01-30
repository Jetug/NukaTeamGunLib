package com.nukateam.ntgl.client.event;

import com.nukateam.ntgl.Ntgl;
import com.nukateam.ntgl.client.render.hud.DebugHud;
import com.nukateam.ntgl.client.render.hud.GunHud;
import com.nukateam.ntgl.client.render.hud.ScopeHud;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterGuiOverlaysEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = Ntgl.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class ClientEvents {
     @SubscribeEvent
     public static void registerHud(RegisterGuiOverlaysEvent event){
         event.registerAboveAll("debug", GunHud.AMMO_HUD);
         event.registerAboveAll("ammo", DebugHud.DEBUG_HUD);
         event.registerBelowAll("scope", ScopeHud.SCOPE_HUD);
     }
}
