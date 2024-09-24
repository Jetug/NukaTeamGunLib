package com.nukateam.ntgl.client.render.hud;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.nukateam.ntgl.client.event.InputEvents;
import com.nukateam.ntgl.common.base.gun.Ammo;
import com.nukateam.ntgl.common.base.gun.Gun;
import com.nukateam.ntgl.common.data.util.GunModifierHelper;
import com.nukateam.ntgl.common.foundation.item.AmmoItem;
import com.nukateam.ntgl.common.foundation.item.GunItem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.client.gui.overlay.ForgeGui;
import net.minecraftforge.client.gui.overlay.IGuiOverlay;
import net.minecraftforge.registries.ForgeRegistries;

import java.text.DecimalFormat;

public class GunHud implements IGuiOverlay {
    private static final DecimalFormat CURRENT_AMMO_FORMAT = new DecimalFormat("000");
    private static final DecimalFormat INVENTORY_AMMO_FORMAT = new DecimalFormat("0000");
    public static final float COUNTER_SCALE = 0.9f;
    public static final int LOW_AMMO_COLOR = 0xFF5555;
    public static final int ICON_X = 115;
    private static long checkAmmoTimestamp = -1L;
    private static int cacheMaxAmmoCount = 0;
    private static int cacheInventoryAmmoCount = 0;

    public static final IGuiOverlay AMMO_HUD = new GunHud();
    public static int hudColor = 0xFFFFFF;


    @Override
    public void render(ForgeGui gui, GuiGraphics graphics, float partialTick, int width, int height) {
        var minecraft = Minecraft.getInstance();

        if(minecraft.player == null) return;
        var mainHandItem = minecraft.player.getMainHandItem();
        var offhandItem  = minecraft.player.getOffhandItem();

        if(mainHandItem.getItem() instanceof GunItem) {
            var x = width;
            renderAmmoCounter(graphics, mainHandItem, x, height);
        }
        if(offhandItem.getItem() instanceof GunItem) {
            var x = 110;
            renderAmmoCounter(graphics, offhandItem, x, height);
        }
    }

    public static void setHudColor(int hudColor) {
        GunHud.hudColor = hudColor;
    }

    private static void renderAmmoCounter(GuiGraphics graphics, ItemStack stack, int width, int height) {
        var mc = Minecraft.getInstance();
        var player = mc.player;
        var ammoCount = Gun.getAmmo(stack);
        int ammoCountColor;

        if (ammoCount < (cacheMaxAmmoCount * 0.25))
            ammoCountColor = LOW_AMMO_COLOR;
        else ammoCountColor = hudColor;

        var currentAmmoCountText = CURRENT_AMMO_FORMAT.format(ammoCount);
        handleCacheCount(player, stack);

        graphics.fill(width - 70, height - 30, width - 43, height - 28, 0xFFFFFFFF);

        var poseStack = graphics.pose();
        var font = mc.font;

        renderCurrentAmmo(graphics, width, height, poseStack, font, currentAmmoCountText, ammoCountColor);
        renderInventoryAmmo(graphics, width, height, poseStack, font);

        RenderSystem.enableDepthTest();
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();

        renderFireModeIcon(graphics, width, height, stack, font, currentAmmoCountText);
        renderAmmoTypeIcon(graphics, width, height, stack, font, currentAmmoCountText);
    }

    private static void renderCurrentAmmo(GuiGraphics graphics, int width, int height, PoseStack poseStack, Font font, String currentAmmoCountText, int ammoCountColor) {
        poseStack.pushPose();
        {
            poseStack.scale(1.5f, 1.5f, 1);
            graphics.drawString(font, currentAmmoCountText,
                    (width - 70) / 1.5f,
                    (height - 43) / 1.5f,
                    ammoCountColor, true);
        }
        poseStack.popPose();
    }

    private static void renderInventoryAmmo(GuiGraphics graphics, int width, int height, PoseStack poseStack, Font font) {
        poseStack.pushPose();
        {
            poseStack.scale(COUNTER_SCALE, COUNTER_SCALE, 1);
            String inventoryAmmoCountText = INVENTORY_AMMO_FORMAT.format(cacheInventoryAmmoCount);

            int inventoryAmmoCountColor = 0xAAAAAA;

            graphics.drawString(font, inventoryAmmoCountText,
                    (width  - 67) / COUNTER_SCALE,
                    (height - 26 ) / COUNTER_SCALE,
                    inventoryAmmoCountColor, true);
        }
        poseStack.popPose();
    }

    private static void renderFireModeIcon(GuiGraphics graphics, int width, int height, ItemStack stack, Font font, String currentAmmoCountText) {
        var fireMode = GunModifierHelper.getCurrentFireMode(stack);
        var icon = fireMode.getIcon();

        var x = (int)(width - ICON_X + font.width(currentAmmoCountText) * 1.5);

        RenderSystem.setShaderColor(1, 1, 1, 1);
        graphics.blit(icon,
                x           + InputEvents.X,
                height - 46 + InputEvents.Y,
                0, 0,
                16, 16,
                16, 16);
    }


    private static void renderAmmoTypeIcon(GuiGraphics graphics, int width, int height, ItemStack stack, Font font, String currentAmmoCountText) {
        var ammoType = GunModifierHelper.getCurrentAmmoType(stack);
        var icon = ammoType.getIcon();

        var x = (int)(width - ICON_X + font.width(currentAmmoCountText) * 1.5);

        RenderSystem.setShaderColor(1, 1, 1, 1);
        graphics.blit(icon,
                x           + InputEvents.X,
                height - 32 + InputEvents.Z,
                0, 0,
                16, 16,
                16, 16);
    }

    private static void handleCacheCount(LocalPlayer player, ItemStack stack) {
        if ((System.currentTimeMillis() - checkAmmoTimestamp) > 200) {
            checkAmmoTimestamp = System.currentTimeMillis();
            cacheMaxAmmoCount = GunModifierHelper.getMaxAmmo(stack);

            if (!player.isCreative()) {
                handleInventoryAmmo(stack, player.getInventory());
            } else {
                cacheInventoryAmmoCount = 9999;
            }
        }
    }

    private static void handleInventoryAmmo(ItemStack stack, Inventory inventory) {
        cacheInventoryAmmoCount = 0;
        for (int i = 0; i < inventory.getContainerSize(); i++) {
            var inventoryItem = inventory.getItem(i);
            if (inventoryItem.getItem() instanceof AmmoItem iAmmo &&
                    GunModifierHelper.getAmmoItem(stack).equals(ForgeRegistries.ITEMS.getKey(iAmmo))) {
                cacheInventoryAmmoCount += inventoryItem.getCount();
            }
//            if (inventoryItem.getItem() instanceof AmmoBoxItem iAmmoBox && iAmmoBox.isAmmoBoxOfGun(stack, inventoryItem)) {
//                if (iAmmoBox.isAllTypeCreative(inventoryItem) || iAmmoBox.isCreative(inventoryItem)) {
//                    cacheInventoryAmmoCount = 9999;
//                    return;
//                }
//                cacheInventoryAmmoCount += iAmmoBox.getAmmoCount(inventoryItem);
//            }
        }
    }
}
