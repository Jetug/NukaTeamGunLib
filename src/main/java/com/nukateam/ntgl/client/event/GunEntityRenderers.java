package com.nukateam.ntgl.client.event;

import com.nukateam.ntgl.client.render.renderers.projectiles.*;
import com.nukateam.ntgl.common.foundation.entity.FlyingGibs;
import com.nukateam.ntgl.common.foundation.init.Projectiles;
import com.nukateam.ntgl.Ntgl;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import com.nukateam.ntgl.client.render.renderers.*;


@Mod.EventBusSubscriber(modid = Ntgl.MOD_ID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.MOD)
public class GunEntityRenderers {
    @SubscribeEvent
    public static void registerEntityRenders(EntityRenderersEvent.RegisterRenderers event) {
        event.registerEntityRenderer(Projectiles.PROJECTILE.get(), ProjectileRenderer::new);
        event.registerEntityRenderer(Projectiles.LASER_PROJECTILE.get(), LaserProjectileRenderer::new);
        event.registerEntityRenderer(Projectiles.CONTINUOUS_LASER_PROJECTILE.get(), LaserProjectileRenderer::new);
        event.registerEntityRenderer(Projectiles.TESLA_PROJECTILE.get(), TeslaProjectileRenderer::new);
        event.registerEntityRenderer(Projectiles.FLAME_PROJECTILE.get(), FlameRenderer::new);
        event.registerEntityRenderer(Projectiles.GRENADE.get(), GrenadeRenderer::new);
        event.registerEntityRenderer(Projectiles.MISSILE.get(), MissileRenderer::new);
        event.registerEntityRenderer(Projectiles.THROWABLE_GRENADE.get(), ThrowableGrenadeRenderer::new);
        event.registerEntityRenderer(Projectiles.THROWABLE_STUN_GRENADE.get(), ThrowableGrenadeRenderer::new);

        event.registerEntityRenderer(Projectiles.FLYING_GIBS.get(), RenderFlyingGibs::new);
    }
}
