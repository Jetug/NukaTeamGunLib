package com.nukateam.ntgl.common.util.helpers.compatibility;

import einstein.subtle_effects.init.ModConfigs;
import einstein.subtle_effects.particle.option.SplashEmitterParticleOptions;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.Vec3;

public class SubtleEffectsHelper {
    public static boolean doSplashEffect(Entity entity, Vec3 pos, boolean inLava) {
        var delta = entity.getDeltaMovement();
        var fluid = BuiltInRegistries.FLUID.getKey(inLava ? Fluids.LAVA : Fluids.WATER);
        var velocity = (float) delta.length();

        if (!ModConfigs.ENTITIES.splashes.splashEffects.get()) {
            return false;
        } else {
            var splashEmitter = new SplashEmitterParticleOptions(fluid, entity.getBbWidth(), entity.getBbHeight() * velocity, -1, -1);
            entity.level().addAlwaysVisibleParticle(splashEmitter, true,
                    pos.x(), pos.y() + 0.01, pos.z(),
                    0.0F, 0.0F, 0.0F);
            return true;
        }
    }

    public static boolean doSplashEffect(Vec3 pos, float size, float speed, boolean isInLava) {
        var level = Minecraft.getInstance().level;
        var fluid = BuiltInRegistries.FLUID.getKey(isInLava ? Fluids.LAVA : Fluids.WATER);

        var ratio = isInLava ? 2f : 1f;

        if (!ModConfigs.ENTITIES.splashes.splashEffects.get()) {
            return false;
        } else {
            var splashEmitter = new SplashEmitterParticleOptions(fluid, size, size * speed / ratio, -1, -1);
            level.addAlwaysVisibleParticle(splashEmitter, true,
                    pos.x(), pos.y() + 0.01, pos.z(),
                    0.0F, 0.0F, 0.0F);
            return true;
        }
    }

    public static void doExplosionSplash(Level level, float radius, Vec3 position) {
        if (level.isClientSide && ModConfigs.ENTITIES.splashes.explosionsCauseSplashes.get()) {
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
                            BuiltInRegistries.FLUID.getKey(Fluids.WATER) :
                            fluidState.is(FluidTags.LAVA) ? BuiltInRegistries.FLUID.getKey(Fluids.LAVA) : null;

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
