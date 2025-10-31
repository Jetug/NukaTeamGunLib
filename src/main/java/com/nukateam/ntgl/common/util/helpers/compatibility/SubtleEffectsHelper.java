package com.nukateam.ntgl.common.util.helpers.compatibility;

import einstein.subtle_effects.init.ModConfigs;
import einstein.subtle_effects.init.ModParticles;
import einstein.subtle_effects.particle.option.SplashEmitterParticleOptions;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

public class SubtleEffectsHelper {
    public static boolean doSplashEffect(Entity entity, Vec3 pos, boolean inLava) {
        var delta = entity.getDeltaMovement();
        var particle = inLava ? ModParticles.LAVA_SPLASH_EMITTER.get() : ModParticles.WATER_SPLASH_EMITTER.get();
        var fluid = inLava ? FluidTags.LAVA : FluidTags.WATER;
        var velocity = (float)delta.length();

        if (!ModConfigs.ENTITIES.splashes.splashEffects) {
            return false;
        } else {
//            var fluidHeight = entity.getFluidHeight(fluid);
//            var yPos = pos.y() + fluidHeight;
//            var offset = yPos + 0.01;
            var splashEmitter = new SplashEmitterParticleOptions(particle, entity.getBbWidth(), entity.getBbHeight() * velocity, -1, -1);
            entity.level().addAlwaysVisibleParticle(splashEmitter, true,
                    pos.x(), pos.y() + 0.01, pos.z(),
                    0.0F, 0.0F, 0.0F);
            return true;
        }
    }

    public static boolean doSplashEffect(Vec3 pos, float size, float speed, boolean isInLava) {
        var level = Minecraft.getInstance().level;
        var particle = isInLava ? ModParticles.LAVA_SPLASH_EMITTER.get() : ModParticles.WATER_SPLASH_EMITTER.get();

        if (!ModConfigs.ENTITIES.splashes.splashEffects) {
            return false;
        }
        else {
            var blockPos = BlockPos.containing(pos);
            var fluidHeight = level.getFluidState(blockPos).getHeight(level, blockPos);
            var yPos = pos.y() + fluidHeight;
            var offset = yPos + 0.01;
//            if (offset <= yPos + (double)entity.getBbHeight()) {
//                var splashEmitter = SplashEmitter.createForEntity(entity, particle, velocity);
            var splashEmitter = new SplashEmitterParticleOptions(particle, size, size * speed, -1, -1);

            level.addAlwaysVisibleParticle(splashEmitter, true,
                    pos.x(), offset, pos.z(),
                        0.0F, 0.0F, 0.0F);
                return true;
//            }

//            return false;
        }
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
