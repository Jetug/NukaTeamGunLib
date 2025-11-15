package com.nukateam.chassis_core.client.gui.screen;

import com.nukateam.chassis_core.client.gui.screen.GuiBase;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;

import static com.mojang.blaze3d.systems.RenderSystem.setShader;
import static com.mojang.blaze3d.systems.RenderSystem.setShaderColor;

public class ArmorStationScreen<T extends AbstractContainerMenu> extends GuiBase<T> {
    private final ResourceLocation guiResource;

    public ArmorStationScreen(T menu, Inventory pPlayerInventory,
                              Component pTitle, ResourceLocation guiResource) {
        super(menu, pPlayerInventory, pTitle);
        this.guiResource = guiResource;
        this.imageHeight = 187;
        this.inventoryLabelY = imageHeight - 94;
    }

    @Override
    public void render(GuiGraphics pGuiGraphics, int mouseX, int mouseY, float delta) {
        renderBackground(pGuiGraphics, mouseX,  mouseY, delta);
        super.render(pGuiGraphics, mouseX, mouseY, delta);
        renderTooltip(pGuiGraphics, mouseX, mouseY);
    }

    @Override
    protected void renderBg(GuiGraphics pGuiGraphics, float pPartialTick, int pMouseX, int pMouseY) {
        setShader(GameRenderer::getPositionTexShader);
        setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);

        int x = (width - imageWidth) / 2;
        int y = (height - imageHeight) / 2;
        pGuiGraphics.blit(guiResource, x, y, 0, 0, imageWidth, imageHeight);
        //blit(pPoseStack, x + 160, y + 5, 178, 6, 6, 6);
    }
}