package com.nukateam.ntgl.client.render.hud;

import com.nukateam.ntgl.client.util.handler.AimingHandler;
import com.nukateam.ntgl.common.base.holders.AttachmentType;
import com.nukateam.ntgl.common.data.config.gun.Gun;
import com.nukateam.ntgl.common.foundation.item.GunItem;
import com.nukateam.ntgl.common.foundation.item.attachment.ScopeItem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraftforge.client.gui.overlay.ForgeGui;
import net.minecraftforge.client.gui.overlay.IGuiOverlay;

public class ScopeHud implements IGuiOverlay {
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
                if(scope.getProperties().hasOverlay()) {
                    graphics.blit(scope.getProperties().getOverlay(), 0, 0, 0, 0, 0.0F,
                            width, height, width, height);
                }
            }
        }
    }

}
