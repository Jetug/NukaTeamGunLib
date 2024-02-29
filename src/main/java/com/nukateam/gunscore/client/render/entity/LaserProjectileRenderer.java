package com.nukateam.gunscore.client.render.entity;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import com.mojang.math.Matrix3f;
import com.mojang.math.Matrix4f;
import com.mojang.math.Vector3f;
import com.nukateam.gunscore.GunMod;
import com.nukateam.gunscore.common.foundation.entity.LaserProjectile;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.level.block.entity.BeaconBlockEntity;

public class LaserProjectileRenderer extends EntityRenderer<LaserProjectile> {
    public static ResourceLocation texture = new ResourceLocation(GunMod.MOD_ID, "textures/fx/laser.png");
    private final double laserWidth = 3.0;

    public LaserProjectileRenderer(EntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    public ResourceLocation getTextureLocation(LaserProjectile entity) {
        return texture;
    }

    @Override
    public void render(LaserProjectile entity, float entityYaw, float partialTicks,
                       PoseStack poseStack, MultiBufferSource renderTypeBuffer, int light) {
        Minecraft minecraft = Minecraft.getInstance();
        TextureManager textureManager = minecraft.getTextureManager();
        RenderSystem.setShaderColor(1.0F, 0.0F, 0.0F, 1.0F); // Устанавливаем цвет лазерного луча (например, красный)

        poseStack.pushPose();
        poseStack.translate(0.0D, -0.1D, 0.0D); // Смещаем лазерный луч немного вверх относительно центра сущности

        // Рисуем лазерный луч как прямую линию
        BufferBuilder bufferBuilder = Tesselator.getInstance().getBuilder();
        bufferBuilder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);
        bufferBuilder.vertex(-0.05D, 0.0D, 0.0D).color(255, 0, 0, 255).endVertex();
        bufferBuilder.vertex(0.05D, 0.0D, 0.0D).color(255, 0, 0, 255).endVertex();
        bufferBuilder.vertex(0.05D, 10.0D, 0.0D).color(255, 0, 0, 255).endVertex();
        bufferBuilder.vertex(-0.05D, 10.0D, 0.0D).color(255, 0, 0, 255).endVertex();
        bufferBuilder.end();

        BufferUploader.end(bufferBuilder);

        poseStack.popPose();
    }

    public void render(BeaconBlockEntity pBlockEntity, float pPartialTick, PoseStack pPoseStack,
                       MultiBufferSource pBufferSource, int pPackedLight, int pPackedOverlay) {
        long gameTime = pBlockEntity.getLevel().getGameTime();
        var list = pBlockEntity.getBeamSections();
        int height = 0;

        for(int id = 0; id < list.size(); ++id) {
            var section = list.get(id);
            renderBeaconBeam(pPoseStack, pBufferSource, pPartialTick, gameTime, height,
                    id == list.size() - 1 ? 1024 : section.getHeight(), section.getColor());
            height += section.getHeight();
        }
    }

    private static void renderBeaconBeam(PoseStack pPoseStack, MultiBufferSource pBufferSource,
                                         float pPartialTick, long pGameTime, int pYOffset, int pHeight, float[] pColors) {
        renderBeaconBeam(pPoseStack, pBufferSource, texture, pPartialTick, 1.0F,
                pGameTime, pYOffset, pHeight, pColors, 0.2F, 0.25F);
    }

