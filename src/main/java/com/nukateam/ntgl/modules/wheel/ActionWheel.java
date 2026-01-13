package com.nukateam.ntgl.modules.wheel;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import com.mojang.math.Axis;
import com.nukateam.ntgl.Ntgl;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraftforge.client.event.RenderGuiEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import org.joml.Matrix4f;
import org.lwjgl.opengl.GL11;

import java.util.ArrayList;
import java.util.List;

public class ActionWheel {
    private static final ResourceLocation WHEEL_TEXTURE = new ResourceLocation(Ntgl.MOD_ID,
            "textures/gui/action_wheel.png");
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

        float animationProgress = Math.min((System.currentTimeMillis() - openTime) / 200f, 1.0f);
        float scale = 0.5f + animationProgress * 0.5f;

        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();

//        renderWheel(guiGraphics, centerX, centerY, scale);


        float radius = 64;
        float startAngle = 45f; // Начальный угол (45 градусов)
        float sweepAngle = 60f; // Угол сегмента (60 градусов)

        // Цвета (ARGB)
        int fillColor = 0x80FFA500; // Полупрозрачный оранжевый
        int outlineColor = 0xFF8B4513; // Коричневый

        // Рисуем кусок пиццы
        drawPizzaSliceWithOutline(
                guiGraphics,
                centerX,
                centerY,
                radius,
                startAngle,
                sweepAngle,
                fillColor,
                outlineColor,
                2.0f
        );

        renderIconsAndText(guiGraphics, centerX, centerY, scale);

