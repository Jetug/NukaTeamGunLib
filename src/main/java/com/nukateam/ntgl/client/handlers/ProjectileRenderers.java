package com.nukateam.ntgl.client.handlers;

import com.nukateam.ntgl.client.render.renderers.misc.AshPileRenderer;
import com.nukateam.ntgl.client.render.renderers.misc.FlyingGibsRenderer;
import com.nukateam.ntgl.client.render.renderers.projectiles.*;
import com.nukateam.ntgl.common.foundation.init.ModEntityTypes;
import com.nukateam.ntgl.common.foundation.init.Projectiles;
import com.nukateam.ntgl.Ntgl;
import net.neoforged.api.distmarker.Dist;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;


@EventBusSubscriber(modid = Ntgl.MOD_ID, value = Dist.CLIENT)
public class ProjectileRenderers {
    @SubscribeEvent
    public static void registerEntityRenders(EntityRenderersEvent.RegisterRenderers event) {
        event.registerEntityRenderer(Projectiles.PROJECTILE.get(), ProjectileRenderer::new);
        event.registerEntityRenderer(Projectiles.ARROW_LIKE.get(), ProjectileRenderer::new);
        event.registerEntityRenderer(Projectiles.LASER_PROJECTILE.get(), LaserProjectileRenderer::new);
        event.registerEntityRenderer(Projectiles.CONTINUOUS_LASER_PROJECTILE.get(), LaserProjectileRenderer::new);
        event.registerEntityRenderer(Projectiles.TESLA_PROJECTILE.get(), TeslaProjectileRenderer::new);
        event.registerEntityRenderer(Projectiles.FLAME_PROJECTILE.get(), FlameRenderer::new);
        event.registerEntityRenderer(Projectiles.GRENADE.get(), GrenadeRenderer::new);
        event.registerEntityRenderer(Projectiles.MISSILE.get(), MissileRenderer::new);
        event.registerEntityRenderer(Projectiles.THROWABLE_GRENADE.get(), ThrowableItemRenderer::new);
        event.registerEntityRenderer(Projectiles.THROWABLE_STUN_GRENADE.get(), ThrowableItemRenderer::new);

        event.registerEntityRenderer(ModEntityTypes.FLYING_GIBS.get(), FlyingGibsRenderer::new);
        event.registerEntityRenderer(ModEntityTypes.ASH_PILE.get(), AshPileRenderer::new);
    }
}
