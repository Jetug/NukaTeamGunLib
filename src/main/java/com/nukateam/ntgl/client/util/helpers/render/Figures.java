package com.nukateam.ntgl.client.util.helpers.render;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.util.Mth;

public class Figures {
    public static void drawBar(GuiGraphics graphics, int x, int y, int width, int height, float percent, int color){
//        var color = percent < 0.25 ? 0xFFFF5555 : 0xFFFFFFFF;
        var value = (int)(width * percent);
        drawFrame(graphics, x, y, width, height, color);
        graphics.fill(x, y, x + value, y + height, color);
    }

    public static void drawFrame(GuiGraphics graphics, int x, int y, int width, int height, int color) {
        graphics.fill(x, y, x + width, y + 1, color); //TOP
        graphics.fill(x, y + height, x + width, y + height + 1, color); //BOTTOM
        graphics.fill(x, y, x + 1, y + height, color); //LEFT
        graphics.fill(x + width - 1, y, x + width, y + height, color); //RIGHT
    }

    public static void drawLine(GuiGraphics graphics, int x, int y, int width, int height) {
        drawLine(graphics, x, y, x + width, y + height, 0xFFFFFFFF);
    }

    public static void drawLine(GuiGraphics graphics, int x, int y, int width, int height, int color) {
        graphics.fill(x, y, x + width, y + height, color);
    }

    public static void drawBorder(GuiGraphics guiGraphics, float centerX, float centerY,
                                  float radius, float startAngle, float sweepAngle,
                                  int color, float thickness) {

        if (radius <= 0 || sweepAngle <= 0 || thickness <= 0) return;

        float r = (color >> 24 & 255) / 255.0F;
        float g = (color >> 16 & 255) / 255.0F;
        float b = (color >> 8 & 255) / 255.0F;
        float a = (color & 255) / 255.0F;

        float startRad = (float) Math.toRadians(startAngle - 90);
        float sweepRad = (float) Math.toRadians(sweepAngle);

        int segments = Math.max(8, (int) (sweepAngle / 2));

        PoseStack poseStack = guiGraphics.pose();
        poseStack.pushPose();

        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.disableDepthTest();
        RenderSystem.setShader(GameRenderer::getPositionColorShader);

        Tesselator tesselator = Tesselator.getInstance();
        BufferBuilder buffer = tesselator.getBuilder();

        buffer.begin(VertexFormat.Mode.TRIANGLE_STRIP, DefaultVertexFormat.POSITION_COLOR);

        for (int i = 0; i <= segments; i++) {
            float angle = startRad + sweepRad * i / segments;
            float cos = Mth.cos(angle);
            float sin = Mth.sin(angle);

            // Внешняя точка (радиус)
            float outerX = centerX + cos * radius;
            float outerY = centerY + sin * radius;

            // Внутренняя точка (радиус - толщина)
            float innerRadius = Math.max(0, radius - thickness);
            float innerX = centerX + cos * innerRadius;
            float innerY = centerY + sin * innerRadius;

            // Добавляем две точки для полоски
            buffer.vertex(poseStack.last().pose(), outerX, outerY, 0)
                    .color(r, g, b, a)
                    .endVertex();
            buffer.vertex(poseStack.last().pose(), innerX, innerY, 0)
                    .color(r, g, b, a)
                    .endVertex();
        }

        BufferUploader.drawWithShader(buffer.end());

        buffer = tesselator.getBuilder();
        buffer.begin(VertexFormat.Mode.TRIANGLES, DefaultVertexFormat.POSITION_COLOR);

        float leftOuterX = centerX + Mth.cos(startRad) * radius;
        float leftOuterY = centerY + Mth.sin(startRad) * radius;
        float leftInnerX = centerX + Mth.cos(startRad) * (radius - thickness);
        float leftInnerY = centerY + Mth.sin(startRad) * (radius - thickness);

        buffer.vertex(poseStack.last().pose(), centerX, centerY, 0)
                .color(r, g, b, a)
                .endVertex();
        buffer.vertex(poseStack.last().pose(), leftOuterX, leftOuterY, 0)
                .color(r, g, b, a)
                .endVertex();
        buffer.vertex(poseStack.last().pose(), leftInnerX, leftInnerY, 0)
                .color(r, g, b, a)
                .endVertex();

        float rightOuterX = centerX + Mth.cos(startRad + sweepRad) * radius;
        float rightOuterY = centerY + Mth.sin(startRad + sweepRad) * radius;
        float rightInnerX = centerX + Mth.cos(startRad + sweepRad) * (radius - thickness);
        float rightInnerY = centerY + Mth.sin(startRad + sweepRad) * (radius - thickness);

        buffer.vertex(poseStack.last().pose(), centerX, centerY, 0)
                .color(r, g, b, a)
                .endVertex();
        buffer.vertex(poseStack.last().pose(), rightInnerX, rightInnerY, 0)
                .color(r, g, b, a)
                .endVertex();
        buffer.vertex(poseStack.last().pose(), rightOuterX, rightOuterY, 0)
                .color(r, g, b, a)
                .endVertex();

        BufferUploader.drawWithShader(buffer.end());

        RenderSystem.enableDepthTest();
        RenderSystem.disableBlend();
        poseStack.popPose();
    }

