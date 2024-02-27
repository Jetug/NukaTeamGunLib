package com.nukateam.gunscore.client.screen.widget;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.resources.ResourceLocation;

public class MiniButton extends Button {
    private final int u, v;
    private final ResourceLocation texture;

    public MiniButton(int x, int y, int u, int v, ResourceLocation texture, OnPress onPress) {
        this(x, y, u, v, texture, onPress, NO_TOOLTIP);
    }

    public MiniButton(int x, int y, int u, int v, ResourceLocation texture, OnPress onPress, OnTooltip onTooltip) {
        super(x, y, 10, 10, TextComponent.EMPTY, onPress, onTooltip);
        this.u = u;
        this.v = v;
        this.texture = texture;
    }

    @Override
    public void renderButton(PoseStack matrixStack, int mouseX, int mouseY, float partialTick) {
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.setShaderTexture(0, this.texture);
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.enableDepthTest();
        this.blit(matrixStack, this.x, this.y, this.u, this.v, this.width, this.height);
        if (this.isHovered) {
            this.fillGradient(matrixStack, this.x, this.y, this.x + 10, this.y + 10, -2130706433, -2130706433);
        }
    }
}
