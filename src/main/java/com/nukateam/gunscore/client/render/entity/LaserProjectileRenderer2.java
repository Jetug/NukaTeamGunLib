package com.nukateam.gunscore.client.render.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Matrix3f;
import com.mojang.math.Matrix4f;
import com.mojang.math.Vector3f;
import com.nukateam.gunscore.GunMod;
import com.nukateam.gunscore.common.data.util.Rgba;
import com.nukateam.gunscore.common.foundation.entity.LaserProjectile;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;

public class LaserProjectileRenderer2 extends EntityRenderer<LaserProjectile> {
    public static ResourceLocation texture = new ResourceLocation(GunMod.MOD_ID, "textures/fx/laser.png");
    private final double laserWidth = 3.0;

    public LaserProjectileRenderer2(EntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    public ResourceLocation getTextureLocation(LaserProjectile entity) {
        return texture;
    }

    @Override
    public void render(LaserProjectile entity, float entityYaw, float partialTicks,
                       PoseStack poseStack, MultiBufferSource bufferSource, int light) {

//        var poseStack = new PoseStack();
//        var player = Minecraft.getInstance().player;
//        poseStack.translate(player.getX(), player.getY(), player.getZ());

        poseStack.pushPose();
        {
            poseStack.mulPose(Vector3f.YP.rotationDegrees(180F));
            poseStack.mulPose(Vector3f.YP.rotationDegrees(entityYaw));
            poseStack.mulPose(Vector3f.XP.rotationDegrees(entity.getXRot() - 90));
            poseStack.translate(0, -1, 0);
            poseStack.translate(-100, 0, 0);

            long gameTime = entity.getLevel().getGameTime();
            int yOffset = 0; //(int) entity.position().y;
            var color = new Rgba(1, 1, 1, 1);

            renderBeam(poseStack, bufferSource, partialTicks, gameTime, yOffset, 100, color);
        }
        poseStack.popPose();
    }

    private static void renderBeam(PoseStack pPoseStack, MultiBufferSource pBufferSource,
                                   float pPartialTick, long pGameTime, int pYOffset, int pHeight, Rgba pColors) {
        renderBeam(pPoseStack, pBufferSource, texture, pPartialTick, 1.0F,
                pGameTime, pYOffset, pHeight, pColors, 0.2F, 0.25F);
    }

    public static void renderBeam(PoseStack pPoseStack, MultiBufferSource pBufferSource, ResourceLocation pBeamLocation,
                                  float pPartialTick, float pTextureScale, long pGameTime, int pYOffset, int pHeight,
                                  Rgba pColors, float pBeamRadius, float pGlowRadius) {
        int maxY = pYOffset + pHeight;
        pPoseStack.pushPose();

        pPoseStack.translate(0.5D, 0.0D, 0.5D);
        float f = (float)Math.floorMod(pGameTime, 40) + pPartialTick;
        float f1 = pHeight < 0 ? f : -f;
        float f2 = Mth.frac(f1 * 0.2F - (float)Mth.floor(f1 * 0.1F));
        pPoseStack.pushPose();

        pPoseStack.mulPose(Vector3f.YP.rotationDegrees(f * 2.25F - 45.0F));
        var minX = -pGlowRadius;
        var maxX = -pGlowRadius;
        var minZ = -pGlowRadius;
        var maxZ = -pBeamRadius;
        var f12 = -pBeamRadius;
        var v = -1.0F + f2;
        var u = (float)pHeight * pTextureScale * (0.5F / pBeamRadius) + v;

        var vertexConsumer = pBufferSource.getBuffer(RenderType.beaconBeam(pBeamLocation, false));

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
        u = (float)pHeight * pTextureScale + v;

        renderPart(pPoseStack, pBufferSource.getBuffer(RenderType.beaconBeam(pBeamLocation, true)),
                pColors.setAlpha(0.125F), pYOffset, maxY, minX, maxX, pGlowRadius, minZ, maxZ,
                pGlowRadius, pGlowRadius, pGlowRadius, u, v);

        pPoseStack.popPose();
    }

    private static void renderPart(PoseStack pPoseStack, VertexConsumer pConsumer,
                                   Rgba pColors,
                                   int pMinY, int pMaxY,
                                   float minX, float maxX,
                                   float minZ, float maxZ,
                                   float pX2, float pZ2,
                                   float pX3, float pZ3,
                                   float u, float v) {
        PoseStack.Pose posestack$pose = pPoseStack.last();
        Matrix4f matrix4f = posestack$pose.pose();
        Matrix3f matrix3f = posestack$pose.normal();

        float red   = pColors.r();
        float green = pColors.g();
        float blue  = pColors.b();
        float alpha = pColors.a();

        renderQuad(matrix4f, matrix3f, pConsumer, red, green, blue, alpha, pMinY, pMaxY, minX, maxX, minZ, maxZ, u, v);
        renderQuad(matrix4f, matrix3f, pConsumer, red, green, blue, alpha, pMinY, pMaxY, pX3, pZ3, pX2, pZ2, u, v);
        renderQuad(matrix4f, matrix3f, pConsumer, red, green, blue, alpha, pMinY, pMaxY, minZ, maxZ, pX3, pZ3, u, v);
        renderQuad(matrix4f, matrix3f, pConsumer, red, green, blue, alpha, pMinY, pMaxY, pX2, pZ2, minX, maxX, u, v);
    }

    private static void renderQuad(Matrix4f pPose, Matrix3f pNormal, VertexConsumer pConsumer,
                                   float pRed, float pGreen, float pBlue, float pAlpha,
                                   int pMinY, int pMaxY,
                                   float pMinX, float pMinZ,
                                   float pMaxX, float pMaxZ,

                                   float pMinV, float pMaxV) {
        addVertex(pPose, pNormal, pConsumer, pRed, pGreen, pBlue, pAlpha, pMaxY, pMinX, pMinZ, 1, pMinV);
        addVertex(pPose, pNormal, pConsumer, pRed, pGreen, pBlue, pAlpha, pMinY, pMinX, pMinZ, 1, pMaxV);
        addVertex(pPose, pNormal, pConsumer, pRed, pGreen, pBlue, pAlpha, pMinY, pMaxX, pMaxZ, 0, pMaxV);
        addVertex(pPose, pNormal, pConsumer, pRed, pGreen, pBlue, pAlpha, pMaxY, pMaxX, pMaxZ, 0, pMinV);
    }

    private static void addVertex(Matrix4f pPose, Matrix3f pNormal, VertexConsumer pConsumer,
                                  float pRed, float pGreen, float pBlue, float pAlpha, int pY,
                                  float pX, float pZ, float pU, float pV) {
        pConsumer.vertex(pPose, pX, (float)pY, pZ)
                .color(pRed, pGreen, pBlue, pAlpha).uv(pU, pV)
                .overlayCoords(OverlayTexture.NO_OVERLAY)
                .uv2(15728880)
                .normal(pNormal, 0.0F, 1.0F, 0.0F)
                .endVertex();
    }


//    @Override
//    public void render(LaserProjectile entity, float entityYaw, float partialTicks,
//                       PoseStack poseStack, MultiBufferSource renderTypeBuffer, int light) {
//        GunMod.LOGGER.warn("Laser render");
//
//        var distance = entity.distance;
//        var maxTicks = entity.getLife();
//        var prog = ((float)entity.tickCount) / ((float)maxTicks);
//        var width = laserWidth * (Math.sin(Math.sqrt(prog) * Math.PI)) * 2;
//        var distance_start = Math.min(1.0d, distance);
//        var u = (float)(distance / laserWidth) * 2.0f;
//
////        bindTexture(TESLA_TEXTURE); // Замените на соответствующий метод загрузки текстуры
//        Minecraft.getInstance().getTextureManager().bindForSetup(TESLA_TEXTURE);
//
//        poseStack.pushPose();
//        RenderSystem.enableBlend();
////        RenderSystem.enableRescaleNormal();
//        RenderSystem.disableCull();
//        RenderSystem.depthMask(false);
//
//        poseStack.translate(entity.getX(), entity.getY(), entity.getZ());
//        poseStack.mulPose(Vector3f.YP.rotationDegrees(entity.laserYaw - 90F));
//        poseStack.mulPose(Vector3f.ZP.rotationDegrees(entity.laserPitch));
//
//        var tesselator = Tesselator.getInstance();
//        var bufferbuilder = tesselator.getBuilder();
//        float f10 = 0.0125F;
//
//        distance *= 80.0D;
//        distance_start *= 80.0D;
//
//        float rot_x = 45f + (prog * 180f);
//
//        poseStack.mulPose(Vector3f.XP.rotationDegrees(rot_x + 90f));
//        poseStack.scale(f10, f10, f10);
//
//        float brightness = (float) Math.sin(Math.sqrt(prog) * Math.PI);
//
//        if (distance > distance_start) { // Beam Segment
//            poseStack.pushPose();
//            for (int i = 0; i < 2; ++i) {
//                poseStack.mulPose(Vector3f.XP.rotationDegrees(90.0F));
//                bufferbuilder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX_COLOR);
//                bufferbuilder.vertex(distance, -width, 0.0D).uv(u + prog, 0).color(255, 255, 255, (int) (brightness * 255)).endVertex();
//                bufferbuilder.vertex(distance_start, -width, 0.0D).uv(prog, 0).color(255, 255, 255, (int) (brightness * 255)).endVertex();
//                bufferbuilder.vertex(distance_start, width, 0.0D).uv(prog, 1).color(255, 255, 255, (int) (brightness * 255)).endVertex();
//                bufferbuilder.vertex(distance, width, 0.0D).uv(u + prog, 1).color(255, 255, 255, (int) (brightness * 255)).endVertex();
//
//                tesselator.end();
//            }
//            poseStack.popPose();
//        }
//
//        poseStack.pushPose();
//        for (int i = 0; i < 2; ++i) { // Beam start segment
//            poseStack.mulPose(Vector3f.XP.rotationDegrees(90.0F));
//            bufferbuilder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX_COLOR);
//
//            bufferbuilder.vertex(distance_start, -width, 0.0D).uv(1, 0).color(255, 255, 255, (int) (brightness * 255)).endVertex();
//            bufferbuilder.vertex(0, -width, 0.0D).uv(0, 0).color(255, 255, 255, (int) (brightness * 255)).endVertex();
//            bufferbuilder.vertex(0, width, 0.0D).uv(0, 1).color(255, 255, 255, (int) (brightness * 255)).endVertex();
//            bufferbuilder.vertex(distance_start, width, 0.0D).uv(1, 1).color(255, 255, 255, (int) (brightness * 255)).endVertex();
//
//            tesselator.end();
//        }
//        poseStack.popPose();
//
//        RenderSystem.depthMask(true);
//        RenderSystem.enableCull();
////        RenderSystem.disableRescaleNormal();
//        RenderSystem.disableBlend();
//
//        poseStack.popPose();
//    }
}
