package com.nukateam.ntgl.client.render.renderers.projectiles;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import com.nukateam.ntgl.Ntgl;
import com.nukateam.ntgl.client.data.util.RenderUtils;
import com.nukateam.ntgl.common.data.util.Rgba;
import com.nukateam.ntgl.common.foundation.entity.TeslaProjectile;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import org.joml.Matrix3f;
import org.joml.Matrix4f;

import java.util.concurrent.ThreadLocalRandom;

public class TeslaProjectileRenderer extends EntityRenderer<TeslaProjectile> {
    public static final float BEAM_ALPHA = 0.7F;
    public static ResourceLocation texture = new ResourceLocation(Ntgl.MOD_ID, "textures/fx/tesla.png");
    private final float laserRadius = 0.05F / 5;
    private final float laserGlowRadius = 0.055F / 5;
    private static final int MIN_ANGLE = -45;
    private static final int MAX_ANGLE = 45;

    static final double offset = 0.50; // Distance per bolt vertex

    public TeslaProjectileRenderer(EntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    public ResourceLocation getTextureLocation(TeslaProjectile entity) {
        return texture;
    }

    @Override
    public boolean shouldRender(TeslaProjectile pLivingEntity, Frustum pCamera, double pCamX, double pCamY, double pCamZ) {
        return true;
    }

    public void render(TeslaProjectile projectile, float entityYaw, float partialTicks,
                        PoseStack poseStack, MultiBufferSource bufferSource, int light) {
        renderLightning(projectile, partialTicks, poseStack, bufferSource);
        renderLightning2(projectile, partialTicks, poseStack, bufferSource);
    }

    private void renderLightning(TeslaProjectile projectile, float partialTicks, PoseStack poseStack, MultiBufferSource bufferSource) {
        var prog = ((float) projectile.tickCount) / ((float) projectile.getLife());
        var fadingValue = Math.sin(Math.sqrt(prog) * Math.PI);
        var radius = (float) (laserRadius * fadingValue * 2);
        var glowRadius = (float) (laserGlowRadius * fadingValue * 2);
        var distance = projectile.getDistance();
        var count = (int) Math.round(distance / offset);
        var playerPos = projectile.getEndVec();
        var laserPos = projectile.getStartVec();
        var pos = playerPos.subtract(laserPos);

        poseStack.pushPose();
        {
            pos = pos.normalize();
            var yPos = (float) Math.acos(pos.y);
            var xzPos = (float) Math.atan2(pos.z, pos.x);
            var side = projectile.isRightHand() ? -1 : 1;

            poseStack.mulPose(Axis.YP.rotationDegrees((((float) Math.PI / 2F) - xzPos) * (180F / (float) Math.PI)));
            poseStack.mulPose(Axis.XP.rotationDegrees(yPos * (180F / (float) Math.PI)));
            poseStack.translate(side * 0.25, 0, 0);

            var angleX = projectile.angle;
            var flag = 1;
            var length = distance / count;

            for (int i = 0; i <= count; i++) {
            poseStack.pushPose();
                if(flag > 0) angleX = getRandomAngle();

                var radiansX = (angleX * (Math.PI)) / 180;
                var offsetZ = length * Math.sin(Math.abs(radiansX)) * -flag;
                var offsetY = length * Math.cos(Math.abs(radiansX));

                poseStack.mulPose(Axis.XP.rotationDegrees(angleX * flag));

                if(angleX < 0) offsetZ = -offsetZ;

                poseStack.translate(0, 0, offsetZ / 2 );

                var gameTime = projectile.level().getGameTime();
                var yOffset = 0; //(int) projectile.position().y;
                var color = new Rgba(1, 1, 1, 1);

                renderBeam(poseStack, bufferSource, texture, partialTicks, 1.0F,
                        gameTime, (float)yOffset - 0.1f, (float)(length + 0.1), color, radius, glowRadius);

            poseStack.popPose();
                poseStack.translate(0, offsetY, 0);
                flag = -flag;
            }
        }
        poseStack.popPose();
    }

    private void renderLightning2(TeslaProjectile projectile, float partialTicks, PoseStack poseStack, MultiBufferSource bufferSource) {
        float prog = ((float) projectile.tickCount) / ((float) projectile.getLife());
        float radius = (float) (laserRadius * (Math.sin(Math.sqrt(prog) * Math.PI)) * 2);
        float glowRadius = (float) (laserGlowRadius * (Math.sin(Math.sqrt(prog) * Math.PI)) * 2);

//        if (shooter == null) return;

        var distance = projectile.getDistance();
        var count = (int) Math.round(distance / offset);
//        offset = (distance / (double) count);

        var playerPos = projectile.getStartVec();
        var laserPos = projectile.getEndVec();
//        var shooter = Minecraft.getInstance().level.getEntity(projectile.getShooterId());
//        var laserPos = shooter.position();
        var pos = playerPos.subtract(laserPos);

        poseStack.pushPose();
        {
//            poseStack.mulPose(Axis.XP.rotationDegrees(projectile.angle));
            pos = pos.normalize();
            float yPos = (float) Math.acos(pos.y);
            float xzPos = (float) Math.atan2(pos.z, pos.x);

            var side = projectile.isRightHand() ? -1 : 1;

            poseStack.mulPose(Axis.YP.rotationDegrees((((float) Math.PI / 2F) - xzPos) * (180F / (float) Math.PI)));
            poseStack.mulPose(Axis.XP.rotationDegrees(yPos * (180F / (float) Math.PI)));
            poseStack.translate(side * 0.25, 0, 0);

//            var angleX = getRandomAngle();
//            var angleZ = getRandomAngle();
//            var angleX = 30f;
//            var angleZ = 30f;
            var angleX = projectile.angle;
            var flag = 1;

            var length = distance / count;
            var totalOffset = 0d;

            for (int i = 0; i <= count; i++) {
            poseStack.pushPose();
                if(flag > 0) {
                    angleX = getRandomAngle();
                }

                var radiansX = (angleX * (Math.PI)) / 180;
                var offsetZ = length * Math.sin(Math.abs(radiansX)) * -flag;
                var offsetY = length * Math.cos(Math.abs(radiansX));
//                var offsetY = Math.sqrt(length * length - offsetZ * offsetZ);

                poseStack.mulPose(Axis.ZP.rotationDegrees(angleX * flag));
//                poseStack.mulPose(Axis.YP.rotationDegrees(angleZ * flag));
//                if(flag > 0){
//                    poseStack.mulPose(Axis.XP.rotationDegrees(angleX * flag));
//                }

                if(angleX > 0) offsetZ = -offsetZ;

                poseStack.translate(offsetZ / 2, 0,  0);

                var gameTime = projectile.level().getGameTime();
                var yOffset = 0; //(int) projectile.position().y;
                var color = new Rgba(1, 1, 1, 1);

                totalOffset = offsetY;
//                poseStack.translate(0, totalOffset, 0);
//                poseStack.translate(0, offsetY, 0);

                renderBeam(poseStack, bufferSource, texture, partialTicks, 1.0F,
                        gameTime, (float)yOffset - 0.1f, (float)(length + 0.1), color, radius, glowRadius);

            poseStack.popPose();
                poseStack.translate(0, offsetY, 0);
                flag = -flag;
            }
        }
        poseStack.popPose();
    }

    public static int getRandomAngle(){
        return ThreadLocalRandom.current().nextInt(MIN_ANGLE, MAX_ANGLE + 1);
    }

    public static void renderBeam(PoseStack pPoseStack, MultiBufferSource pBufferSource, ResourceLocation pBeamLocation,
                                  float pPartialTick, float pTextureScale, long pGameTime, float pYOffset, float pHeight,
                                  Rgba pColors, float pBeamRadius, float pGlowRadius) {
        var maxY = pYOffset + pHeight;
        pPoseStack.pushPose();

        //jet
//        pPoseStack.translate(0.5D, 0.0D, 0.5D);
        float f = (float) Math.floorMod(pGameTime, 40) + pPartialTick;
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
