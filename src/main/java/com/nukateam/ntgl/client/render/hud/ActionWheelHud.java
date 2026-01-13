package com.nukateam.ntgl.client.render.hud;

import com.mojang.blaze3d.systems.RenderSystem;
import com.nukateam.ntgl.client.util.handler.AimingHandler;
import com.nukateam.ntgl.common.data.holders.AttachmentType;
import com.nukateam.ntgl.common.util.util.WeaponStateHelper;
import com.nukateam.ntgl.modules.wheel.ActionWheel;
import com.nukateam.ntgl.modules.wheel.ActionWheelManager;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraftforge.client.gui.overlay.ForgeGui;
import net.minecraftforge.client.gui.overlay.IGuiOverlay;

public class ActionWheelHud implements IGuiOverlay {
    public static final IGuiOverlay HUD = new ActionWheelHud();

    @Override
    public void render(ForgeGui gui, GuiGraphics graphics, float partialTick, int width, int height) {
        var minecraft = Minecraft.getInstance();
        var player = minecraft.player;
        if (player == null) return;
        var wheel = ActionWheelManager.getInstance().getWheel();

        var mouseX = minecraft.mouseHandler.xpos();
        var mouseY = minecraft.mouseHandler.ypos();

        if (wheel.isVisible()) {
            wheel.updateMousePosition(mouseX, mouseY);
            wheel.render(graphics);
        }
    }
}
