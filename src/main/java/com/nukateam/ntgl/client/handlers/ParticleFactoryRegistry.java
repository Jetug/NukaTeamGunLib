package com.nukateam.ntgl.client.handlers;

import com.nukateam.ntgl.Ntgl;
import com.nukateam.ntgl.client.render.particle.BloodParticle;
import com.nukateam.ntgl.client.render.particle.BulletHoleParticle;
import com.nukateam.ntgl.client.render.particle.TrailParticle;
import com.nukateam.ntgl.common.foundation.init.ModParticleTypes;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.neoforge.client.event.*;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;

/**
 * Author: MrCrayfish
 */
@EventBusSubscriber(modid = Ntgl.MOD_ID, value = Dist.CLIENT)
public class ParticleFactoryRegistry {
    @SubscribeEvent
    public static void onRegisterParticleFactory(RegisterParticleProvidersEvent event) {
        event.registerSpecial(ModParticleTypes.BULLET_HOLE.get(),
                (typeIn, worldIn, x, y, z, xSpeed, ySpeed, zSpeed) ->
                        new BulletHoleParticle(worldIn, x, y, z, typeIn.direction(), typeIn.pos()));
        event.registerSpriteSet(ModParticleTypes.BLOOD.get(), BloodParticle.Factory::new);
        event.registerSpriteSet(ModParticleTypes.TRAIL.get(), TrailParticle.Factory::new);
    }
}
