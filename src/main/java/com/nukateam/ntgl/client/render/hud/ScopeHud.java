package com.nukateam.ntgl.client.render.hud;

import com.mojang.blaze3d.systems.RenderSystem;
import com.nukateam.ntgl.client.util.handler.AimingHandler;
import com.nukateam.ntgl.common.base.holders.AttachmentType;
import com.nukateam.ntgl.common.data.config.gun.Gun;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;
import net.minecraftforge.client.gui.overlay.ForgeGui;
import net.minecraftforge.client.gui.overlay.IGuiOverlay;

public class ScopeHud implements IGuiOverlay {
    protected static final ResourceLocation SPYGLASS_SCOPE_LOCATION = new ResourceLocation("textures/misc/spyglass_scope.png");
    public static final IGuiOverlay SCOPE_HUD = new ScopeHud();
    private float scopeScale;

    @Override
    public void render(ForgeGui gui, GuiGraphics graphics, float partialTick, int width, int height) {
        var minecraft = Minecraft.getInstance();
        var player = minecraft.player;
        if (player == null) return;
        var gun = player.getMainHandItem();
        var frameTime = minecraft.getDeltaFrameTime();

        scopeScale = Mth.lerp(0.5F * frameTime, scopeScale, 1.125F);

        if (AimingHandler.isScoping(gun)) {
            var attachment = Gun.getAttachmentItem(AttachmentType.SCOPE, gun);
            if (!attachment.isEmpty()) {
                var scope = Gun.getScopeItem(gun);
                var overlay = scope.getProperties().getOverlay();
                setupOverlayRenderState(true);
                renderScope(graphics, width, height, overlay);
            }
        } else {
            scopeScale = 0.5F;
        }
    }

    private void renderScope(GuiGraphics graphics, int width, int height, ResourceLocation overlay) {
        var f = (float) Math.min(width, height);
        var f1 = Math.min((float) width / f, (float) height / f) * scopeScale;
        int i = Mth.floor(f * f1);
        int j = Mth.floor(f * f1);
        int k = (width - i) / 2;
        int l = (height - j) / 2;
        int i1 = k + i;
        int j1 = l + j;
        graphics.blit(overlay, k, l, -90, 0.0F, 0.0F, i, j, i, j);
        graphics.fill(RenderType.guiOverlay(), 0, j1, width, height, -90, -16777216);
        graphics.fill(RenderType.guiOverlay(), 0, 0, width, l, -90, -16777216);
        graphics.fill(RenderType.guiOverlay(), 0, l, k, j1, -90, -16777216);
        graphics.fill(RenderType.guiOverlay(), i1, l, width, j1, -90, -16777216);
    }

    public void setupOverlayRenderState(boolean blend) {
        if (blend) {
            RenderSystem.enableBlend();
            RenderSystem.defaultBlendFunc();
        }
        else RenderSystem.disableBlend();

        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
    }
}
