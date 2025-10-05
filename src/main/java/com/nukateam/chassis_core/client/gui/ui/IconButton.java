package com.nukateam.chassis_core.client.gui.ui;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractButton;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@SuppressWarnings("PointlessArithmeticExpression")
@OnlyIn(Dist.CLIENT)
public abstract class IconButton extends AbstractButton {
    private final ResourceLocation texture;
    private final int iconX;
    private final int iconY;

    private boolean selected;

    public IconButton(int pX, int pY, int iconX, int iconY, Component pMessage, ResourceLocation texture) {
        super(pX, pY, 22, 22, pMessage);
        this.texture = texture;
        this.iconX = iconX;
        this.iconY = iconY;
    }

    protected void renderIcon(GuiGraphics graphics, ResourceLocation resourceLocation) {
        graphics.blit(resourceLocation, this.getX() + 2, this.getY(), this.iconX, this.iconY, 18, 18);
    }

    public void renderToolTip(GuiGraphics pGuiGraphics, int pMouseX, int pMouseY) {}

    @Override
    protected void renderWidget(GuiGraphics pGuiGraphics, int pMouseX, int pMouseY, float pPartialTick) {
//        super.renderWidget(pGuiGraphics, pMouseX, pMouseY, pPartialTick);

        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderTexture(0, texture);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        int j = 0;
        if (!this.active) {
            j += this.width * 2;
        } else if (this.selected) {
            j += this.width * 1;
        } else if (this.isHoveredOrFocused()) {
            j += this.width * 3;
        }

        pGuiGraphics.blit(texture, this.getX(), this.getY(), j, 166, this.width, this.height);
        this.renderIcon(pGuiGraphics, texture);
    }

//    @Override
//    public void renderButton(GuiGraphics graphics, int pMouseX, int pMouseY, float pPartialTick) {
//        RenderSystem.setShader(GameRenderer::getPositionTexShader);
//        RenderSystem.setShaderTexture(0, texture);
//        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
//        int j = 0;
//        if (!this.active) {
//            j += this.width * 2;
//        } else if (this.selected) {
//            j += this.width * 1;
//        } else if (this.isHoveredOrFocused()) {
//            j += this.width * 3;
//        }
//
//        graphics.blit(texture, this.getX(), this.getY(), j, 166, this.width, this.height);
//        this.renderIcon(graphics, texture);
//    }

    public boolean isSelected() {
        return this.selected;
    }

    public void setSelected(boolean pSelected) {
        this.selected = pSelected;
    }

    public boolean isShowingTooltip() {
        return this.isHovered;
    }
//
//    @Override
//    public void updateNarration(NarrationElementOutput pNarrationElementOutput) {
//        this.defaultButtonNarrationText(pNarrationElementOutput);
//    }
}