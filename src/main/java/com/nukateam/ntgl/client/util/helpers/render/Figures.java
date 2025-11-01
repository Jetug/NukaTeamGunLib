package com.nukateam.ntgl.client.util.helpers.render;

import net.minecraft.client.gui.GuiGraphics;

public class Figures {
    public static void drawBar(GuiGraphics graphics, int x, int y, int width, int height, float percent, int color){
//        var color = percent < 0.25 ? 0xFFFF5555 : 0xFFFFFFFF;
        var value = (int)(width * percent);
        drawFrame(graphics, x, y, width, height, color);
        graphics.fill(x, y, x + value, y + height, color);
    }

    protected static void drawFrame(GuiGraphics graphics, int x, int y, int width, int height, int color) {
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
}
