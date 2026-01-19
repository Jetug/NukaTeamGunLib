package com.nukateam.ntgl.modules.wheel;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import java.util.ArrayList;
import java.util.List;

public class ActionWheel {
    private static final int WHEEL_SIZE = 128;

    private final List<WheelAction> actions = new ArrayList<>();
    private boolean isVisible = false;
    private int selectedSegment = -1;
    private long openTime = 0;
    private final Minecraft minecraft;

    private double lastMouseX = -1;
    private double lastMouseY = -1;
    private double accumulatedDeltaX = 0;
    private double accumulatedDeltaY = 0;
    private static final double MOVEMENT_THRESHOLD = 2.0;
    private static final double MOVEMENT_DECAY = 0.7;

    public static class WheelAction {
        private ResourceLocation icon;
        private Component title = Component.literal("");
        private Runnable action = () -> {};
        private int color = 0xC6C6C6FF;

        public WheelAction() {}

        public ResourceLocation getIcon() { return icon; }
        public Component getTitle() { return title; }
        public Runnable getAction() { return action; }
        public int getColor() { return color; }


        public WheelAction setIcon(ResourceLocation icon) {
            this.icon = icon;
            return this;
        }

        public WheelAction setTitle(Component title) {
            this.title = title;
            return this;
        }

        public WheelAction setAction(Runnable action) {
            this.action = action;
            return this;
        }

        public WheelAction setColor(int color) {
            this.color = color;
            return this;
        }
    }

    public ActionWheel(Minecraft minecraft) {
        this.minecraft = minecraft;
    }

    public void show(List<WheelAction> actions) {
        this.actions.clear();
        this.actions.addAll(actions);
        this.isVisible = true;
        this.openTime = System.currentTimeMillis();
        this.selectedSegment = -1;
        this.lastMouseX = -1;
        this.lastMouseY = -1;
        this.accumulatedDeltaX = 0;
        this.accumulatedDeltaY = 0;
    }

    public void hide() {
        if (isVisible) {
            isVisible = false;
            if (selectedSegment >= 0 && selectedSegment < actions.size()) {
                actions.get(selectedSegment).getAction().run();
            }
        }
    }

    public void render(GuiGraphics guiGraphics) {
        if (!isVisible || actions.isEmpty()) return;

        int screenWidth = minecraft.getWindow().getGuiScaledWidth();
        int screenHeight = minecraft.getWindow().getGuiScaledHeight();
        int centerX = screenWidth / 2;
        int centerY = screenHeight / 2;

        var animationProgress = Math.min((System.currentTimeMillis() - openTime) / 200f, 1.0f);
        var scale = 0.5f + animationProgress * 0.5f;

        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();

        renderWheel(guiGraphics, scale, centerX, centerY);
        renderIconsAndText(guiGraphics, centerX, centerY, scale);

        RenderSystem.disableBlend();
        guiGraphics.flush();
    }

    private void renderWheel(GuiGraphics guiGraphics, float scale, int centerX, int centerY) {
        RenderSystem.setShaderColor(1,1,1,0.3f);
        var poseStack = guiGraphics.pose();
        poseStack.pushPose();
        {
            poseStack.scale(scale, scale, 1.0f);

            var count = actions.size();
            var anglePerSegment = 360.0f / count;

            for(var i = 0; i < count; i++){

            }
            var offset = 0;
            for(var action : actions){
                drawSegment(guiGraphics, centerX / scale, centerY / scale,
                        WHEEL_SIZE / 2f, offset, anglePerSegment, action.color);
                offset += anglePerSegment;
            }


//            drawSegment(guiGraphics, centerX / scale, centerY / scale,
//                    WHEEL_SIZE / 2f, 0, 360, 0xC6C6C6FF);
            renderSelectedSegment(guiGraphics, centerX, centerY, scale);
        }
        poseStack.popPose();
        RenderSystem.setShaderColor(1,1,1,1);
    }

    private void renderSelectedSegment(GuiGraphics guiGraphics, int centerX, int centerY, float scale) {
        var count = actions.size();
        if (count == 0 || selectedSegment < 0) return;

        var anglePerSegment = 360.0f / count;
        var startAngle = selectedSegment * anglePerSegment;

        drawSegment(guiGraphics, centerX / scale, centerY / scale,
                WHEEL_SIZE / 2f, startAngle, anglePerSegment, 0xFFFFFFFF);
    }

