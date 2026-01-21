package com.nukateam.ntgl.client.render.hud;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import com.nukateam.ntgl.client.util.handler.AimingHandler;
import com.nukateam.ntgl.common.data.holders.AttachmentType;
import com.nukateam.ntgl.common.util.util.WeaponStateHelper;
import com.nukateam.ntgl.modules.wheel.ActionWheel;
import com.nukateam.ntgl.modules.wheel.ActionWheelManager;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraftforge.client.gui.overlay.ForgeGui;
import net.minecraftforge.client.gui.overlay.IGuiOverlay;

public class ActionWheelHud implements IGuiOverlay {
    public static final IGuiOverlay HUD = new ActionWheelHud();
    private static final int WHEEL_SIZE = 128;

    @Override
    public void render(ForgeGui gui, GuiGraphics graphics, float partialTick, int width, int height) {
        var minecraft = Minecraft.getInstance();
        var player = minecraft.player;
        if (player == null) return;
        var wheel = ActionWheelManager.getInstance().getWheel();

        if (wheel.isVisible()) {
            var mouseX = minecraft.mouseHandler.xpos();
            var mouseY = minecraft.mouseHandler.ypos();

            wheel.updateMousePosition(mouseX, mouseY);
            render(graphics, wheel);
        }
    }

    public void render(GuiGraphics guiGraphics, ActionWheel wheel) {
        if (!wheel.isVisible() || wheel.getActions().isEmpty()) return;
        var minecraft = Minecraft.getInstance();

        int screenWidth = minecraft.getWindow().getGuiScaledWidth();
        int screenHeight = minecraft.getWindow().getGuiScaledHeight();
        int centerX = screenWidth / 2;
        int centerY = screenHeight / 2;

        var animationProgress = Math.min((System.currentTimeMillis() - wheel.getOpenTime()) / 200f, 1.0f);
        var scale = 0.5f + animationProgress * 0.5f;

        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();

        renderWheel(wheel, guiGraphics, scale, centerX, centerY);
        renderIconsAndText(wheel, guiGraphics, centerX, centerY, scale);

        RenderSystem.disableBlend();
        guiGraphics.flush();
    }

    private void renderWheel(ActionWheel wheel, GuiGraphics guiGraphics, float scale, int centerX, int centerY) {
        RenderSystem.setShaderColor(1,1,1,0.3f);
        var poseStack = guiGraphics.pose();
        poseStack.pushPose();
        {
            poseStack.scale(scale, scale, 1.0f);

            var count = wheel.getActions().size();
            var anglePerSegment = 360.0f / count;

            for(var i = 0; i < count; i++){

            }
            var offset = 0;
            for(var action : wheel.getActions()){
                drawSegment(guiGraphics, centerX / scale, centerY / scale,
                        WHEEL_SIZE / 2f, offset, anglePerSegment, action.getColor());

                if(wheel.getActions().size() > 1) {
                    drawOutline(guiGraphics, centerX / scale, centerY / scale,
                            WHEEL_SIZE / 2f, offset, anglePerSegment, 0xFFFFFFFF, 8);
                }
                offset += anglePerSegment;
            }
            renderSelectedSegment(wheel, guiGraphics, centerX, centerY, scale);
        }
        poseStack.popPose();
        RenderSystem.setShaderColor(1,1,1,1);
    }

    private void renderSelectedSegment(ActionWheel wheel, GuiGraphics guiGraphics, int centerX, int centerY, float scale) {
        var count = wheel.getActions().size();
        if (count == 0 || wheel.getSelectedSegment() < 0) return;

        var anglePerSegment = 360.0f / count;
        var startAngle = wheel.getSelectedSegment() * anglePerSegment;

        drawSegment(guiGraphics, centerX / scale, centerY / scale,
                WHEEL_SIZE / 2f, startAngle, anglePerSegment, 0xFFFFFFFF);
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

    private void renderIconsAndText(ActionWheel wheel, GuiGraphics guiGraphics, int centerX, int centerY, float scale) {
        int count = wheel.getActions().size();
        if (count == 0) return;

        float anglePerSegment = 360.0f / count;
        int radius = 40;

        for (int i = 0; i < count; i++) {
            float angle = (float) Math.toRadians(i * anglePerSegment - 90 + (anglePerSegment / 2));
            int x = centerX + (int) (Math.cos(angle) * radius * scale) - 8;
            int y = centerY + (int) (Math.sin(angle) * radius * scale) - 8;

            var action = wheel.getActions().get(i);
            var icon = action.getIcon();

            if (i == wheel.getSelectedSegment()) {
                guiGraphics.pose().pushPose();
                float iconScale = 1.2f;
                guiGraphics.pose().translate(x + 8, y + 8, 100);
                guiGraphics.pose().scale(iconScale, iconScale, 1.0f);
                guiGraphics.pose().translate(-8, -8, 0);

                guiGraphics.blit(icon,0,0,0,0, 16, 16, 16, 16);
                guiGraphics.pose().popPose();
            } else {
                guiGraphics.blit(icon,x, y,0,0, 16, 16, 16, 16);
            }

            if (i == wheel.getSelectedSegment()) {
                Component title = action.getTitle();
                int color = action.getColor();

                guiGraphics.drawCenteredString(
                        Minecraft.getInstance().font,
                        title,
                        centerX,
                        centerY - 75,
                        0xFFFFFFFF
                );
            }
        }
    }
}
