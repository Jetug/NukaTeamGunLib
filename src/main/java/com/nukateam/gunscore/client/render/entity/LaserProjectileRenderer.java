package com.nukateam.gunscore.client.render.entity;

import com.mojang.blaze3d.vertex.*;
import com.mojang.math.Matrix3f;
import com.mojang.math.Matrix4f;
import com.mojang.math.Vector3f;
import com.nukateam.gunscore.GunMod;
import com.nukateam.gunscore.common.data.util.Rgba;
import com.nukateam.gunscore.common.foundation.entity.LaserProjectile;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;

public class LaserProjectileRenderer extends EntityRenderer<LaserProjectile> {
    public static final float BEAM_ALPHA = 0.7F;
    public static ResourceLocation texture = new ResourceLocation(GunMod.MOD_ID, "textures/fx/laser.png");
    private final float laserRadius = 0.05F / 4;
    private final float laserGlowRadius = 0.055F / 4;
    private float laserWidth = 3.0f;
    private static final ResourceLocation GUARDIAN_LOCATION = new ResourceLocation("textures/entity/guardian.png");
    private static final ResourceLocation GUARDIAN_BEAM_LOCATION = new ResourceLocation("textures/entity/guardian_beam.png");
    private static final RenderType BEAM_RENDER_TYPE = RenderType.entityCutoutNoCull(texture);

