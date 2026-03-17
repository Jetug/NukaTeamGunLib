package com.nukateam.ntgl.mixin.ntgl.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.nukateam.ntgl.client.render.renderers.projectiles.ProjectileRenderer;
import com.nukateam.ntgl.common.foundation.entity.ProjectileEntity;
import com.nukateam.ntgl.client.helpers.MuzzleMatrixHelper;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ProjectileRenderer.class)
public class ProjectileRendererMixin {

    @Inject(method = "render(Lcom/nukateam/ntgl/common/foundation/entity/ProjectileEntity;FFLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;I)V", at = @At("HEAD"), remap = false)
    private void ntgl$applyMuzzleOffset(ProjectileEntity entity, float entityYaw, float partialTicks, PoseStack poseStack, MultiBufferSource bufferSource, int light, CallbackInfo ci) {
        if (entity.tickCount < 3) {
            int shooterId = ((ProjectileEntityAccessor) entity).ntgl$getShooter() != null
                    ? ((ProjectileEntityAccessor) entity).ntgl$getShooter().getId()
                    : -1;
            if (shooterId < 0) return;

            Vec3 muzzlePos = MuzzleMatrixHelper.getMuzzleWorldPosForEntity(shooterId, partialTicks);
            if (muzzlePos != null) {
                double entityX = Mth.lerp((double) partialTicks, entity.xOld, entity.getX());
                double entityY = Mth.lerp((double) partialTicks, entity.yOld, entity.getY());
                double entityZ = Mth.lerp((double) partialTicks, entity.zOld, entity.getZ());

                double offsetX = muzzlePos.x() - entityX;
                double offsetY = muzzlePos.y() - entityY;
                double offsetZ = muzzlePos.z() - entityZ;

                float progress = (entity.tickCount + partialTicks) / 3.0f;
                progress = Mth.clamp(progress, 0.0f, 1.0f);
                float multiplier = 1.0f - progress;

                poseStack.translate(offsetX * multiplier, offsetY * multiplier, offsetZ * multiplier);
            }
        }
    }
}