    public static void drawSegment(GuiGraphics guiGraphics, float centerX, float centerY,
                                   float radius, float startAngle, float sweepAngle, int color) {

        if (radius <= 0 || sweepAngle <= 0) return;

        float r = (color >> 24 & 255) / 255.0F;
        float g = (color >> 16 & 255) / 255.0F;
        float b = (color >> 8 & 255) / 255.0F;
        float a = (color & 255) / 255.0F;

        float startRad = (float) Math.toRadians(startAngle - 90); // -90 для начала с 12 часов
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

    private void renderIconsAndText(GuiGraphics guiGraphics, int centerX, int centerY, float scale) {
        int count = actions.size();
        if (count == 0) return;

        float anglePerSegment = 360.0f / count;
        int radius = 40;

        for (int i = 0; i < count; i++) {
            float angle = (float) Math.toRadians(i * anglePerSegment - 90 + (anglePerSegment / 2));
            int x = centerX + (int) (Math.cos(angle) * radius * scale) - 8;
            int y = centerY + (int) (Math.sin(angle) * radius * scale) - 8;

            var action = actions.get(i);
            var icon = action.getIcon();

            if (i == selectedSegment) {
                guiGraphics.pose().pushPose();
                float iconScale = 1.2f;
                guiGraphics.pose().translate(x + 8, y + 8, 100);
                guiGraphics.pose().scale(iconScale, iconScale, 1.0f);
                guiGraphics.pose().translate(-8, -8, 0);

                guiGraphics.blit(icon,0,0,0,0, 16, 16, 16, 16);
//                guiGraphics.renderItem(icon, 0, 0);
//                guiGraphics.renderItemDecorations(minecraft.font, icon, 0, 0);
                guiGraphics.pose().popPose();
            } else {
                guiGraphics.blit(icon,x, y,0,0, 16, 16, 16, 16);
//                guiGraphics.renderItem(icon, x, y);
//                guiGraphics.renderItemDecorations(minecraft.font, icon, x, y);
            }

            if (i == selectedSegment) {
                Component title = action.getTitle();
                int color = action.getColor();

                guiGraphics.drawCenteredString(
                        minecraft.font,
                        title,
                        centerX,
                        centerY - 75,
                        color
                );
            }
        }
    }

    public void updateMousePosition(double mouseX, double mouseY) {
        if (!isVisible || actions.isEmpty()) {
            selectedSegment = -1;
            return;
        }

        if (lastMouseX == -1 || lastMouseY == -1) {
            lastMouseX = mouseX;
            lastMouseY = mouseY;
            return;
        }

        double deltaX = mouseX - lastMouseX;
        double deltaY = mouseY - lastMouseY;

        lastMouseX = mouseX;
        lastMouseY = mouseY;

        double movement = Math.sqrt(deltaX * deltaX + deltaY * deltaY);
        if (movement < MOVEMENT_THRESHOLD) {
            accumulatedDeltaX *= MOVEMENT_DECAY;
            accumulatedDeltaY *= MOVEMENT_DECAY;

            if (Math.abs(accumulatedDeltaX) < 0.1 && Math.abs(accumulatedDeltaY) < 0.1) {
                return;
            }
        } else {
            accumulatedDeltaX += deltaX;
            accumulatedDeltaY += deltaY;
        }

        double angle = getMovementAngle(accumulatedDeltaX, accumulatedDeltaY);

        int count = actions.size();
        float anglePerSegment = 360.0f / count;

        angle = (angle + 360) % 360;

        selectedSegment = (int) (angle / anglePerSegment);

        if (selectedSegment >= count) {
            selectedSegment = count - 1;
        }
    }

    private double getMovementAngle(double deltaX, double deltaY) {
        double movement = Math.sqrt(deltaX * deltaX + deltaY * deltaY);
        if (movement < 0.1) {
            return -1; // Не выбирать сегмент
        }

        double angleRad = Math.atan2(deltaY, deltaX);
        double angleDeg = Math.toDegrees(angleRad);

        angleDeg += 90;

        if (angleDeg < 0) angleDeg += 360;

        return angleDeg;
    }

    public boolean isVisible() {
        return isVisible;
    }

    public int getSelectedSegment() {
        return selectedSegment;
    }
}