    public LaserProjectileRenderer(EntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    public ResourceLocation getTextureLocation(LaserProjectile entity) {
        return texture;
    }

    private Vec3 getPosition(Entity pLivingEntity, double pYOffset, float pPartialTick) {
        double d0 = Mth.lerp(pPartialTick, pLivingEntity.xOld, pLivingEntity.getX());
        double d1 = Mth.lerp(pPartialTick, pLivingEntity.yOld, pLivingEntity.getY()) + pYOffset;
        double d2 = Mth.lerp(pPartialTick, pLivingEntity.zOld, pLivingEntity.getZ());
        return new Vec3(d0, d1, d2);
    }

    public void render2(LaserProjectile laser, float entityYaw, float partialTicks,
                       PoseStack poseStack2, MultiBufferSource bufferSource, int light) {
        float distance = (float) laser.distance;
        int maxTicks = laser.maxTicks;

        var prog = ((float) laser.tickCount) / ((float) maxTicks);
        var width = laserWidth * (Mth.sin(Mth.sqrt(prog) * Mth.PI)) * 2;
        var distance_start = (float)Math.min(1.0d, distance);
        var u = (distance / laserWidth) * 2.0f;

        var poseStack = new PoseStack();
        poseStack.translate(laser.getX(), laser.getY(), laser.getZ());

//        GlStateManager.rotate(laser.laserYaw - 90F, 0.0F, 1.0F, 0.0F);
//        GlStateManager.rotate(laser.laserPitch, 0.0F, 0.0F, 1.0F);
        poseStack.mulPose(Vector3f.YP.rotationDegrees(laser.laserYaw - 90F));
        poseStack.mulPose(Vector3f.ZP.rotationDegrees(laser.laserPitch));

//        Tessellator tessellator = Tessellator.getInstance();
//        BufferBuilder bufferbuilder = tessellator.getBuffer();

        var vertexConsumer = bufferSource.getBuffer(RenderType.beaconBeam(texture, false));
        float scale = 0.0125F;

        poseStack.pushPose();
        {
            distance *= 80.0D;
            distance_start *= 80.0D;

            float rot_x = 45f + (prog * 180f);

            poseStack.mulPose(Vector3f.XP.rotationDegrees(rot_x));
//            GlStateManager.rotate(rot_x + 90f, 1.0F, 0.0F, 0.0F);
            poseStack.scale(scale, scale, scale);

            float brightness = (float) Math.sin(Math.sqrt(prog) * Math.PI);

            var pose = poseStack.last();
            var matrix4f = pose.pose();
            var matrix3f = pose.normal();

            if (distance > distance_start) { //Beam Segment
                poseStack.pushPose();
                for (int i = 0; i < 2; ++i) {
//                    GlStateManager.rotate(90.0F, 1.0F, 0.0F, 0.0F);
                    poseStack.mulPose(Vector3f.XP.rotationDegrees(90f));
//                    GlStateManager.glNormal3f(0.0F, 0.0F, scale); //????
//                    bufferbuilder.begin(7, DefaultVertexFormats.POSITION_TEX_COLOR);

//                    pConsumer.vertex(pPose, pX, pY, pZ)
//                            .color(pRed, pGreen, pBlue, pAlpha)
//                            .uv(pU, pV)
//                            .overlayCoords(OverlayTexture.NO_OVERLAY)
//                            .uv2(15728880)
//                            .normal(matrix3f, 0.0F, 1.0F, 0.0F)
//                            .endVertex();

                    vertexConsumer.vertex(matrix4f, distance, -width, 0.0F)
                            .color(1.0f, 1.0f, 1.0f, brightness)
                            .uv(u + prog, 0)
                            .overlayCoords(OverlayTexture.NO_OVERLAY)
                            .uv2(15728880)
                            .normal(matrix3f, 0.0F, 1.0F, 0.0F)
                            .endVertex();
                    vertexConsumer.vertex(matrix4f, distance_start, -width, 0.0F)
                            .color(1.0f, 1.0f, 1.0f, brightness)
                            .uv(prog, 0)
                            .overlayCoords(OverlayTexture.NO_OVERLAY)
                            .uv2(15728880)
                            .normal(matrix3f, 0.0F, 1.0F, 0.0F)
                            .endVertex();
                    vertexConsumer.vertex(matrix4f, distance_start, width, 0.0F)
                            .color(1.0f, 1.0f, 1.0f, brightness)
                            .uv(prog, 1)
                            .overlayCoords(OverlayTexture.NO_OVERLAY)
                            .uv2(15728880)
                            .normal(matrix3f, 0.0F, 1.0F, 0.0F)
                            .endVertex();
                    vertexConsumer.vertex(matrix4f, distance, width, 0.0F)
                            .color(1.0f, 1.0f, 1.0f, brightness)
                            .uv(u + prog, 1).color(1.0f, 1.0f, 1.0f, brightness)
                            .overlayCoords(OverlayTexture.NO_OVERLAY)
                            .uv2(15728880)
                            .normal(matrix3f, 0.0F, 1.0F, 0.0F)
                            .endVertex();
                }
                poseStack.popPose();
            }

            poseStack.pushPose();
//            renderManager.renderEngine.bindTexture(textureStart);

//            for (int i = 0; i < 2; ++i) //Beam start segment
//            {
//                GlStateManager.rotate(90.0F, 1.0F, 0.0F, 0.0F);
//                GlStateManager.glNormal3f(0.0F, 0.0F, scale); //????
//
//                bufferbuilder.begin(7, FefaultVertexFormats.POSITION_TEX_COLOR);
//
//                bufferbuilder.pos(distance_start, -width, 0.0F).tex(1, 0).color(1.0f, 1.0f, 1.0f, brightness).endVertex();
//                bufferbuilder.pos(0, -width, 0.0F).tex(0, 0).color(1.0f, 1.0f, 1.0f, brightness).endVertex();
//                bufferbuilder.pos(0, width, 0.0F).tex(0, 1).color(1.0f, 1.0f, 1.0f, brightness).endVertex();
//                bufferbuilder.pos(distance_start, width, 0.0F).tex(1, 1).color(1.0f, 1.0f, 1.0f, brightness).endVertex();
//            }

            poseStack.popPose();
        }
        poseStack.popPose();
    }

    public void render(LaserProjectile laserProjectile, float entityYaw, float partialTicks,
                        PoseStack poseStack, MultiBufferSource bufferSource, int light) {
        var eyeHeight = laserProjectile.getEyeHeight();
        var shooterId = laserProjectile.getShooterId();
        var shooter = Minecraft.getInstance().level.getEntity(shooterId);

        float prog = ((float) laserProjectile.tickCount) / ((float) laserProjectile.getLife());

        float radius = (float) (laserRadius * (Math.sin(Math.sqrt(prog) * Math.PI)) * 2);
        float glowRadius = (float) (laserGlowRadius * (Math.sin(Math.sqrt(prog) * Math.PI)) * 2);

        if (shooter == null) return;

        poseStack.pushPose();
        {
            var playerPos = laserProjectile.endVec;
            var laserPos = laserProjectile.startVec;
            var pos = playerPos.subtract(laserPos);

            pos = pos.normalize();
            float yPos = (float) Math.acos(pos.y);
            float xzPos = (float) Math.atan2(pos.z, pos.x);

            var side = laserProjectile.isRightHand() ? -1 : 1;

            poseStack.mulPose(Vector3f.YP.rotationDegrees((((float) Math.PI / 2F) - xzPos) * (180F / (float) Math.PI)));
            poseStack.mulPose(Vector3f.XP.rotationDegrees(yPos * (180F / (float) Math.PI)));

            poseStack.translate(side * 0.25, 0, 0);
//            poseStack.mulPose(Vector3f.ZP.rotationDegrees(180F));

            long gameTime = laserProjectile.getLevel().getGameTime();
            int yOffset = 0; //(int) laserProjectile.position().y;
            var color = new Rgba(1, 1, 1, 1);

            renderBeam(poseStack, bufferSource, texture, partialTicks, 1.0F,
                    gameTime, (float) yOffset, (float) laserProjectile.distance, color, radius, glowRadius);
        }
        poseStack.popPose();
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

//        pPoseStack.mulPose(Vector3f.YP.rotationDegrees(f * 2.25F - 45.0F));
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
    public boolean shouldRender(LaserProjectile pLivingEntity, Frustum pCamera, double pCamX, double pCamY, double pCamZ) {
        return true;
    }
}
