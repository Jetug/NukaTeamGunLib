package com.nukateam.ntgl.client.render.hud;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.nukateam.ntgl.client.event.InputEvents;
import com.nukateam.ntgl.common.data.config.gun.Gun;
import com.nukateam.ntgl.common.foundation.item.AmmoBoxItem;
import com.nukateam.ntgl.common.util.util.GunData;
import com.nukateam.ntgl.common.util.util.GunModifierHelper;
import com.nukateam.ntgl.common.foundation.item.GunItem;
import com.nukateam.ntgl.common.foundation.item.interfaces.IAmmo;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.client.gui.overlay.ForgeGui;
import net.minecraftforge.client.gui.overlay.IGuiOverlay;
import net.minecraftforge.registries.ForgeRegistries;

import java.text.DecimalFormat;
import java.util.Map;

public class GunHud implements IGuiOverlay {
    private static final DecimalFormat CURRENT_AMMO_FORMAT = new DecimalFormat("000");
    private static final DecimalFormat INVENTORY_AMMO_FORMAT = new DecimalFormat("0000");
    public static final float COUNTER_SCALE = 0.9f;
    public static final int DEFAULT_AMMO_COLOR = 0xFFFFFF;
    public static final int LOW_AMMO_COLOR = 0xFF5555;
    public static final int ICON_X = 115;
    public static final int OFFHAND_X_OFFSET = 110;
    private static final Map<InteractionHand, GunHudCache> cache = Map.of(
            InteractionHand.MAIN_HAND, new GunHudCache(InteractionHand.MAIN_HAND),
            InteractionHand.OFF_HAND, new GunHudCache(InteractionHand.OFF_HAND)
    );

    public static final IGuiOverlay AMMO_HUD = new GunHud();
    public static int hudColor = DEFAULT_AMMO_COLOR;

    public static void setHudColor(int hudColor) {
        GunHud.hudColor = hudColor;
    }

    @Override
    public void render(ForgeGui gui, GuiGraphics graphics, float partialTick, int width, int height) {
        var minecraft = Minecraft.getInstance();

        if (minecraft.player == null) return;
        var player = minecraft.player;
        var mainHandItem = player.getMainHandItem();
        var offhandItem = player.getOffhandItem();
        var mainHandCache = cache.get(InteractionHand.MAIN_HAND);
        var offhandCache = cache.get(InteractionHand.OFF_HAND);

        if (mainHandItem.getItem() instanceof GunItem) {
            updateCache(mainHandCache, player, mainHandItem);
            renderAmmoCounter(graphics, mainHandCache, mainHandItem, width + InputEvents.X, height + InputEvents.Y);
        }

        if (offhandItem.getItem() instanceof GunItem) {
            updateCache(offhandCache, player, offhandItem);
            renderAmmoCounter(graphics, offhandCache, offhandItem, OFFHAND_X_OFFSET - InputEvents.X, height - InputEvents.Y);
        }
    }

    private static void renderAmmoCounter(GuiGraphics graphics, GunHudCache handCache, ItemStack stack, int width, int height) {
        var mc = Minecraft.getInstance();
        if(!GunModifierHelper.shouldRenderHud(new GunData(stack, mc.player))) return;

        int ammoCountColor = handCache.ammoCount < (handCache.maxAmmoCount * 0.25) ? LOW_AMMO_COLOR : hudColor;
        var currentAmmoCountText = CURRENT_AMMO_FORMAT.format(handCache.ammoCount);

        drawLine(graphics, width - 70, height - 30, 27, 2);

        var poseStack = graphics.pose();
        var font = mc.font;

        renderCurrentAmmo(graphics, handCache, width, height, poseStack, font, currentAmmoCountText, ammoCountColor);
        renderInventoryAmmo(graphics, handCache, width, height, poseStack, font);

        RenderSystem.enableDepthTest();
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();

        renderFireModeIcon(graphics, handCache, width, height, font, currentAmmoCountText);
        renderAmmoTypeIcon(graphics, handCache, width, height, font, currentAmmoCountText);
    }

