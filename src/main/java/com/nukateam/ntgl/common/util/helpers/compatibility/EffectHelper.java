package com.nukateam.ntgl.common.util.helpers.compatibility;

import com.nukateam.ntgl.Ntgl;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.sounds.*;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

public class EffectHelper {
    private static final RandomSource random = RandomSource.create();

    public static void doSplashEffect(Vec3 pos, float size, float speed, boolean isInLava) {
        if (Ntgl.subtleEffectsLoaded && SubtleEffectsHelper.doSplashEffect(pos, size, speed, isInLava))
            return;
        if(!isInLava){
            doWaterSplashEffect(pos, size, speed);
        }
    }

    public static void doExplosionSplash(Level level, float radius, Vec3 position) {
        if(Ntgl.subtleEffectsLoaded) {
            SubtleEffectsHelper.doExplosionSplash(level, radius, position);
        }
    }

    private static void doWaterSplashEffect(Vec3 pos, float size, float speed) {
        var level = Minecraft.getInstance().level;
        var velocity = pos.multiply(speed, speed, speed);
        playSplashSound(pos);
        var waterLevelY = Mth.floor(pos.y) + 1.0F;

        for(int i = 0; i < 1.0F + size * 20.0F; ++i) {
            addBubbleParticle(pos, size, level, waterLevelY, velocity);
        }

        for(int j = 0; j < 1.0F + size * 20.0F; ++j) {
            addSplashParticle(pos, size, level, waterLevelY, velocity);
        }
    }

    private static void addSplashParticle(Vec3 pos, float size, ClientLevel level, float waterLevelY, Vec3 velocity) {
        var offsetX = (random.nextDouble() * 2.0D - 1.0D) * size;
        var offsetZ = (random.nextDouble() * 2.0D - 1.0D) * size;

        level.addParticle(
                ParticleTypes.SPLASH,
                pos.x + offsetX,
                waterLevelY,
                pos.z + offsetZ,
                velocity.x,
                velocity.y,
                velocity.z
        );
    }

    private static void addBubbleParticle(Vec3 pos, float size, ClientLevel level, float waterLevelY, Vec3 velocity) {
        var offsetX = (random.nextDouble() * 2.0D - 1.0D) * size;
        var offsetZ  = (random.nextDouble() * 2.0D - 1.0D) * size;
        level.addParticle(
                ParticleTypes.BUBBLE,
                pos.x + offsetX,
                waterLevelY,
                pos.z + offsetZ,
                velocity.x,
                velocity.y - random.nextDouble() * 0.2D,
                velocity.z
        );
    }

    private static void playSplashSound(Vec3 pos) {
        var volumeModifier = 0.2F;
        var splashStrength = (float) Math.sqrt(
                pos.x * pos.x * 0.2D +
                        pos.y * pos.y +
                        pos.z * pos.z * 0.2D
        ) * volumeModifier;
        splashStrength = Math.min(1.0F, splashStrength);

        var pitch = 1.0F + (random.nextFloat() - random.nextFloat()) * 0.4F;
        playSound(pos, SoundEvents.GENERIC_SPLASH, splashStrength, pitch);
    }

    public static void playSound(Vec3 pos, SoundEvent pSound, float pVolume, float pPitch) {
        var level = Minecraft.getInstance().level;
        level.playSound(null, pos.x(), pos.y(), pos.z(), pSound, SoundSource.NEUTRAL, pVolume, pPitch);
    }
}
