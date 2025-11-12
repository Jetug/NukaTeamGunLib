package com.nukateam.chassis_core.client.render.utils;

import com.nukateam.chassis_core.common.util.helpers.PlayerUtils;
import net.minecraft.client.gui.GuiGraphics;

public class GuiUtils {
    public static void drawChassisIcon(GuiGraphics gui, int x, int y) {
//        RenderSystem.setShaderTexture(0, PlayerUtils.getLocalPlayerChassis().getIcon());
        gui.blit(PlayerUtils.getLocalPlayerChassis().getIcon(), x, y, 0, 0, 16, 16, 16, 16);
    }
}
