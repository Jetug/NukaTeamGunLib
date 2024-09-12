package com.nukateam.ntgl.client.render.hud;

import com.nukateam.ntgl.Ntgl;
import com.nukateam.ntgl.client.event.InputEvents;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraftforge.client.gui.overlay.IGuiOverlay;

public class DebugHud {
    private static final int BAR_OFFSET_X = 140;
    private static final int OFFSET_Y = 20;

    public static final IGuiOverlay DEBUG_HUD = ((gui, graphics, partialTick, width, height) -> {
        var minecraft = Minecraft.getInstance();
        if(!Ntgl.isDebugging() || minecraft.player == null) return;

        var xCenter = width / 2;
        var x = xCenter + BAR_OFFSET_X;

        renderAmmoCounter(graphics, "x", InputEvents.X, x, OFFSET_Y);
        renderAmmoCounter(graphics, "y", InputEvents.Y, x, OFFSET_Y * 2);
        renderAmmoCounter(graphics, "z", InputEvents.Z, x, OFFSET_Y * 3);
    });

    private static void renderAmmoCounter(GuiGraphics graphics, String label, float val, int x, int y) {
        var text = label + ":" + val;
        graphics.drawString(Minecraft.getInstance().font, text, x, y, 0xFFFFFFFF, true);
    }
}