    public static void drawOutline(GuiGraphics guiGraphics, float centerX, float centerY,
                                   float radius, float startAngle, float sweepAngle,
                                   int color, float lineWidth) {

        if (radius <= 0 || sweepAngle <= 0) return;

        float r = (color >> 24 & 255) / 255.0F;
        float g = (color >> 16 & 255) / 255.0F;
        float b = (color >> 8 & 255) / 255.0F;
        float a = (color & 255) / 255.0F;

        float startRad = (float) Math.toRadians(startAngle  - 90);
        float sweepRad = (float) Math.toRadians(sweepAngle);

        int segments = Math.max(8, (int) (radius * Mth.PI / 4));

        PoseStack poseStack = guiGraphics.pose();
        poseStack.pushPose();

        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.disableDepthTest();
        RenderSystem.setShader(GameRenderer::getPositionColorShader);
        RenderSystem.lineWidth(lineWidth);

        Tesselator tesselator = Tesselator.getInstance();
        BufferBuilder buffer = tesselator.getBuilder();

        buffer.begin(VertexFormat.Mode.DEBUG_LINE_STRIP, DefaultVertexFormat.POSITION_COLOR);

        for (int i = 0; i <= segments; i++) {
            float angle = startRad + sweepRad * i / segments;
            float x = centerX + Mth.cos(angle) * radius;
            float y = centerY + Mth.sin(angle) * radius;

            buffer.vertex(poseStack.last().pose(), x, y, 0)
                    .color(r, g, b, a)
                    .endVertex();
        }

        BufferUploader.drawWithShader(buffer.end());

        buffer = tesselator.getBuilder();
        buffer.begin(VertexFormat.Mode.DEBUG_LINES, DefaultVertexFormat.POSITION_COLOR);

        float startX = centerX + Mth.cos(startRad) * radius;
        float startY = centerY + Mth.sin(startRad) * radius;
        buffer.vertex(poseStack.last().pose(), centerX, centerY, 0)
                .color(r, g, b, a)
                .endVertex();
        buffer.vertex(poseStack.last().pose(), startX, startY, 0)
                .color(r, g, b, a)
                .endVertex();

        float endX = centerX + Mth.cos(startRad + sweepRad) * radius;
        float endY = centerY + Mth.sin(startRad + sweepRad) * radius;
        buffer.vertex(poseStack.last().pose(), centerX, centerY, 0)
                .color(r, g, b, a)
                .endVertex();
        buffer.vertex(poseStack.last().pose(), endX, endY, 0)
                .color(r, g, b, a)
                .endVertex();

        BufferUploader.drawWithShader(buffer.end());

        RenderSystem.enableDepthTest();
        RenderSystem.disableBlend();
        poseStack.popPose();
    }

    public static void drawSegment(GuiGraphics guiGraphics, float centerX, float centerY,
                                   float radius, float startAngle, float sweepAngle, int color) {
        if (radius <= 0 || sweepAngle <= 0) return;

        float r = (color >> 24 & 255) / 255.0F;
        float g = (color >> 16 & 255) / 255.0F;
        float b = (color >> 8 & 255) / 255.0F;
        float a = (color & 255) / 255.0F;

        float startRad = (float) Math.toRadians(startAngle - 90);
        float sweepRad = (float) Math.toRadians(sweepAngle);

        int segments = Math.max(8, (int) (radius * Mth.PI / 4));

        PoseStack poseStack = guiGraphics.pose();
        poseStack.pushPose();

        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.disableCull();
        RenderSystem.disableDepthTest();
        RenderSystem.setShader(GameRenderer::getPositionColorShader);

        Tesselator tesselator = Tesselator.getInstance();
        BufferBuilder buffer = tesselator.getBuilder();
        buffer.begin(VertexFormat.Mode.TRIANGLE_FAN, DefaultVertexFormat.POSITION_COLOR);

        buffer.vertex(poseStack.last().pose(), centerX, centerY, 0)
                .color(r, g, b, a)
                .endVertex();

        for (int i = 0; i <= segments; i++) {
            float angle = startRad + sweepRad * i / segments;
            float x = centerX + Mth.cos(angle) * radius;
            float y = centerY + Mth.sin(angle) * radius;

            buffer.vertex(poseStack.last().pose(), x, y, 0)
                    .color(r, g, b, a)
                    .endVertex();
        }

        BufferUploader.drawWithShader(buffer.end());

        RenderSystem.enableDepthTest();
        RenderSystem.enableCull();
        RenderSystem.disableBlend();
        poseStack.popPose();
    }
}
