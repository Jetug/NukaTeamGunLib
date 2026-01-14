package com.nukateam.ntgl.modules.wheel;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import com.nukateam.ntgl.Ntgl;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.joml.Matrix4f;

import java.util.ArrayList;
import java.util.List;

public class ActionWheel {
    private static final int WHEEL_SIZE = 128;

    private final List<WheelAction> actions = new ArrayList<>();
    private boolean isVisible = false;
    private int selectedSegment = -1;
    private long openTime = 0;
    private final Minecraft minecraft;

    public static class WheelAction {
        private final ItemStack icon;
        private final Component title;
        private final Runnable action;
        private final int color;

        public WheelAction(ItemStack icon, Component title, Runnable action, int color) {
            this.icon = icon;
            this.title = title;
            this.action = action;
            this.color = color;
        }

        public WheelAction(ItemStack icon, Component title, Runnable action) {
            this(icon, title, action, 0xFFFFFFFF);
        }

        public ItemStack getIcon() { return icon; }
        public Component getTitle() { return title; }
        public Runnable getAction() { return action; }
        public int getColor() { return color; }
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
            drawSegment(guiGraphics, centerX / scale, centerY / scale,
                    WHEEL_SIZE / 2f, 0, 360, 0xC6C6C6FF);
            renderSelectedSegment(guiGraphics, centerX, centerY, scale);
        }
        poseStack.popPose();
        RenderSystem.setShaderColor(1,1,1,1);
    }

    private void renderSelectedSegment(GuiGraphics guiGraphics, int centerX, int centerY, float scale) {
        var count = actions.size();
        var anglePerSegment = 360.0f / count;
        var startAngle = selectedSegment * anglePerSegment;

        drawSegment(guiGraphics, centerX / scale, centerY / scale,
                WHEEL_SIZE / 2f, startAngle, anglePerSegment, 0xFFFFFFFF);
    }

    public static void drawSegment(GuiGraphics guiGraphics, float centerX, float centerY,
                                   float radius, float startAngle, float sweepAngle, int color) {

        if (radius <= 0 || sweepAngle <= 0) return;

        // Разбираем цвет
        float a = (color >> 24 & 255) / 255.0F;
        float r = (color >> 16 & 255) / 255.0F;
        float g = (color >> 8 & 255) / 255.0F;
        float b = (color & 255) / 255.0F;

        // Конвертируем углы
        float startRad = (float) Math.toRadians(startAngle - 90); // -90 для начала с 12 часов
        float sweepRad = (float) Math.toRadians(sweepAngle);

        // Количество сегментов
        int segments = Math.max(8, (int) (radius * Mth.PI / 4));

        // Получаем PoseStack
        PoseStack poseStack = guiGraphics.pose();
        poseStack.pushPose();

        // Настраиваем рендер
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.disableCull();
        RenderSystem.disableDepthTest();
        RenderSystem.setShader(GameRenderer::getPositionColorShader);

        // Начинаем рисовать
        Tesselator tesselator = Tesselator.getInstance();
        BufferBuilder buffer = tesselator.getBuilder();
        buffer.begin(VertexFormat.Mode.TRIANGLE_FAN, DefaultVertexFormat.POSITION_COLOR);

        // Центральная точка
        buffer.vertex(poseStack.last().pose(), centerX, centerY, 0)
                .color(r, g, b, a)
                .endVertex();

        // Добавляем точки по окружности
        for (int i = 0; i <= segments; i++) {
            float angle = startRad + sweepRad * i / segments;
            float x = centerX + Mth.cos(angle) * radius;
            float y = centerY + Mth.sin(angle) * radius;

            buffer.vertex(poseStack.last().pose(), x, y, 0)
                    .color(r, g, b, a)
                    .endVertex();
        }

        // Завершаем рисование
        BufferUploader.drawWithShader(buffer.end());

        // Восстанавливаем состояние
        RenderSystem.enableDepthTest();
        RenderSystem.enableCull();
        RenderSystem.disableBlend();
        poseStack.popPose();
    }
//    private void renderWheel(GuiGraphics guiGraphics, int centerX, int centerY, float scale) {
//        var poseStack = guiGraphics.pose();
//        poseStack.pushPose();
//        poseStack.translate(centerX, centerY, 0);
//        poseStack.scale(scale, scale, 1.0f);
//
//        int halfSize = WHEEL_SIZE / 2;
//
//        guiGraphics.blit(
//                WHEEL_TEXTURE,
//                -halfSize, -halfSize,
//                0, 0,
//                WHEEL_SIZE, WHEEL_SIZE,
//                WHEEL_SIZE, WHEEL_SIZE
//        );
//
//        poseStack.popPose();
//    }

    private void renderIconsAndText(GuiGraphics guiGraphics, int centerX, int centerY, float scale) {
        int count = actions.size();
        if (count == 0) return;

        float anglePerSegment = 360.0f / count;
        int radius = 40;

        for (int i = 0; i < count; i++) {
            float angle = (float) Math.toRadians(i * anglePerSegment - 90 + (anglePerSegment / 2));
            int x = centerX + (int) (Math.cos(angle) * radius * scale) - 8;
            int y = centerY + (int) (Math.sin(angle) * radius * scale) - 8;

            // Отрисовка иконки
            WheelAction action = actions.get(i);
            ItemStack icon = action.getIcon();
            if (icon.isEmpty()) {
                icon = new ItemStack(Items.PAPER);
            }

            if (i == selectedSegment) {
                // Подсветка выбранной иконки
                guiGraphics.pose().pushPose();
                float iconScale = 1.2f; // Немного увеличиваем масштаб
                guiGraphics.pose().translate(x + 8, y + 8, 100);
                guiGraphics.pose().scale(iconScale, iconScale, 1.0f);
                guiGraphics.pose().translate(-8, -8, 0);

                guiGraphics.renderItem(icon, 0, 0);
                guiGraphics.renderItemDecorations(minecraft.font, icon, 0, 0);
                guiGraphics.pose().popPose();
            } else {
                guiGraphics.renderItem(icon, x, y);
                guiGraphics.renderItemDecorations(minecraft.font, icon, x, y);
            }

            if (i == selectedSegment) {
                Component title = action.getTitle();
                int color = action.getColor();

                guiGraphics.drawCenteredString(
                        minecraft.font,
                        title,
                        centerX,
                        centerY - 70,
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

        double angle = getAngle(mouseX, mouseY);
        if (angle < 0) angle += 360;

        int count = actions.size();
        float anglePerSegment = 360.0f / count;
        selectedSegment = (int) (angle / anglePerSegment);

        if (selectedSegment >= count) {
            selectedSegment = count - 1;
        }
    }

    private double getAngle(double mouseX, double mouseY) {
        int screenWidth = minecraft.getWindow().getGuiScaledWidth();
        int screenHeight = minecraft.getWindow().getGuiScaledHeight();
        int centerX = screenWidth / 2;
        int centerY = screenHeight / 2;

        double dx = mouseX - centerX;
        double dy = mouseY - centerY;

        // Игнорируем центр колеса (мертвая зона)
        double distance = Math.sqrt(dx * dx + dy * dy);
        if (distance < 20) {
            selectedSegment = -1;
            return 0;
        }

        return Math.toDegrees(Math.atan2(dy, dx)) + 90;
    }

    public boolean isVisible() {
        return isVisible;
    }

    public int getSelectedSegment() {
        return selectedSegment;
    }
}