package com.nukateam.ntgl.client.render.renderers.projectiles;

import com.mojang.blaze3d.vertex.*;

import com.mojang.math.Axis;
import com.nukateam.ntgl.Ntgl;
import com.nukateam.ntgl.client.data.util.RenderUtils;
import com.nukateam.ntgl.common.data.util.Rgba;
import com.nukateam.ntgl.common.foundation.entity.LaserProjectile;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import org.jetbrains.annotations.NotNull;

public class LaserProjectileRenderer extends EntityRenderer<LaserProjectile> {
    public static final float BEAM_ALPHA = 0.7F;
    public static ResourceLocation LASER_TEXTURE = new ResourceLocation(Ntgl.MOD_ID, "textures/fx/laser.png");
    private static final float LASER_RADIUS = 0.05F / 4;
    private static final float laserGlowRadius = 0.055F / 4;

    public LaserProjectileRenderer(EntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    public ResourceLocation getTextureLocation(LaserProjectile entity) {
        return LASER_TEXTURE;
    }

    @Override
    public boolean shouldRender(LaserProjectile pLivingEntity, Frustum pCamera, double pCamX, double pCamY, double pCamZ) {
        return true;
    }

    @Override
    public void render(LaserProjectile projectile, float entityYaw, float partialTicks,
                       PoseStack poseStack, @NotNull MultiBufferSource bufferSource, int light) {
        var prog = ((float) projectile.tickCount) / ((float) projectile.getLife());
        var sin = Math.sin(Math.sqrt(prog) * Math.PI);
//        var radius = LASER_RADIUS;
        var radius = (float) (LASER_RADIUS * sin * 2);
        var glowRadius = (float) (laserGlowRadius * sin * 2);

        Ntgl.LOGGER.debug("laser render");

        poseStack.pushPose();
        {
            var playerPos = projectile.getEndVec();
            var laserPos = projectile.getStartVec();

            var pos = playerPos.subtract(laserPos);
            var offsetX = 0.20f;
            var offsetY = 0.25f;
            var offsetZ = 0.07f;
            var distance = projectile.getDistance() - offsetY;

            pos = pos.normalize();
            float yPos = (float) Math.acos(pos.y);
            float xzPos = (float) Math.atan2(pos.z, pos.x);

            var side = projectile.isRightHand() ? -1 : 1;

            poseStack.mulPose(Axis.YP.rotationDegrees((((float) Math.PI / 2F) - xzPos) * (180F / (float) Math.PI)));
            poseStack.mulPose(Axis.XP.rotationDegrees(yPos * (180F / (float) Math.PI)));

            poseStack.translate(side * offsetX, offsetY, offsetZ);
//            poseStack.mulPose(Axis.ZP.rotationDegrees(180F));

            long gameTime = projectile.level().getGameTime();
            int yOffset = 0; //(int) projectile.position().y;
            var color = new Rgba(1, 1, 1, 1);

            renderBeam(poseStack, bufferSource, getTextureLocation(projectile), partialTicks, 1.0F,
                    gameTime, (float) yOffset, distance, color, radius, glowRadius);
        }
        poseStack.popPose();
    }

    public static void renderBeam(PoseStack pPoseStack, MultiBufferSource pBufferSource, ResourceLocation pBeamLocation,
                                  float pPartialTick, float pTextureScale, long gameTime, float pYOffset, float pHeight,
                                  Rgba pColors, float pBeamRadius, float pGlowRadius) {
        var maxY = pYOffset + pHeight;
        pPoseStack.pushPose();

        //jet
//        pPoseStack.translate(0.5D, 0.0D, 0.5D);
        float f = (float) Math.floorMod(gameTime, 40) + pPartialTick;
        float f1 = pHeight < 0 ? f : -f;
        float f2 = Mth.frac(f1 * 0.2F - (float) Mth.floor(f1 * 0.1F));
        pPoseStack.pushPose();

//        pPoseStack.mulPose(Axis.YP.rotationDegrees(f * 2.25F - 45.0F));
        var minX = -pGlowRadius;
        var maxX = -pGlowRadius;
        var minZ = -pGlowRadius;
        var maxZ = -pBeamRadius;
        var f12 = -pBeamRadius;
        var v = -1.0F + f2;
        var u = pHeight * pTextureScale * (BEAM_ALPHA / pBeamRadius) + v;

        var vertexConsumer = pBufferSource
                .getBuffer(RenderType.beaconBeam(pBeamLocation, false));

        RenderUtils.renderPart(pPoseStack, vertexConsumer, pColors.setAlpha(1.0F),
                pYOffset, maxY,
                0.0F, pBeamRadius,
                pBeamRadius, 0.0F,
                maxZ, 0.0F,
                0.0F, f12,
                u, v);

        pPoseStack.popPose();

        maxZ = -pGlowRadius;
        v = -1.0F + f2;
        u = pHeight * pTextureScale + v;

        RenderUtils.renderPart(pPoseStack, pBufferSource.getBuffer(RenderType.beaconBeam(pBeamLocation, true)),
                pColors.setAlpha(BEAM_ALPHA), pYOffset, maxY, minX, maxX, pGlowRadius, minZ, maxZ,
                pGlowRadius, pGlowRadius, pGlowRadius, u, v);

        pPoseStack.popPose();
    }
}
