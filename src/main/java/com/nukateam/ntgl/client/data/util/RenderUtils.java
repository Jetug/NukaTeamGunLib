package com.nukateam.ntgl.client.data.util;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.nukateam.ntgl.common.data.util.Rgba;
import net.minecraft.client.renderer.texture.OverlayTexture;
import org.joml.Matrix3f;
import org.joml.Matrix4f;

public class RenderUtils {
    public static void renderPart(PoseStack pPoseStack, VertexConsumer pConsumer,
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

    public static void renderQuad(Matrix4f pPose, Matrix3f pNormal, VertexConsumer pConsumer,
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

    public static void addVertex(Matrix4f pPose, Matrix3f pNormal, VertexConsumer pConsumer,
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
}
