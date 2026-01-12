package com.nukateam.ntgl.modules.wheel;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import com.nukateam.ntgl.Ntgl;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.joml.Matrix4f;

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

        // Анимация появления
        float animationProgress = Math.min((System.currentTimeMillis() - openTime) / 200f, 1.0f);
        float scale = 0.5f + animationProgress * 0.5f;

        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();

        // Отрисовка колеса
        renderWheel(guiGraphics, centerX, centerY, scale);

        // Отрисовка выделенного сегмента
        if (selectedSegment >= 0) {
            renderSelectedSegment(guiGraphics, centerX, centerY, selectedSegment, scale);
        }

        // Отрисовка иконок и текста
        renderIconsAndText(guiGraphics, centerX, centerY, scale);

        RenderSystem.disableBlend();
    }

    private void renderWheel(GuiGraphics guiGraphics, int centerX, int centerY, float scale) {
        PoseStack poseStack = guiGraphics.pose();
        poseStack.pushPose();
        poseStack.translate(centerX, centerY, 0);
        poseStack.scale(scale, scale, 1.0f);

        int halfSize = WHEEL_SIZE / 2;

        // Используем blit с явным указанием размеров текстуры
        guiGraphics.blit(
                WHEEL_TEXTURE,
                -halfSize, -halfSize,          // позиция на экране
                0, 0,                          // координаты текстуры (u, v)
                WHEEL_SIZE, WHEEL_SIZE,        // размер отрисовываемой области
                WHEEL_SIZE, WHEEL_SIZE         // размер текстуры
        );

        poseStack.popPose();
    }

    private void renderSelectedSegment(GuiGraphics guiGraphics, int centerX, int centerY, int segment, float scale) {
        if (segment < 0 || segment >= actions.size()) return;

        PoseStack poseStack = guiGraphics.pose();
        poseStack.pushPose();
        poseStack.translate(centerX, centerY, 10);
        poseStack.scale(scale, scale, 1.0f);

        RenderSystem.setShader(GameRenderer::getPositionColorShader);
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();

        float anglePerSegment = 360.0f / actions.size();
        float startAngle = segment * anglePerSegment - 90;
        float endAngle = startAngle + anglePerSegment;

        // Отрисовка выделенного сегмента как сектора круга
        Tesselator tessellator = Tesselator.getInstance();
        BufferBuilder buffer = tessellator.getBuilder();

        buffer.begin(VertexFormat.Mode.TRIANGLE_FAN, DefaultVertexFormat.POSITION_COLOR);

        int radius = 50;
        int innerRadius = 30;
        Matrix4f matrix = poseStack.last().pose();
        int color = actions.get(segment).getColor();

        // Центр (прозрачный)
        buffer.vertex(matrix, 0, 0, 0).color((color & 0x00FFFFFF) | 0x80000000);

        // Вершины дуги
        int segments = 20;
        for (int i = 0; i <= segments; i++) {
            float angle = (float) Math.toRadians(startAngle + (anglePerSegment * i / segments));
            float x = (float) Math.cos(angle) * radius;
            float y = (float) Math.sin(angle) * radius;
            buffer.vertex(matrix, x, y, 0).color((color & 0x00FFFFFF) | 0x80000000);
        }

        tessellator.end();

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

            guiGraphics.renderItem(icon, x, y);

            // Отрисовка текста при выделении
            if (i == selectedSegment) {
                Component title = action.getTitle();
                int textWidth = minecraft.font.width(title);
                guiGraphics.drawCenteredString(
                        minecraft.font,
                        title,
                        centerX,
                        centerY - 70,
                        0xFFFFFF
                );
            }
        }
    }

    public void updateMousePosition(double mouseX, double mouseY) {
        if (!isVisible || actions.isEmpty()) {
            selectedSegment = -1;
            return;
        }

        int screenWidth = minecraft.getWindow().getGuiScaledWidth();
        int screenHeight = minecraft.getWindow().getGuiScaledHeight();
        int centerX = screenWidth / 2;
        int centerY = screenHeight / 2;

        double dx = mouseX - centerX;
        double dy = mouseY - centerY;
        double distance = Math.sqrt(dx * dx + dy * dy);

        // Игнорируем клики слишком близко или далеко от центра
        if (distance < 20) {
            selectedSegment = -1;
            return;
        }

        // Вычисляем угол
        double angle = Math.toDegrees(Math.atan2(dy, dx)) + 90;
        if (angle < 0) angle += 360;

        int count = actions.size();
        float anglePerSegment = 360.0f / count;
        selectedSegment = (int) (angle / anglePerSegment);

        if (selectedSegment >= count) {
            selectedSegment = count - 1;
        }
    }

    public boolean isVisible() {
        return isVisible;
    }

    public void setVisible(boolean visible) {
        this.isVisible = visible;
    }
}