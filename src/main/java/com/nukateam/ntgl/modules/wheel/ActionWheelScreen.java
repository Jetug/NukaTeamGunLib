package com.nukateam.ntgl.modules.wheel;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public class ActionWheelScreen extends Screen {
    private final ActionWheel wheel;

    public ActionWheelScreen() {
        super(Component.empty());
        this.wheel = ActionWheelManager.getInstance().getWheel();
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks) {
        // Прозрачный фон
        renderBackground(guiGraphics);

        // Отрисовка колеса
        if (wheel.isVisible()) {
            wheel.updateMousePosition(mouseX, mouseY);
            wheel.render(guiGraphics);
        }

        super.render(guiGraphics, mouseX, mouseY, partialTicks);
    }

    @Override
    public void renderBackground(GuiGraphics guiGraphics) {
        // Полупрозрачный черный фон
        guiGraphics.fill(0, 0, this.width, this.height, 0x80000000);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button == 0) { // ЛКМ
            ActionWheelManager.getInstance().onKeyReleased();
            return true;
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean keyReleased(int keyCode, int scanCode, int modifiers) {
        if (keyCode == org.lwjgl.glfw.GLFW.GLFW_KEY_R) {
            ActionWheelManager.getInstance().onKeyReleased();
            return true;
        }
        return super.keyReleased(keyCode, scanCode, modifiers);
    }
}