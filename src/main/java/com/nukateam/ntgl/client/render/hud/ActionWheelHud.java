package com.nukateam.ntgl.client.render.hud;

import com.mojang.blaze3d.systems.RenderSystem;
import com.nukateam.ntgl.client.util.helpers.render.Figures;
import com.nukateam.ntgl.modules.wheel.ActionWheel;
import com.nukateam.ntgl.modules.wheel.ActionWheelManager;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
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
        var poseStack = guiGraphics.pose();
        poseStack.pushPose();
        {
            poseStack.scale(scale, scale, 1.0f);

            guiGraphics.drawCenteredString(
                    Minecraft.getInstance().font,
                    wheel.getTitle(),
                    (int) (centerX / scale),
                    (int)((centerY - 75) / scale),
                    0xFFFFFFFF
            );

            RenderSystem.setShaderColor(1,1,1,0.3f);
            {
                var count = wheel.getActions().size();
                var anglePerSegment = 360.0f / count;

                var offset = 0;
                for (var action : wheel.getActions()) {
                    Figures.drawSegment(guiGraphics, centerX / scale, centerY / scale,
                            WHEEL_SIZE / 2f, offset, anglePerSegment, action.getColor());

                    Figures.drawBorder(guiGraphics, centerX / scale, centerY / scale,
                            WHEEL_SIZE / 2f, offset, anglePerSegment, 0xFFFFFFFF, 1);

                    if (wheel.getActions().size() > 1) {
                        Figures.drawOutline(guiGraphics, centerX / scale, centerY / scale,
                                WHEEL_SIZE / 2f, offset, anglePerSegment, 0xFFFFFFFF, 1);
                    }
                    offset += anglePerSegment;
                }
                renderSelectedSegment(wheel, guiGraphics, centerX, centerY, scale);
            }
            RenderSystem.setShaderColor(1,1,1,1);
        }
        poseStack.popPose();
    }

    private void renderSelectedSegment(ActionWheel wheel, GuiGraphics guiGraphics, int centerX, int centerY, float scale) {
        var count = wheel.getActions().size();
        if (count == 0 || wheel.getSelectedSegment() < 0) return;

        var anglePerSegment = 360.0f / count;
        var startAngle = wheel.getSelectedSegment() * anglePerSegment;

        Figures.drawSegment(guiGraphics, centerX / scale, centerY / scale,
                WHEEL_SIZE / 2f, startAngle, anglePerSegment, 0xFFFFFFFF);
        Figures.drawBorder(guiGraphics, centerX / scale, centerY / scale,
                WHEEL_SIZE / 2f, startAngle, anglePerSegment, 0xF7FF00FF, 2);
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

                guiGraphics.drawCenteredString(
                        Minecraft.getInstance().font,
                        title,
                        centerX,
                        centerY + 75,
                        0xFFFFFFFF
                );
            }
        }
    }
}
