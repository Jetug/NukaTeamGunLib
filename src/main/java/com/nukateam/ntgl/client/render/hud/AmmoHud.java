package com.nukateam.ntgl.client.render.hud;

import com.mojang.blaze3d.systems.RenderSystem;
import com.nukateam.ntgl.common.base.gun.Gun;
import com.nukateam.ntgl.common.data.util.GunEnchantmentHelper;
import com.nukateam.ntgl.common.foundation.item.GunItem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.client.gui.overlay.ForgeGui;
import net.minecraftforge.client.gui.overlay.IGuiOverlay;

public class AmmoHud {
    private static final int BAR_OFFSET_X = 95;
    private static final int BG_WIDTH = 44;
    private static final int BG_HEIGHT = 50;

    public static final IGuiOverlay AMMO_HUD = ((gui, graphics, partialTick, width, height) -> {
        var minecraft = Minecraft.getInstance();
        if(minecraft.player == null) return;
        var mainHandItem = minecraft.player.getMainHandItem();
        var offhandItem  = minecraft.player.getOffhandItem();
        var screenWidth = minecraft.getWindow().getGuiScaledWidth();
        var screenHeight = minecraft.getWindow().getGuiScaledHeight();
        var y = height - 39;

        if(mainHandItem.getItem() instanceof GunItem gunItem) {
            var x = screenWidth / 2 + BAR_OFFSET_X;
            renderAmmoCounter(graphics, gunItem, mainHandItem, x, y);
        }
        if(offhandItem.getItem() instanceof GunItem gunItem) {
            var x = screenWidth / 2 - BAR_OFFSET_X - 26;
            renderAmmoCounter(graphics, gunItem, offhandItem, x, y);
        }
    });

    private static void renderAmmoCounter(GuiGraphics graphics, GunItem gunItem, ItemStack heldStack, int x, int y) {
        var gun = gunItem.getModifiedGun(heldStack);
        var currentAmmo = Gun.getAmmo(heldStack);
        var maxAmmo = GunEnchantmentHelper.getAmmoCapacity(heldStack, gun);
        var text = currentAmmo + "/" + maxAmmo;

        graphics.drawString(Minecraft.getInstance().font, text, x, y, 0xFFFFFFFF, true);
    }
}

