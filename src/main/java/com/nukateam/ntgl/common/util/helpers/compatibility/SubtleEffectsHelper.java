package com.nukateam.ntgl.common.util.helpers.compatibility;

import einstein.subtle_effects.init.ModConfigs;
import einstein.subtle_effects.init.ModParticles;
import einstein.subtle_effects.particle.emitter.SplashEmitter;
import einstein.subtle_effects.particle.option.SplashEmitterParticleOptions;
import einstein.subtle_effects.util.ParticleSpawnUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

public class SubtleEffectsHelper {
    public static boolean doSplashEffect(Entity entity) {
        var delta = entity.getDeltaMovement();
        var particle = entity.isInLava() ? ModParticles.LAVA_SPLASH_EMITTER.get() : ModParticles.WATER_SPLASH_EMITTER.get();
        var yPos = entity.getY() + entity.getFluidHeight(FluidTags.WATER);
        var velocity = delta.length();

        if (!ModConfigs.ENTITIES.splashes.splashEffects) {
            return false;
        } else if (ModConfigs.ENTITIES.splashes.entityBlocklist.contains(entity.getType())) {
            return false;
        } else {
            double offset = yPos + 0.01;
            if (offset <= yPos + (double)entity.getBbHeight()) {
                var splashEmitter = SplashEmitter.createForEntity(entity, particle, velocity);
                entity.level().addAlwaysVisibleParticle(splashEmitter, true,
                        entity.getX(), offset, entity.getZ(),
                        0.0F, 0.0F, 0.0F);
                return true;
            }

            return false;
        }
//        return ParticleSpawnUtil.spawnSplashEffects(entity, entity.level(),
//                particle, yPos, delta.length() * 2);
    }

    public static boolean doSplashEffect2(Entity entity) {
        var delta = entity.getDeltaMovement();
        var emitter = entity.isInLava() ? ModParticles.LAVA_SPLASH_EMITTER.get() : ModParticles.WATER_SPLASH_EMITTER.get();

        var yPos = entity.getY() + entity.getFluidHeight(FluidTags.WATER);

        return ParticleSpawnUtil.spawnSplashEffects(entity, entity.level(),
                emitter, yPos, delta.y * 2);
    }

    public static void doExplosionSplash(Level level, float radius, Vec3 position) {
        if (level.isClientSide && ModConfigs.ENTITIES.splashes.explosionsCauseSplashes) {
            var pos = BlockPos.containing(position);
            var fluidState = level.getFluidState(pos);

            if (!fluidState.isEmpty()) {
                int blockY = pos.getY();

                for (int y = blockY; y < blockY + (radius) + 1; y++) {
                    var currentPos = pos.atY(y);
                    var currentFluidState = level.getFluidState(currentPos);

                    if (fluidState.getType().isSame(currentFluidState.getType())) {
                        continue;
                    }

                    if (level.getBlockState(currentPos).isSolidRender(level, currentPos)) {
                        return;
                    }

                    var type = fluidState.is(FluidTags.WATER) ?
                            ModParticles.WATER_SPLASH_EMITTER.get() :
                            fluidState.is(FluidTags.LAVA) ? ModParticles.LAVA_SPLASH_EMITTER.get() : null;

                    if (type != null) {
                        var surfacePos = currentPos.below();
                        var surfaceFluidState = level.getFluidState(surfacePos);
                        var scale = radius - ((y - blockY) / radius);

                        level.addAlwaysVisibleParticle(new SplashEmitterParticleOptions(type, scale, scale * (scale * 0.1F), -1, -1),
                                true, position.x, surfacePos.getY() + surfaceFluidState.getHeight(level, surfacePos) + 0.01, position.z,
                                0, 0, 0
                        );
                    }
                    return;
                }
            }
        }
    }
}
