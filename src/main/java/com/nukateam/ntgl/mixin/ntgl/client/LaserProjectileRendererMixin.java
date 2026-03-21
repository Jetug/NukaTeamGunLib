package com.nukateam.ntgl.mixin.ntgl.client;

import com.nukateam.ntgl.client.render.renderers.projectiles.LaserProjectileRenderer;
import com.nukateam.ntgl.common.foundation.entity.LaserProjectile;
import com.nukateam.ntgl.client.helpers.MuzzleMatrixHelper;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.world.entity.Entity;
import org.joml.Vector3f;

@Mixin(LaserProjectileRenderer.class)
public class LaserProjectileRendererMixin {

    @Redirect(method = "render(Lcom/nukateam/ntgl/common/foundation/entity/LaserProjectile;FFLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;I)V",
            at = @At(value = "INVOKE", target = "Lcom/nukateam/ntgl/client/render/renderers/projectiles/LaserProjectileRenderer;getBeamOffset()Lorg/joml/Vector3f;"),
            remap = false)
    private Vector3f ntgl$zeroBeamOffsetForFirstPerson(LaserProjectileRenderer instance,
            LaserProjectile projectile, float entityYaw, float partialTicks,
            PoseStack poseStack, MultiBufferSource bufferSource, int light) {
        int shooterId = projectile.getShooterId();
        Vec3 muzzlePos = MuzzleMatrixHelper.getMuzzleWorldPosForEntity(shooterId, partialTicks);
        if (muzzlePos != null) {
            return new Vector3f(0, 0, 0);
        }
        return new Vector3f(0.2f, 0.25f, 0.07f);
    }

    @Inject(method = "render(Lcom/nukateam/ntgl/common/foundation/entity/LaserProjectile;FFLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;I)V",
            at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/vertex/PoseStack;pushPose()V", remap = true),
            remap = false)
    private void ntgl$applyMuzzleOffset(LaserProjectile projectile, float entityYaw, float partialTicks,
            PoseStack poseStack, MultiBufferSource bufferSource, int light, CallbackInfo ci) {
        int shooterId = projectile.getShooterId();
        Vec3 muzzleWorldPos = MuzzleMatrixHelper.getMuzzleWorldPosForEntity(shooterId, partialTicks);

        if (muzzleWorldPos != null) {
            double projX = net.minecraft.util.Mth.lerp((double) partialTicks, projectile.xOld, projectile.getX());
            double projY = net.minecraft.util.Mth.lerp((double) partialTicks, projectile.yOld, projectile.getY());
            double projZ = net.minecraft.util.Mth.lerp((double) partialTicks, projectile.zOld, projectile.getZ());

            poseStack.translate(muzzleWorldPos.x() - projX, muzzleWorldPos.y() - projY, muzzleWorldPos.z() - projZ);
        }
    }

    @Redirect(method = "render(Lcom/nukateam/ntgl/common/foundation/entity/LaserProjectile;FFLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;I)V",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/Entity;getEyePosition(F)Lnet/minecraft/world/phys/Vec3;"))
    private Vec3 redirectLaserStartPos(Entity shooter, float pPartialTicks,
            LaserProjectile projectile, float entityYaw, float partialTicks,
            PoseStack poseStack, MultiBufferSource bufferSource, int light) {
        Vec3 muzzlePos = MuzzleMatrixHelper.getMuzzleWorldPosForEntity(projectile.getShooterId(), partialTicks);
        if (muzzlePos != null) return muzzlePos;
        return shooter.getEyePosition(pPartialTicks);
    }
}
