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
import org.joml.Matrix4f;

import java.util.ArrayList;
import java.util.List;

public class ActionWheel {
    private static final ResourceLocation WHEEL_TEXTURE = new ResourceLocation(Ntgl.MOD_ID, "textures/gui/action_wheel.png");
    private static final int WHEEL_SIZE = 128;
    private static final int SEGMENT_SIZE = 64;

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

//        RenderSystem.setShader(GameRenderer::getPositionTexShader);
//        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 0.9F);
//        RenderSystem.setShaderTexture(0, WHEEL_TEXTURE);

        int halfSize = WHEEL_SIZE / 2;
        guiGraphics.blit(WHEEL_TEXTURE, -halfSize, -halfSize, 0, 0, WHEEL_SIZE, WHEEL_SIZE, WHEEL_SIZE, WHEEL_SIZE);

        poseStack.popPose();
    }

    private void renderSelectedSegment(GuiGraphics guiGraphics, int centerX, int centerY, int segment, float scale) {
        if (segment < 0 || segment >= actions.size()) return;

        PoseStack poseStack = guiGraphics.pose();
        poseStack.pushPose();
        poseStack.translate(centerX, centerY, 10);
        poseStack.scale(scale, scale, 1.0f);

        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 0.5F);
        RenderSystem.setShaderTexture(0, WHEEL_TEXTURE);

        float anglePerSegment = 360.0f / actions.size();
        float startAngle = segment * anglePerSegment - 90;

        // Отрисовка выделенного сегмента
        Tesselator tessellator = Tesselator.getInstance();
        BufferBuilder buffer = tessellator.getBuilder();
        RenderSystem.setShader(GameRenderer::getPositionColorShader);

        buffer.begin(VertexFormat.Mode.TRIANGLE_FAN, DefaultVertexFormat.POSITION_COLOR);

        int radius = 50;
        int innerRadius = 30;
        Matrix4f matrix = poseStack.last().pose();

        // Центр
        buffer.vertex(matrix, 0, 0, 0).color(actions.get(segment).getColor());

        // Вершины дуги
        int segments = 20;
        for (int i = 0; i <= segments; i++) {
            float angle = (float) Math.toRadians(startAngle + (anglePerSegment * i / segments));
            float x = (float) Math.cos(angle) * radius;
            float y = (float) Math.sin(angle) * radius;
            buffer.vertex(matrix, x, y, 0).color(actions.get(segment).getColor());
        }

        tessellator.end();

        poseStack.popPose();
    }

    private void renderIconsAndText(GuiGraphics guiGraphics, int centerX, int centerY, float scale) {
        int count = actions.size();
        float anglePerSegment = 360.0f / count;
        int radius = 40;

        for (int i = 0; i < count; i++) {
            float angle = (float) Math.toRadians(i * anglePerSegment - 90);
            int x = centerX + (int) (Math.cos(angle) * radius * scale);
            int y = centerY + (int) (Math.sin(angle) * radius * scale);

            // Отрисовка иконки
            WheelAction action = actions.get(i);
            guiGraphics.renderItem(action.getIcon(), x - 8, y - 8);

            // Отрисовка текста при выделении
            if (i == selectedSegment) {
                guiGraphics.drawCenteredString(minecraft.font, action.getTitle(),
                        centerX, centerY - 70, 0xFFFFFF);
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

        if (distance < 20 || distance > 60) {
            selectedSegment = -1;
            return;
        }

        double angle = Math.toDegrees(Math.atan2(dy, dx)) + 90;
        if (angle < 0) angle += 360;

        int count = actions.size();
        float anglePerSegment = 360.0f / count;
        selectedSegment = (int) (angle / anglePerSegment) % count;
    }

    public boolean isVisible() {
        return isVisible;
    }

    public void setVisible(boolean visible) {
        this.isVisible = visible;
    }
}