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

    private void renderSelectedSegment(GuiGraphics guiGraphics, int centerX, int centerY, int segment, float scale) {
        if (segment < 0 || segment >= actions.size()) return;

        PoseStack poseStack = guiGraphics.pose();
        poseStack.pushPose();
        poseStack.translate(centerX, centerY, 50); // Z=50 чтобы рисовать поверх колеса

        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.setShader(GameRenderer::getPositionColorShader);

        // Получаем цвет и делаем полупрозрачным
        int color = actions.get(segment).getColor();
        color = (color & 0x00FFFFFF) | 0x80000000;

        // Разбираем цвет на компоненты
        float a = ((color >> 24) & 0xFF) / 255.0f;
        float r = ((color >> 16) & 0xFF) / 255.0f;
        float g = ((color >> 8) & 0xFF) / 255.0f;
        float b = (color & 0xFF) / 255.0f;

        // Усиливаем цвет для выделения
        r = Math.min(1.0f, r * 1.5f);
        g = Math.min(1.0f, g * 1.5f);
        b = Math.min(1.0f, b * 1.5f);

        // Параметры сегмента
        int count = actions.size();
        if (count == 0) {
            poseStack.popPose();
            return;
        }

        // Углы В ГРАДУСАХ, как в остальном коде
        float anglePerSegment = 360.0f / count;

        // Начальный и конечный углы В ГРАДУСАХ
        float startAngleDeg = segment * anglePerSegment;
        float endAngleDeg = startAngleDeg + anglePerSegment;

        // Конвертируем в радианы
        float startAngle = (float) Math.toRadians(startAngleDeg);
        float endAngle = (float) Math.toRadians(endAngleDeg);

        // Радиусы
        float innerRadius = 10f;  // От центра
        float outerRadius = 55f;  // До края

        Tesselator tessellator = Tesselator.getInstance();
        BufferBuilder buffer = tessellator.getBuilder();

        // Рисуем сегмент как треугольную полоску (TRIANGLE_STRIP)
        buffer.begin(VertexFormat.Mode.TRIANGLE_STRIP, DefaultVertexFormat.POSITION_COLOR);

        Matrix4f matrix = poseStack.last().pose();

        // Количество шагов для сглаживания дуги
        int steps = 16;

        for (int i = 0; i <= steps; i++) {
            // Интерполируем угол от startAngle до endAngle
            float t = (float) i / steps;
            float angle = startAngle + (endAngle - startAngle) * t;

            // Вычисляем синус и косинус один раз
            float cos = (float) Math.cos(angle);
            float sin = (float) Math.sin(angle);

            // Внутренняя точка
            buffer.vertex(matrix, cos * innerRadius, sin * innerRadius, 0)
                    .color(r, g, b, a * 0.3f);

            // Внешняя точка
            buffer.vertex(matrix, cos * outerRadius, sin * outerRadius, 0)
                    .color(r, g, b, a * 0.7f);
        }

        tessellator.end();

        // Также рисуем треугольник к центру, чтобы закрыть внутреннюю дырку
        buffer = tessellator.getBuilder();
        buffer.begin(VertexFormat.Mode.TRIANGLE_FAN, DefaultVertexFormat.POSITION_COLOR);

        // Центр
        buffer.vertex(matrix, 0, 0, 0).color(r, g, b, a * 0.2f);

        // Внутренний круг
        for (int i = 0; i <= steps; i++) {
            float t = (float) i / steps;
            float angle = startAngle + (endAngle - startAngle) * t;

            float x = (float) Math.cos(angle) * innerRadius;
            float y = (float) Math.sin(angle) * innerRadius;

            buffer.vertex(matrix, x, y, 0).color(r, g, b, a * 0.3f);
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

            // Если это выбранный сегмент, делаем иконку больше
            if (i == selectedSegment) {
                guiGraphics.pose().pushPose();
                // Масштабируем иконку
                float iconScale = 1.3f;
                guiGraphics.pose().translate(x + 8, y + 8, 100);
                guiGraphics.pose().scale(iconScale, iconScale, 1.0f);
                guiGraphics.pose().translate(-8, -8, 0);

                // Рисуем подсветку вокруг иконки
                RenderSystem.enableBlend();
                RenderSystem.defaultBlendFunc();
                RenderSystem.setShader(GameRenderer::getPositionColorShader);

                Tesselator tessellator = Tesselator.getInstance();
                BufferBuilder buffer = tessellator.getBuilder();
                buffer.begin(VertexFormat.Mode.TRIANGLE_FAN, DefaultVertexFormat.POSITION_COLOR);

                Matrix4f matrix = guiGraphics.pose().last().pose();
                int color = action.getColor();
                float cr = ((color >> 16) & 0xFF) / 255.0f;
                float cg = ((color >> 8) & 0xFF) / 255.0f;
                float cb = (color & 0xFF) / 255.0f;

                // Центр подсветки
                buffer.vertex(matrix, 8, 8, 0).color(cr, cg, cb, 0.3f);

                // Круг подсветки
                int segments = 16;
                float highlightRadius = 12;
                for (int j = 0; j <= segments; j++) {
                    float a = (float) (j * 2 * Math.PI / segments);
                    float hx = 8 + (float) Math.cos(a) * highlightRadius;
                    float hy = 8 + (float) Math.sin(a) * highlightRadius;
                    buffer.vertex(matrix, hx, hy, 0).color(cr, cg, cb, 0.1f);
                }

                tessellator.end();

                guiGraphics.renderItem(icon, 0, 0);
                guiGraphics.pose().popPose();
            } else {
                guiGraphics.renderItem(icon, x, y);
            }

            // Отрисовка текста при выделении
            if (i == selectedSegment) {
                Component title = action.getTitle();
                // Тень текста
                guiGraphics.drawCenteredString(
                        minecraft.font,
                        title,
                        centerX + 1,
                        centerY - 70,
                        0x000000
                );
                // Основной текст
                guiGraphics.drawCenteredString(
                        minecraft.font,
                        title,
                        centerX,
                        centerY - 71,
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
}