    private static void drawLine(GuiGraphics graphics, int x, int y, int width, int height) {
        graphics.fill(x, y, x + width, y + height, 0xFFFFFFFF);
    }

    private static void renderCurrentAmmo(GuiGraphics graphics, GunHudCache handCache, int width, int height, PoseStack poseStack, Font font, String currentAmmoCountText, int ammoCountColor) {
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

    private static void renderInventoryAmmo(GuiGraphics graphics, GunHudCache handCache, int width, int height, PoseStack poseStack, Font font) {
        poseStack.pushPose();
        {
            poseStack.scale(COUNTER_SCALE, COUNTER_SCALE, 1);
            var inventoryAmmoCountText = INVENTORY_AMMO_FORMAT.format(handCache.inventoryAmmoCount);
            int inventoryAmmoCountColor = 0xAAAAAA;

            graphics.drawString(font, inventoryAmmoCountText,
                    (width - 67) / COUNTER_SCALE,
                    (height - 26) / COUNTER_SCALE,
                    inventoryAmmoCountColor, true);
        }
        poseStack.popPose();
    }

    private static void renderFireModeIcon(GuiGraphics graphics, GunHudCache handCache, int width, int height,
                                           Font font, String currentAmmoCountText) {
        var fireMode = handCache.fireMode;
        var icon = fireMode.getIcon();
        var textWidth = font.width(currentAmmoCountText) * 1.5;
        var x = (int) (width - getIconX(handCache, textWidth) + textWidth);

        RenderSystem.setShaderColor(1, 1, 1, 1);
        graphics.blit(icon,
                x,
                height - 46,
                0, 0,
                16, 16,
                16, 16);
    }

    private static void renderAmmoTypeIcon(GuiGraphics graphics, GunHudCache handCache, int width, int height, Font font, String currentAmmoCountText) {
        var ammoType = handCache.ammoType;
        var icon = ammoType.getIcon();
        var textWidth =  font.width(currentAmmoCountText) * 1.5;
        var x = (int) (width - getIconX(handCache, textWidth) + textWidth);

        RenderSystem.setShaderColor(1, 1, 1, 1);
        graphics.blit(icon,
                x,
                height - 32,
                0, 0,
                16, 16,
                16, 16);
    }

    private static int getIconX(GunHudCache handCache, double textWidth) {
        if (handCache.hand == InteractionHand.OFF_HAND){
            return ICON_X - (int)textWidth - 20;
        }
        return ICON_X;
    }

    private static void updateCache(GunHudCache handCache, LocalPlayer player, ItemStack stack) {
        if ((System.currentTimeMillis() - handCache.checkAmmoTimestamp) > 200) {
            var data = new GunData(stack, player);
            handCache.checkAmmoTimestamp = System.currentTimeMillis();
            handCache.maxAmmoCount = GunModifierHelper.getMaxAmmo(data);
            handCache.fireMode = GunModifierHelper.getCurrentFireMode(data);
            handCache.ammoType = GunModifierHelper.getCurrentAmmoType(data);
            handCache.ammoCount = Gun.getAmmo(stack);

            if (!player.isCreative()) {
                handCache.inventoryAmmoCount = getInventoryAmmoCount(stack, player.getInventory());
            } else {
                handCache.inventoryAmmoCount = 9999;
            }
        }
    }

    private static int getInventoryAmmoCount(ItemStack stack, Inventory inventory) {
        var inventoryAmmoCount = 0;
        for (int i = 0; i < inventory.getContainerSize(); i++) {
            var gunData = new GunData(stack, Minecraft.getInstance().player);
            var inventoryStack = inventory.getItem(i);
            var inventoryItem = inventoryStack.getItem();

            if (GunModifierHelper.isCurrentAmmo(gunData, inventoryItem)) {
                inventoryAmmoCount += inventoryStack.getCount();
            }
            else if (inventoryItem instanceof AmmoBoxItem iAmmoBox) {
                var currentAmmo = GunModifierHelper.getCurrentAmmoItem(gunData);
                inventoryAmmoCount += iAmmoBox.getAmmoCount(inventoryStack, currentAmmo);
            }
        }
        return inventoryAmmoCount;
    }
}
