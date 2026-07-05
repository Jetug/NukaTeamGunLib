package com.nukateam.ntgl.client.render.hud;

import com.nukateam.ntgl.Ntgl;
import com.nukateam.ntgl.client.util.ClientDebug;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;

public class DebugHud {
    private static final int OFFSET_Y = 12;

    public static void render(GuiGraphics graphics, DeltaTracker partialTick) {
        var minecraft = Minecraft.getInstance();
        var mainWindow = minecraft.getWindow();
        int width  = mainWindow.getGuiScaledWidth ();
        int height = mainWindow.getGuiScaledHeight();

        if(ClientDebug.isHidden || !Ntgl.isDebugging() || minecraft.player == null) return;
        var x = width - 70;

        renderString(graphics, "Mode: " + ClientDebug.tuningMode.getName(), x - 80, OFFSET_Y);
        renderAmmoCounter(graphics, "x", ClientDebug.getX(), x, OFFSET_Y * 2);
        renderAmmoCounter(graphics, "y", ClientDebug.getY(), x, OFFSET_Y * 3);
        renderAmmoCounter(graphics, "z", ClientDebug.getZ(), x, OFFSET_Y * 4);
    };

    private static void renderAmmoCounter(GuiGraphics graphics, String label, float val, int x, int y) {
        var text = label + ":" + val;
        graphics.drawString(Minecraft.getInstance().font, text, x, y, 0xFFFFFFFF, true);
    }

    private static void renderString(GuiGraphics graphics, String text, int x, int y) {
        graphics.drawString(Minecraft.getInstance().font, text, x, y, 0xFFFFFFFF, true);
    }
}