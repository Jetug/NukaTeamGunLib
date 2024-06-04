package com.nukateam.ntgl.client.event;

import com.nukateam.ntgl.Ntgl;
import com.nukateam.ntgl.client.render.hud.AmmoHud;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterGuiOverlaysEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = Ntgl.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class ClientEvents {
     @SubscribeEvent
     public static void registerHud(RegisterGuiOverlaysEvent event){
        event.registerAbove(new ResourceLocation("hotbar"), "ammo", AmmoHud.AMMO_HUD);
     }
}
