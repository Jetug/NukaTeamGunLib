package com.nukateam.ntgl.mixin.ntgl.client;

import com.nukateam.ntgl.common.foundation.entity.FlameProjectile;
import com.nukateam.ntgl.client.helpers.MuzzleMatrixHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(FlameProjectile.class)
public class FlameProjectileMixin {

    @Inject(method = "onProjectileTick", at = @At("HEAD"), cancellable = true, remap = false)
    private void ntgl$offsetFlameParticles(CallbackInfo ci) {
        FlameProjectile projectile = (FlameProjectile) (Object) this;
        Level level = projectile.level();

        if (level.isClientSide) {
            int shooterId = projectile.getShooterId();
            Entity entity = (Entity) (Object) this;

            double startX = entity.xOld;
            double startY = entity.yOld;
            double startZ = entity.zOld;

            double offsetX = 0;
            double offsetY = 0;
            double offsetZ = 0;

            if (entity.tickCount < 10) {
                Vec3 muzzleWorldPos = MuzzleMatrixHelper.getMuzzleWorldPosForEntity(shooterId, Minecraft.getInstance().getFrameTime());

                if (muzzleWorldPos != null) {
                    double blend = 1.0 - ((double) entity.tickCount / 10.0);
                    if (blend < 0) blend = 0;

                    startX = entity.xOld + (muzzleWorldPos.x() - entity.xOld) * blend;
                    startY = entity.yOld + (muzzleWorldPos.y() - entity.yOld) * blend;
                    startZ = entity.zOld + (muzzleWorldPos.z() - entity.zOld) * blend;

                    double endBlend = 1.0 - ((double) (entity.tickCount + 1) / 10.0);
                    if (endBlend < 0) endBlend = 0;
                    offsetX = (muzzleWorldPos.x() - projectile.getX()) * endBlend;
                    offsetY = (muzzleWorldPos.y() - projectile.getY()) * endBlend;
                    offsetZ = (muzzleWorldPos.z() - projectile.getZ()) * endBlend;
                }
            }

            double endX = projectile.getX() + offsetX;
            double endY = projectile.getY() + offsetY;
            double endZ = projectile.getZ() + offsetZ;

            for (int i = 1; i <= 5; ++i) {
                double fraction = (double) i / 5.0;
                double px = startX + (endX - startX) * fraction;
                double py = startY + (endY - startY) * fraction;
                double pz = startZ + (endZ - startZ) * fraction;
                level.addParticle(ParticleTypes.FLAME, true, px, py, pz, 0.0, 0.0, 0.0);
            }
            if (level.random.nextInt(2) == 0) {
                level.addParticle(ParticleTypes.SMOKE, true, endX, endY, endZ, 0.0, 0.0, 0.0);
                level.addParticle(ParticleTypes.FLAME, true, endX, endY, endZ, 0.0, 0.0, 0.0);
            }
            ci.cancel();
        }
    }
}