        RenderSystem.disableBlend();
        guiGraphics.flush();
    }

    public static void drawPizzaSlice(GuiGraphics guiGraphics, float centerX, float centerY,
                                      float radius, float startAngle, float sweepAngle, int color) {

        if (radius <= 0 || sweepAngle <= 0) return;

        // Конвертируем углы в радианы
        float startRad = (float) Math.toRadians(startAngle);
        float sweepRad = (float) Math.toRadians(sweepAngle);

        // Разбираем цвет на компоненты
        float alpha = (color >> 24 & 255) / 255.0F;
        float red = (color >> 16 & 255) / 255.0F;
        float green = (color >> 8 & 255) / 255.0F;
        float blue = (color & 255) / 255.0F;

        // Определяем количество сегментов для сглаживания
        int segments = Math.max(16, (int)(sweepAngle / 5));

        // Получаем PoseStack из GuiGraphics
        PoseStack poseStack = guiGraphics.pose();
        poseStack.pushPose();

        // Настраиваем рендер
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.setShader(GameRenderer::getPositionColorShader);

        // Начинаем построение вершин
        Tesselator tesselator = Tesselator.getInstance();
        BufferBuilder buffer = tesselator.getBuilder();
        buffer.begin(VertexFormat.Mode.TRIANGLE_FAN, DefaultVertexFormat.POSITION_COLOR);

        // Центральная точка (острие куска пиццы)
        buffer.vertex(poseStack.last().pose(), centerX, centerY, 0)
                .color(red, green, blue, alpha)
                .endVertex();

        // Генерируем точки по окружности
        for (int i = 0; i <= segments; i++) {
            float angle = startRad + (sweepRad * i / segments);
            float x = centerX + Mth.cos(angle) * radius;
            float y = centerY + Mth.sin(angle) * radius;

            buffer.vertex(poseStack.last().pose(), x, y, 0)
                    .color(red, green, blue, alpha)
                    .endVertex();
        }

        // Завершаем рисование
        BufferUploader.drawWithShader(buffer.end());

        // Восстанавливаем состояние
        RenderSystem.disableBlend();
        poseStack.popPose();
    }

    /**
     * Рисует контур куска пиццы
     */
    public static void drawPizzaSliceOutline(GuiGraphics guiGraphics, float centerX, float centerY,
                                             float radius, float startAngle, float sweepAngle,
                                             int color, float lineWidth) {

        if (radius <= 0 || sweepAngle <= 0 || lineWidth <= 0) return;

        float startRad = (float)  Math.toRadians(startAngle);
        float sweepRad = (float) Math.toRadians(sweepAngle);

        float alpha = (color >> 24 & 255) / 255.0F;
        float red = (color >> 16 & 255) / 255.0F;
        float green = (color >> 8 & 255) / 255.0F;
        float blue = (color & 255) / 255.0F;

        int segments = Math.max(16, (int)(sweepAngle / 5));

        PoseStack poseStack = guiGraphics.pose();
        poseStack.pushPose();

        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.setShader(GameRenderer::getPositionColorShader);
        RenderSystem.lineWidth(lineWidth);

        Tesselator tesselator = Tesselator.getInstance();
        BufferBuilder buffer = tesselator.getBuilder();

        // Рисуем дугу
        buffer.begin(VertexFormat.Mode.DEBUG_LINE_STRIP, DefaultVertexFormat.POSITION_COLOR);

        for (int i = 0; i <= segments; i++) {
            float angle = startRad + (sweepRad * i / segments);
            float x = centerX + Mth.cos(angle) * radius;
            float y = centerY + Mth.sin(angle) * radius;

            buffer.vertex(poseStack.last().pose(), x, y, 0)
                    .color(red, green, blue, alpha)
                    .endVertex();
        }

        BufferUploader.drawWithShader(buffer.end());

        // Рисуем линии от центра к краям
        buffer = tesselator.getBuilder();
        buffer.begin(VertexFormat.Mode.DEBUG_LINES, DefaultVertexFormat.POSITION_COLOR);

        float startX = centerX + Mth.cos(startRad) * radius;
        float startY = centerY + Mth.sin(startRad) * radius;
        float endX = centerX + Mth.cos(startRad + sweepRad) * radius;
        float endY = centerY + Mth.sin(startRad + sweepRad) * radius;

        // Первая линия
        buffer.vertex(poseStack.last().pose(), centerX, centerY, 0)
                .color(red, green, blue, alpha)
                .endVertex();
        buffer.vertex(poseStack.last().pose(), startX, startY, 0)
                .color(red, green, blue, alpha)
                .endVertex();

        // Вторая линия
        buffer.vertex(poseStack.last().pose(), centerX, centerY, 0)
                .color(red, green, blue, alpha)
                .endVertex();
        buffer.vertex(poseStack.last().pose(), endX, endY, 0)
                .color(red, green, blue, alpha)
                .endVertex();

        BufferUploader.drawWithShader(buffer.end());

        RenderSystem.disableBlend();
        poseStack.popPose();
    }

    /**
     * Рисует кусок пиццы с контуром (заливка + контур)
     */
    public static void drawPizzaSliceWithOutline(GuiGraphics guiGraphics, float centerX, float centerY,
                                                 float radius, float startAngle, float sweepAngle,
                                                 int fillColor, int outlineColor, float outlineWidth) {

        // Сначала заливка, потом контур
        drawPizzaSlice(guiGraphics, centerX, centerY, radius, startAngle, sweepAngle, fillColor);
        drawPizzaSliceOutline(guiGraphics, centerX, centerY, radius, startAngle, sweepAngle,
                outlineColor, outlineWidth);
    }

    private void renderWheel(GuiGraphics guiGraphics, int centerX, int centerY, float scale) {
        PoseStack poseStack = guiGraphics.pose();
        poseStack.pushPose();
        poseStack.translate(centerX, centerY, 0);
        poseStack.scale(scale, scale, 1.0f);

        int halfSize = WHEEL_SIZE / 2;

        RenderSystem.setShaderColor(1,1,1,0.3f);
        guiGraphics.blit(
                WHEEL_TEXTURE,
                -halfSize, -halfSize,
                0, 0,
                WHEEL_SIZE, WHEEL_SIZE,
                WHEEL_SIZE, WHEEL_SIZE
        );
        RenderSystem.setShaderColor(1,1,1,1);

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

            // Отрисовка текста при выделении
            if (i == selectedSegment) {
                Component title = action.getTitle();
                int color = action.getColor();

                // Тень текста
                guiGraphics.drawCenteredString(
                        minecraft.font,
                        title,
                        centerX + 1,
                        centerY - 70 + 1,
                        0x000000
                );
                // Основной текст
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