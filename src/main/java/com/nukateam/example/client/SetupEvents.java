package com.nukateam.example.client;

import com.nukateam.ntgl.Ntgl;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;

import net.neoforged.fml.common.EventBusSubscriber;

import static com.nukateam.example.common.registery.EntityTypes.*;

@OnlyIn(Dist.CLIENT)
@EventBusSubscriber(modid = Ntgl.MOD_ID, value = Dist.CLIENT)
public class SetupEvents {
    @OnlyIn(Dist.CLIENT)
    @SubscribeEvent()
    public static void onRegisterRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerEntityRenderer(RAIDER.get(), RaiderRenderer::new);
        event.registerEntityRenderer(BRAHMIN.get(), ExampleGeoRenderer::new);
    }
}
