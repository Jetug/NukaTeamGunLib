package com.nukateam.ntgl.client.render.hud;

import com.nukateam.ntgl.client.util.handler.AimingHandler;
import com.nukateam.ntgl.common.base.holders.AttachmentType;
import com.nukateam.ntgl.common.data.config.gun.Gun;
import com.nukateam.ntgl.common.foundation.item.GunItem;
import com.nukateam.ntgl.common.foundation.item.attachment.ScopeItem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.client.gui.overlay.ForgeGui;
import net.minecraftforge.client.gui.overlay.IGuiOverlay;

public class ScopeHud implements IGuiOverlay {
    public static final IGuiOverlay SCOPE_HUD = new ScopeHud();

    @Override
    public void render(ForgeGui gui, GuiGraphics graphics, float partialTick, int width, int height) {
        var player = Minecraft.getInstance().player; if (player == null) return;
        var gun = player.getMainHandItem();

        if (Gun.isAiming(gun) && Gun.hasScopeOverlay(gun)) {
            var attachment = Gun.getAttachmentItem(AttachmentType.SCOPE, gun);
            if(!attachment.isEmpty()){
                var scope = Gun.getScopeItem(gun);
                graphics.blit(scope.getProperties().getOverlay(),
                        0, 0, 0, 0, 0.0F,
                        width, height, width, height);
            }
        }
    }
}
