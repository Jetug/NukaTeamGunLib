package com.nukateam.ntgl.client.render.crosshair;


import com.nukateam.ntgl.client.util.handler.AimingHandler;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.resources.ResourceLocation;

/**
 * Author: MrCrayfish
 */
public class TexturedCrosshair extends Crosshair {
    private ResourceLocation texture;
    private boolean blend;

    public TexturedCrosshair(ResourceLocation id) {
        this(id, true);
    }

    public TexturedCrosshair(ResourceLocation id, boolean blend) {
        super(id);
        this.texture = ResourceLocation.tryBuild(id.getNamespace(), "textures/crosshair/" + id.getPath() + ".png");
        this.blend = blend;
    }

    @Override
    public void render(Minecraft mc, PoseStack stack, int windowWidth, int windowHeight, float partialTicks) {
        stack.pushPose();

        float alpha = 1.0F - (float) AimingHandler.get().getNormalisedAdsProgress();
        float size = 8.0F;
        stack.translate((windowWidth - size) / 2F, (windowHeight - size) / 2F, 0);

        RenderSystem.setShader(GameRenderer::getPositionTexColorShader);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.setShaderTexture(0, this.texture);
        RenderSystem.enableBlend();

        if (this.blend) {
            RenderSystem.blendFuncSeparate(GlStateManager.SourceFactor.ONE_MINUS_DST_COLOR, GlStateManager.DestFactor.ONE_MINUS_SRC_COLOR, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
        }

        var matrix = stack.last().pose();
        var tesselator = Tesselator.getInstance();
        var builder = tesselator.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX_COLOR);
        builder.addVertex(matrix, 0, size, 0).setUv(0, 1).setColor(1.0F, 1.0F, 1.0F, alpha);
        builder.addVertex(matrix, size, size, 0).setUv(1, 1).setColor(1.0F, 1.0F, 1.0F, alpha);
        builder.addVertex(matrix, size, 0, 0).setUv(1, 0).setColor(1.0F, 1.0F, 1.0F, alpha);
        builder.addVertex(matrix, 0, 0, 0).setUv(0, 0).setColor(1.0F, 1.0F, 1.0F, alpha);
        BufferUploader.drawWithShader(builder.build());

        if (this.blend) {
            RenderSystem.defaultBlendFunc();
        }

        stack.popPose();
    }
}
