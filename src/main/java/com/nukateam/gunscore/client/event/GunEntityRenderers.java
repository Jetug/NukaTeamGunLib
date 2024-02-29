package com.nukateam.gunscore.client.event;

import com.nukateam.gunscore.client.render.entity.GrenadeRenderer;
import com.nukateam.gunscore.client.render.entity.MissileRenderer;
import com.nukateam.gunscore.client.render.entity.ProjectileRenderer;
import com.nukateam.gunscore.client.render.entity.ThrowableGrenadeRenderer;
import com.nukateam.gunscore.common.foundation.init.ModEntities;
import com.nukateam.gunscore.GunMod;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = GunMod.MOD_ID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.MOD)
public class GunEntityRenderers {
    @SubscribeEvent
    public static void registerEntityRenders(EntityRenderersEvent.RegisterRenderers event) {
        event.registerEntityRenderer(ModEntities.PROJECTILE.get(), ProjectileRenderer::new);
        event.registerEntityRenderer(ModEntities.GRENADE.get(), GrenadeRenderer::new);
        event.registerEntityRenderer(ModEntities.BASEGRENADE.get(), GrenadeRenderer::new);
        event.registerEntityRenderer(ModEntities.MISSILE.get(), MissileRenderer::new);
        event.registerEntityRenderer(ModEntities.THROWABLE_GRENADE.get(), ThrowableGrenadeRenderer::new);
        event.registerEntityRenderer(ModEntities.THROWABLE_STUN_GRENADE.get(), ThrowableGrenadeRenderer::new);
    }
}
