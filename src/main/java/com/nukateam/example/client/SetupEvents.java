package com.nukateam.example.client;

import com.nukateam.ntgl.Ntgl;
import com.nukateam.ntgl.client.render.renderers.projectiles.ThrowableGrenadeRenderer;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import static com.nukateam.example.common.registery.EntityTypes.*;

@Mod.EventBusSubscriber(modid = Ntgl.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class SetupEvents {
    @OnlyIn(Dist.CLIENT)
    @SubscribeEvent()
    public static void onRegisterRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerEntityRenderer(RAIDER.get(), RaiderRenderer::new);
//        event.registerEntityRenderer(DEATHCLAW.get(), DeathclawRenderer::new);

        event.registerEntityRenderer(BRAHMIN.get(), (context) -> new SimpleEntityRenderer<>(context, new BrahminModel()));
    }


}
