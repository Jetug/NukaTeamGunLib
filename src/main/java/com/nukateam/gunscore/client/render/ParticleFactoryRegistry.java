package com.nukateam.gunscore.client.render;

import com.nukateam.gunscore.client.render.particle.BloodParticle;
import com.nukateam.gunscore.client.render.particle.BulletHoleParticle;
import com.nukateam.gunscore.client.render.particle.TrailParticle;
import com.nukateam.gunscore.common.foundation.init.ModParticleTypes;
import com.nukateam.gunscore.GunMod;
import net.minecraft.client.Minecraft;
import net.minecraft.client.particle.ParticleEngine;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.*;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/**
 * Author: MrCrayfish
 */
@Mod.EventBusSubscriber(modid = GunMod.MOD_ID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.MOD)
public class ParticleFactoryRegistry {
    @SubscribeEvent
    public static void onRegisterParticleFactory(ParticleFactoryRegisterEvent event) {
        ParticleEngine particleManager = Minecraft.getInstance().particleEngine;
        particleManager.register(ModParticleTypes.BULLET_HOLE.get(),
                (typeIn, worldIn, x, y, z, xSpeed, ySpeed, zSpeed) ->
                        new BulletHoleParticle(worldIn, x, y, z, typeIn.getDirection(), typeIn.getPos()));
        particleManager.register(ModParticleTypes.BLOOD.get(), BloodParticle.Factory::new);
        particleManager.register(ModParticleTypes.TRAIL.get(), TrailParticle.Factory::new);
    }
}
