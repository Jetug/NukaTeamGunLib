package com.nukateam.ntgl.mixin.ntgl.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.nukateam.ntgl.client.render.renderers.projectiles.TeslaProjectileRenderer;
import com.nukateam.ntgl.common.foundation.entity.TeslaProjectile;
import com.nukateam.ntgl.client.helpers.MuzzleMatrixHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(TeslaProjectileRenderer.class)
public class TeslaProjectileRendererMixin {

    @Inject(method = "render(Lcom/nukateam/ntgl/common/foundation/entity/TeslaProjectile;FFLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;I)V",
            at = @At("HEAD"), remap = false)
    private void ntgl$applyMuzzleOffset(TeslaProjectile projectile, float entityYaw, float partialTicks,
            PoseStack poseStack, MultiBufferSource bufferSource, int light, CallbackInfo ci) {
        int shooterId = projectile.getShooterId();
        Vec3 muzzleWorldPos = MuzzleMatrixHelper.getMuzzleWorldPosForEntity(shooterId, partialTicks);

        if (muzzleWorldPos != null) {
            double projX = net.minecraft.util.Mth.lerp((double) partialTicks, projectile.xOld, projectile.getX());
            double projY = net.minecraft.util.Mth.lerp((double) partialTicks, projectile.yOld, projectile.getY());
            double projZ = net.minecraft.util.Mth.lerp((double) partialTicks, projectile.zOld, projectile.getZ());

            double yOffset = 0.0;
            boolean isLocalFirstPerson = Minecraft.getInstance().player != null
                    && shooterId == Minecraft.getInstance().player.getId()
                    && Minecraft.getInstance().options.getCameraType().isFirstPerson();
            if (isLocalFirstPerson) {
                yOffset = 0.15;
            }

            poseStack.translate(muzzleWorldPos.x() - projX, muzzleWorldPos.y() - projY + yOffset, muzzleWorldPos.z() - projZ);
        }
    }

    @Redirect(method = "renderLightning", at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/vertex/PoseStack;translate(DDD)V", ordinal = 0), remap = false)
    private void ntgl$cancelSideOffset(PoseStack poseStack, double x, double y, double z, TeslaProjectile projectile, float partialTicks, PoseStack originalPoseStack, MultiBufferSource bufferSource, boolean isVertical) {
        int shooterId = projectile.getShooterId();
        Vec3 muzzleWorldPos = MuzzleMatrixHelper.getMuzzleWorldPosForEntity(shooterId, partialTicks);
        if (muzzleWorldPos != null) {
            poseStack.translate(0, 0, 0);
            return;
        }
        poseStack.translate(x, y, z);
    }
}
