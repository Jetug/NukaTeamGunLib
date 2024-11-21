package com.nukateam.ntgl.client.render.hud;

import com.mojang.blaze3d.vertex.BufferBuilder;
import com.nukateam.ntgl.Ntgl;
import com.nukateam.ntgl.client.util.handler.AimingHandler;
import com.nukateam.ntgl.common.base.holders.AttachmentType;
import com.nukateam.ntgl.common.data.config.gun.Gun;
import com.nukateam.ntgl.common.foundation.item.GunItem;
import com.nukateam.ntgl.common.foundation.item.ScopeItem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraftforge.client.gui.overlay.ForgeGui;
import net.minecraftforge.client.gui.overlay.IGuiOverlay;

public class ScopeHud implements IGuiOverlay {
    protected static final ResourceLocation SPYGLASS_SCOPE_LOCATION = new ResourceLocation(Ntgl.MOD_ID, "textures/hud/overlay/scope_long_overlay.png");
    public static final IGuiOverlay SCOPE_HUD = new ScopeHud();

    @Override
    public void render(ForgeGui gui, GuiGraphics graphics, float partialTick, int width, int height) {
        var minecraft = Minecraft.getInstance();

        if (minecraft.player == null) return;

        var mainHandItem = minecraft.player.getMainHandItem();

        var prog = AimingHandler.get().getAimProgress(minecraft.player, minecraft.getFrameTime());

        if (mainHandItem.getItem() instanceof GunItem && AimingHandler.get().isAiming() && prog == 1) {
            var attachment = Gun.getAttachmentItem(AttachmentType.SCOPE, mainHandItem);
            if(!attachment.isEmpty() ){
                var scope = (ScopeItem)attachment.getItem();
                if(scope.getProperties().drawOverlay())
                    renderSpyglassOverlay(graphics, 1, width, height);
            }
        }
    }

    public void renderSpyglassOverlay(GuiGraphics pGuiGraphics, float pScopeScale, int screenWidth, int screenHeight) {
        float f = (float)Math.min(screenWidth, screenHeight);
        float f1 = Math.min((float)screenWidth / f, (float)screenHeight / f) * pScopeScale;
        int i = Mth.floor(f * f1);
        int j = Mth.floor(f * f1);
        int k = (screenWidth - i) / 2;
        int l = (screenHeight - j) / 2;
        int i1 = k + i;
        int j1 = l + j;
        pGuiGraphics.blit(SPYGLASS_SCOPE_LOCATION, 0, 0, 0, 0, 0.0F, screenWidth, screenHeight, screenWidth, screenHeight);
//        pGuiGraphics.fill(RenderType.guiOverlay(), 0, j1, screenWidth, screenHeight, -90, -16777216);
//        pGuiGraphics.fill(RenderType.guiOverlay(), 0, 0, screenWidth, l, -90, -16777216);
//        pGuiGraphics.fill(RenderType.guiOverlay(), 0, l, k, j1, -90, -16777216);
//        pGuiGraphics.fill(RenderType.guiOverlay(), i1, l, screenWidth, j1, -90, -16777216);

//        if(scopeType != null && scopeType == ItemScope.Type.LONG && normalZoomProgress == 1.0)
//        {
//            Minecraft mc = Minecraft.getMinecraft();
//            mc.getTextureManager().bindTexture(SCOPE_OVERLAY);
//            GlStateManager.color(1.0F, 1.0F, 1.0F);
//            GlStateManager.enableBlend();
//            GlStateManager.enableAlpha();
//            GlStateManager.disableDepth();
//
//            ScaledResolution scaledResolution = new ScaledResolution(mc);
//
//            Tessellator tessellator = Tessellator.getInstance();
//            BufferBuilder buffer = tessellator.getBuffer();
//            buffer.begin(7, DefaultVertexFormats.POSITION_TEX);
//            buffer.pos(0, scaledResolution.getScaledHeight(), 0).tex(0, 1).endVertex();
//            buffer.pos(scaledResolution.getScaledWidth(), scaledResolution.getScaledHeight(), 0).tex(1, 1).endVertex();
//            buffer.pos(scaledResolution.getScaledWidth(), 0, 0).tex(1, 0).endVertex();
//            buffer.pos(0, 0, 0).tex(0, 0).endVertex();
//            tessellator.draw();
//
//            GlStateManager.disableAlpha();
//            GlStateManager.disableBlend();
//            GlStateManager.enableDepth();
//        }

    }
}
