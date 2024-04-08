package com.nukateam.ntgl.client.render.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Matrix3f;

import com.mojang.math.Axis;
import com.nukateam.ntgl.Ntgl;
import com.nukateam.ntgl.common.data.util.Rgba;
import com.nukateam.ntgl.common.foundation.entity.TeslaProjectile;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;

import java.util.concurrent.ThreadLocalRandom;

public class TeslaProjectileRenderer extends EntityRenderer<TeslaProjectile> {
    public static final float BEAM_ALPHA = 0.7F;
    public static ResourceLocation texture = new ResourceLocation(Ntgl.MOD_ID, "textures/fx/tesla.png");
    private final float laserRadius = 0.05F / 5;
    private final float laserGlowRadius = 0.055F / 5;
    private float laserWidth = 3.0f;

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

    public void render(TeslaProjectile projectile, float entityYaw, float partialTicks,
                        PoseStack poseStack, MultiBufferSource bufferSource, int light) {
        renderLightning(projectile, partialTicks, poseStack, bufferSource);
        renderLightning2(projectile, partialTicks, poseStack, bufferSource);
    }

    private void renderLightning(TeslaProjectile projectile, float partialTicks, PoseStack poseStack, MultiBufferSource bufferSource) {
        var shooterId = projectile.getShooterId();
        var shooter = Minecraft.getInstance().level.getEntity(shooterId);

        float prog = ((float) projectile.tickCount) / ((float) projectile.getLife());

        float radius = (float) (laserRadius * (Math.sin(Math.sqrt(prog) * Math.PI)) * 2);
        float glowRadius = (float) (laserGlowRadius * (Math.sin(Math.sqrt(prog) * Math.PI)) * 2);

        if (shooter == null) return;

        double distance = projectile.distance;
        int count = (int) Math.round(distance / offset);
//        offset = (distance / (double) count);

        var playerPos = projectile.endVec;
        var laserPos = projectile.startVec;
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

                poseStack.mulPose(Axis.XP.rotationDegrees(angleX * flag));
//                poseStack.mulPose(Axis.YP.rotationDegrees(angleZ * flag));
//                if(flag > 0){
//                    poseStack.mulPose(Axis.XP.rotationDegrees(angleX * flag));
//                }

                if(angleX < 0) offsetZ = -offsetZ;

                poseStack.translate(0, 0, offsetZ / 2 );

                var gameTime = projectile.getLevel().getGameTime();
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

    private void renderLightning2(TeslaProjectile projectile, float partialTicks, PoseStack poseStack, MultiBufferSource bufferSource) {
        var shooterId = projectile.getShooterId();
        var shooter = Minecraft.getInstance().level.getEntity(shooterId);

        float prog = ((float) projectile.tickCount) / ((float) projectile.getLife());

        float radius = (float) (laserRadius * (Math.sin(Math.sqrt(prog) * Math.PI)) * 2);
        float glowRadius = (float) (laserGlowRadius * (Math.sin(Math.sqrt(prog) * Math.PI)) * 2);

        if (shooter == null) return;

        double distance = projectile.distance;
        int count = (int) Math.round(distance / offset);
//        offset = (distance / (double) count);

        var playerPos = projectile.endVec;
        var laserPos = projectile.startVec;
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

                var gameTime = projectile.getLevel().getGameTime();
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

        renderPart(pPoseStack, vertexConsumer, pColors.setAlpha(1.0F),
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

        renderPart(pPoseStack, pBufferSource.getBuffer(RenderType.beaconBeam(pBeamLocation, true)),
                pColors.setAlpha(BEAM_ALPHA), pYOffset, maxY, minX, maxX, pGlowRadius, minZ, maxZ,
                pGlowRadius, pGlowRadius, pGlowRadius, u, v);

        pPoseStack.popPose();
    }

    private static void renderPart(PoseStack pPoseStack, VertexConsumer pConsumer,
                                   Rgba pColors,
                                   float pMinY, float pMaxY,
                                   float minX, float maxX,
                                   float minZ, float maxZ,
                                   float pX2, float pZ2,
                                   float pX3, float pZ3,
                                   float u, float v) {
        var pose = pPoseStack.last();
        var matrix4f = pose.pose();
        var matrix3f = pose.normal();

        float red = pColors.r();
        float green = pColors.g();
        float blue = pColors.b();
        float alpha = pColors.a();

        renderQuad(matrix4f, matrix3f, pConsumer, red, green, blue, alpha, pMinY, pMaxY, minX, maxX, minZ, maxZ, u, v);
        renderQuad(matrix4f, matrix3f, pConsumer, red, green, blue, alpha, pMinY, pMaxY, pX3, pZ3, pX2, pZ2, u, v);
        renderQuad(matrix4f, matrix3f, pConsumer, red, green, blue, alpha, pMinY, pMaxY, minZ, maxZ, pX3, pZ3, u, v);
        renderQuad(matrix4f, matrix3f, pConsumer, red, green, blue, alpha, pMinY, pMaxY, pX2, pZ2, minX, maxX, u, v);
    }

    private static void renderQuad(Matrix4f pPose, Matrix3f pNormal, VertexConsumer pConsumer,
                                   float pRed, float pGreen, float pBlue, float pAlpha,
                                   float pMinY, float pMaxY,
                                   float pMinX, float pMinZ,
                                   float pMaxX, float pMaxZ,

                                   float pMinV, float pMaxV) {
        addVertex(pPose, pNormal, pConsumer, pRed, pGreen, pBlue, pAlpha, pMaxY, pMinX, pMinZ, 1, pMinV);
        addVertex(pPose, pNormal, pConsumer, pRed, pGreen, pBlue, pAlpha, pMinY, pMinX, pMinZ, 1, pMaxV);
        addVertex(pPose, pNormal, pConsumer, pRed, pGreen, pBlue, pAlpha, pMinY, pMaxX, pMaxZ, 0, pMaxV);
        addVertex(pPose, pNormal, pConsumer, pRed, pGreen, pBlue, pAlpha, pMaxY, pMaxX, pMaxZ, 0, pMinV);
    }

    private static void addVertex(Matrix4f pPose, Matrix3f pNormal, VertexConsumer pConsumer,
                                  float pRed, float pGreen, float pBlue, float pAlpha, float pY,
                                  float pX, float pZ, float pU, float pV) {
        pConsumer.vertex(pPose, pX, pY, pZ)
                .color(pRed, pGreen, pBlue, pAlpha)
                .uv(pU, pV)
                .overlayCoords(OverlayTexture.NO_OVERLAY)
                .uv2(15728880)
                .normal(pNormal, 0.0F, 1.0F, 0.0F)
                .endVertex();
    }

    @Override
    public boolean shouldRender(TeslaProjectile pLivingEntity, Frustum pCamera, double pCamX, double pCamY, double pCamZ) {
        return true;
    }
}
