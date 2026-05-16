package com.nukateam.ntgl.client.util.helpers.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.nukateam.ntgl.common.util.data.Rgba;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import org.joml.Matrix3f;
import org.joml.Matrix4f;

public class RenderUtil {
    public static final float BEAM_ALPHA = 0.7F;

    public static void renderBeam(PoseStack poseStack, MultiBufferSource pBufferSource, ResourceLocation pBeamLocation,
                                  float pPartialTick, float pTextureScale, long gameTime, float pYOffset, float pHeight,
                                  Rgba colors, float pBeamRadius, float pGlowRadius) {
        var maxY = pYOffset + pHeight;
        float f = (float) Math.floorMod(gameTime, 40) + pPartialTick;
        float f1 = pHeight < 0 ? f : -f;
        float f2 = Mth.frac(f1 * 0.2F - (float) Mth.floor(f1 * 0.1F));
        var minX = -pGlowRadius;
        var maxX = -pGlowRadius;
        var minZ = -pGlowRadius;
        var maxZ = -pBeamRadius;
        var v = -1.0F + f2;
        var u = pHeight * pTextureScale * (BEAM_ALPHA / pBeamRadius) + v;

        poseStack.pushPose();
        {
            poseStack.pushPose();
            {
                var vertexConsumer = pBufferSource
                        .getBuffer(RenderType.beaconBeam(pBeamLocation, false));

                RenderUtil.renderPart(poseStack, vertexConsumer, colors.setAlpha(1.0F),
                        pYOffset, maxY,
                        0.0F, pBeamRadius,
                        pBeamRadius, 0.0F,
                        maxZ, 0.0F,
                        0.0F, -pBeamRadius,
                        u, v);
            }
            poseStack.popPose();

            maxZ = -pGlowRadius;
            v = -1.0F + f2;
            u = pHeight * pTextureScale + v;

            RenderUtil.renderPart(poseStack, pBufferSource.getBuffer(RenderType.beaconBeam(pBeamLocation, true)),
                    colors.setAlpha(BEAM_ALPHA), pYOffset, maxY, minX, maxX, pGlowRadius, minZ, maxZ,
                    pGlowRadius, pGlowRadius, pGlowRadius, u, v);
        }
        poseStack.popPose();
    }

    public static void renderPart(PoseStack poseStack, VertexConsumer consumer,
                                   Rgba colors,
                                   float pMinY, float pMaxY,
                                   float minX, float maxX,
                                   float minZ, float maxZ,
                                   float pX2, float pZ2,
                                   float pX3, float pZ3,
                                   float u, float v) {
        var pose = poseStack.last();

        renderQuad(pose, consumer, colors, pMinY, pMaxY, minX, maxX, minZ, maxZ, u, v);
        renderQuad(pose, consumer, colors, pMinY, pMaxY, pX3, pZ3, pX2, pZ2, u, v);
        renderQuad(pose, consumer, colors, pMinY, pMaxY, minZ, maxZ, pX3, pZ3, u, v);
        renderQuad(pose, consumer, colors, pMinY, pMaxY, pX2, pZ2, minX, maxX, u, v);
    }

    public static void renderQuad(PoseStack.Pose pose, VertexConsumer consumer, Rgba colors,
                                   float pMinY, float pMaxY,
                                   float pMinX, float pMinZ,
                                   float pMaxX, float pMaxZ,

                                   float pMinV, float pMaxV) {
        addVertex(pose, consumer, colors, pMaxY, pMinX, pMinZ, 1, pMinV);
        addVertex(pose, consumer, colors, pMinY, pMinX, pMinZ, 1, pMaxV);
        addVertex(pose, consumer, colors, pMinY, pMaxX, pMaxZ, 0, pMaxV);
        addVertex(pose, consumer, colors, pMaxY, pMaxX, pMaxZ, 0, pMinV);
    }

    public static void addVertex(PoseStack.Pose pose, VertexConsumer consumer,
                                 Rgba colors, float pY, float pX, float pZ, float pU, float pV) {
        float red = colors.r();
        float green = colors.g();
        float blue = colors.b();
        float alpha = colors.a();

        consumer.addVertex(pose, pX, pY, pZ)
                .setColor(red, green, blue, alpha)
                .setUv(pU, pV)
                .setOverlay(OverlayTexture.NO_OVERLAY)
                .setLight(LightTexture.FULL_BRIGHT)
                .setNormal(0.0F, 1.0F, 0.0F);
    }
}