    public static void renderBeaconBeam(PoseStack pPoseStack, MultiBufferSource pBufferSource, ResourceLocation pBeamLocation,
                                        float pPartialTick, float pTextureScale, long pGameTime, int pYOffset, int pHeight,
                                        float[] pColors, float pBeamRadius, float pGlowRadius) {
        int i = pYOffset + pHeight;
        pPoseStack.pushPose();
        pPoseStack.translate(0.5D, 0.0D, 0.5D);
        float f = (float)Math.floorMod(pGameTime, 40) + pPartialTick;
        float f1 = pHeight < 0 ? f : -f;
        float f2 = Mth.frac(f1 * 0.2F - (float)Mth.floor(f1 * 0.1F));
        float f3 = pColors[0];
        float f4 = pColors[1];
        float f5 = pColors[2];
        pPoseStack.pushPose();
        pPoseStack.mulPose(Vector3f.YP.rotationDegrees(f * 2.25F - 45.0F));
        float f6 = 0.0F;
        float f8 = 0.0F;
        float f9 = -pBeamRadius;
        float f10 = 0.0F;
        float f11 = 0.0F;
        float f12 = -pBeamRadius;
        float f13 = 0.0F;
        float f14 = 1.0F;
        float f15 = -1.0F + f2;
        float f16 = (float)pHeight * pTextureScale * (0.5F / pBeamRadius) + f15;
        renderPart(pPoseStack, pBufferSource.getBuffer(RenderType.beaconBeam(pBeamLocation, false)), f3, f4, f5, 1.0F, pYOffset, i, 0.0F, pBeamRadius, pBeamRadius, 0.0F, f9, 0.0F, 0.0F, f12, 0.0F, 1.0F, f16, f15);
        pPoseStack.popPose();
        f6 = -pGlowRadius;
        float f7 = -pGlowRadius;
        f8 = -pGlowRadius;
        f9 = -pGlowRadius;
        f13 = 0.0F;
        f14 = 1.0F;
        f15 = -1.0F + f2;
        f16 = (float)pHeight * pTextureScale + f15;
        renderPart(pPoseStack, pBufferSource.getBuffer(RenderType.beaconBeam(pBeamLocation, true)), f3, f4, f5, 0.125F, pYOffset, i, f6, f7, pGlowRadius, f8, f9, pGlowRadius, pGlowRadius, pGlowRadius, 0.0F, 1.0F, f16, f15);
        pPoseStack.popPose();
    }

    private static void renderPart(PoseStack pPoseStack, VertexConsumer pConsumer, float pRed, float pGreen, float pBlue, float pAlpha, int pMinY, int pMaxY, float pX0, float pZ0, float pX1, float pZ1, float pX2, float pZ2, float pX3, float pZ3, float pMinU, float pMaxU, float pMinV, float pMaxV) {
        PoseStack.Pose posestack$pose = pPoseStack.last();
        Matrix4f matrix4f = posestack$pose.pose();
        Matrix3f matrix3f = posestack$pose.normal();
        renderQuad(matrix4f, matrix3f, pConsumer, pRed, pGreen, pBlue, pAlpha, pMinY, pMaxY, pX0, pZ0, pX1, pZ1, pMinU, pMaxU, pMinV, pMaxV);
        renderQuad(matrix4f, matrix3f, pConsumer, pRed, pGreen, pBlue, pAlpha, pMinY, pMaxY, pX3, pZ3, pX2, pZ2, pMinU, pMaxU, pMinV, pMaxV);
        renderQuad(matrix4f, matrix3f, pConsumer, pRed, pGreen, pBlue, pAlpha, pMinY, pMaxY, pX1, pZ1, pX3, pZ3, pMinU, pMaxU, pMinV, pMaxV);
        renderQuad(matrix4f, matrix3f, pConsumer, pRed, pGreen, pBlue, pAlpha, pMinY, pMaxY, pX2, pZ2, pX0, pZ0, pMinU, pMaxU, pMinV, pMaxV);
    }

    private static void renderQuad(Matrix4f pPose, Matrix3f pNormal, VertexConsumer pConsumer, float pRed, float pGreen, float pBlue, float pAlpha, int pMinY, int pMaxY, float pMinX, float pMinZ, float pMaxX, float pMaxZ, float pMinU, float pMaxU, float pMinV, float pMaxV) {
        addVertex(pPose, pNormal, pConsumer, pRed, pGreen, pBlue, pAlpha, pMaxY, pMinX, pMinZ, pMaxU, pMinV);
        addVertex(pPose, pNormal, pConsumer, pRed, pGreen, pBlue, pAlpha, pMinY, pMinX, pMinZ, pMaxU, pMaxV);
        addVertex(pPose, pNormal, pConsumer, pRed, pGreen, pBlue, pAlpha, pMinY, pMaxX, pMaxZ, pMinU, pMaxV);
        addVertex(pPose, pNormal, pConsumer, pRed, pGreen, pBlue, pAlpha, pMaxY, pMaxX, pMaxZ, pMinU, pMinV);
    }

    private static void addVertex(Matrix4f pPose, Matrix3f pNormal, VertexConsumer pConsumer, float pRed, float pGreen, float pBlue, float pAlpha, int pY, float pX, float pZ, float pU, float pV) {
        pConsumer.vertex(pPose, pX, (float)pY, pZ).color(pRed, pGreen, pBlue, pAlpha).uv(pU, pV).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(15728880).normal(pNormal, 0.0F, 1.0F, 0.0F).endVertex();
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
////        bindTexture(texture); // Замените на соответствующий метод загрузки текстуры
//        Minecraft.getInstance().getTextureManager().bindForSetup(texture);
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
