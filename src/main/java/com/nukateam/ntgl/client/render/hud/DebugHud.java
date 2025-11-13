package com.nukateam.ntgl.client.render.hud;

import com.nukateam.ntgl.Ntgl;
import com.nukateam.ntgl.client.util.ClientDebug;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.neoforged.neoforge.client.gui.overlay.IGuiOverlay;

public class DebugHud {
    private static final int BAR_OFFSET_X = 140;
    private static final int OFFSET_Y = 15;

    public static final IGuiOverlay DEBUG_HUD = ((gui, graphics, partialTick, width, height) -> {
        var minecraft = Minecraft.getInstance();
        if(ClientDebug.isHidden || !Ntgl.isDebugging() || minecraft.player == null) return;
        var x = width - 70;

        renderAmmoCounter(graphics, "x", ClientDebug.X, x, OFFSET_Y);
        renderAmmoCounter(graphics, "y", ClientDebug.Y, x, OFFSET_Y * 2);
        renderAmmoCounter(graphics, "z", ClientDebug.Z, x, OFFSET_Y * 3);
    });

    private static void renderAmmoCounter(GuiGraphics graphics, String label, float val, int x, int y) {
        var text = label + ":" + val;
        graphics.drawString(Minecraft.getInstance().font, text, x, y, 0xFFFFFFFF, true);
    }
}