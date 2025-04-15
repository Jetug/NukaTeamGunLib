package com.nukateam.ntgl.client.render.hud;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.nukateam.ntgl.client.util.util.render.Figures;
import com.nukateam.ntgl.common.base.holders.FuelType;
import com.nukateam.ntgl.common.base.utils.FuelUtils;
import com.nukateam.ntgl.common.data.config.gun.Gun;
import com.nukateam.ntgl.common.foundation.item.AmmoBoxItem;
import com.nukateam.ntgl.common.util.util.GunData;
import com.nukateam.ntgl.common.util.util.GunModifierHelper;
import com.nukateam.ntgl.common.foundation.item.GunItem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.client.gui.overlay.ForgeGui;
import net.minecraftforge.client.gui.overlay.IGuiOverlay;

import java.text.DecimalFormat;
import java.util.Map;

public class GunHud implements IGuiOverlay {
    public static final float COUNTER_SCALE = 0.9f;
    public static final int DEFAULT_AMMO_COLOR = 0xFFFFFF;
    public static final int LOW_AMMO_COLOR = 0xFF5555;
    public static final int ICON_X = 115;
    public static final int OFFHAND_X_OFFSET = 110;
    public static final int BAR_WIDTH = 35;
    public static final int BAR_HEIGHT = 6;
    public static final int BAR_START_X = 70;
    public static final int BAR_START_Y = 57;
    public static final int INVENTORY_AMMO_COUNT_COLOR = 0xAAAAAA;
    protected Minecraft minecraft = Minecraft.getInstance();

    protected static final DecimalFormat CURRENT_AMMO_FORMAT = new DecimalFormat("000");
    protected static final DecimalFormat INVENTORY_AMMO_FORMAT = new DecimalFormat("0000");
    protected static final Map<InteractionHand, GunHudCache> cache = Map.of(
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
        if (minecraft.player == null) return;
        var player = minecraft.player;

        cache.forEach((hand, cache) -> {
            var heldItem = player.getItemInHand(hand);
            var x = hand == InteractionHand.OFF_HAND ? OFFHAND_X_OFFSET : width;
            if(heldItem.getItem() instanceof GunItem) {
                updateCache(cache, player, heldItem);
                renderAmmoCounter(graphics, cache, heldItem, x, height);
            }
        });
    }

    protected void renderAmmoCounter(GuiGraphics graphics, GunHudCache handCache, ItemStack stack, int x, int y) {
        if(!GunModifierHelper.shouldRenderHud(new GunData(stack, minecraft.player))) return;
        var currentAmmoCountText = CURRENT_AMMO_FORMAT.format(handCache.ammoCount);
        var poseStack = graphics.pose();

        renderCurrentAmmo(graphics, handCache, x - 70, y - 43, poseStack, currentAmmoCountText);
        Figures.drawLine(graphics, x - 70, y - 30, 27, 2);
        renderInventoryAmmo(graphics, handCache, x - 67, y - 26, poseStack, minecraft.font);

        RenderSystem.enableDepthTest();
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();

        renderFireModeIcon(graphics, handCache, x, y, currentAmmoCountText);
        renderAmmoTypeIcon(graphics, handCache, x, y, currentAmmoCountText);
        renderFuelCounters(graphics, stack, x - BAR_START_X, y - BAR_START_Y);
    }

    protected void renderFuelCounters(GuiGraphics graphics, ItemStack stack, int x, int y) {
        var gunData = new GunData(stack, minecraft.player);
        var allFuel = GunModifierHelper.getFuelTypes(gunData);
        var barOffsetY = 0;

        for (var fuelType : allFuel) {
            renderFuelCounter(graphics, stack, fuelType, x, y - barOffsetY);
            barOffsetY += 18;
        }
    }

    protected void renderFuelCounter(GuiGraphics graphics, ItemStack stack, FuelType fuelType, int x, int y) {
        var gunData = new GunData(stack, minecraft.player);
        var fuelPercent = FuelUtils.getFuelPercent(stack, fuelType, gunData);
        renderIcon(graphics, fuelType.getIcon(), x - 18, y - 4);
        Figures.drawBar(graphics, x, y, BAR_WIDTH, BAR_HEIGHT, fuelPercent);
    }

    protected void renderCurrentAmmo(GuiGraphics graphics, GunHudCache handCache,
                                     int x, int y,
                                     PoseStack poseStack,
                                     String currentAmmoCountText) {
        var ammoCountColor = handCache.ammoCount < (handCache.maxAmmoCount * 0.25) ? LOW_AMMO_COLOR : hudColor;
        poseStack.pushPose();
        {
            var scale = 1.5f;
            poseStack.scale(scale, scale, 1);
            graphics.drawString(minecraft.font, currentAmmoCountText,
                    x / scale,
                    y / scale,
                    ammoCountColor, true);
        }
        poseStack.popPose();
    }

    protected void renderInventoryAmmo(GuiGraphics graphics, GunHudCache handCache, int x, int y,
                                       PoseStack poseStack, Font font) {
        var inventoryAmmoCountText = INVENTORY_AMMO_FORMAT.format(handCache.inventoryAmmoCount);
        poseStack.pushPose();
        {
            poseStack.scale(COUNTER_SCALE, COUNTER_SCALE, 1);
            graphics.drawString(font, inventoryAmmoCountText,
                    x / COUNTER_SCALE,
                    y / COUNTER_SCALE,
                    INVENTORY_AMMO_COUNT_COLOR, true);
        }
        poseStack.popPose();
    }

    protected void renderFireModeIcon(GuiGraphics graphics, GunHudCache handCache, int width, int height,
                                      String currentAmmoCountText) {
        var fireMode = handCache.fireMode;
        var icon = fireMode.getIcon();
        var textWidth = minecraft.font.width(currentAmmoCountText) * 1.5;
        var x = (int) (width - getIconX(handCache, textWidth) + textWidth);

        RenderSystem.setShaderColor(1, 1, 1, 1);
        renderIcon(graphics, icon, x, height - 46);
    }

    protected void renderAmmoTypeIcon(GuiGraphics graphics, GunHudCache handCache, int width, int height, String currentAmmoCountText) {
        var ammoType = handCache.ammoType;
        var icon = ammoType.getIcon();
        var textWidth =  minecraft.font.width(currentAmmoCountText) * 1.5;
        var x = (int) (width - getIconX(handCache, textWidth) + textWidth);

        RenderSystem.setShaderColor(1, 1, 1, 1);
        renderIcon(graphics, icon, x, height - 32);
    }

    protected void renderIcon(GuiGraphics graphics, ResourceLocation icon, int x, int y) {
        graphics.blit(icon, x, y, 0F, 0F, 16, 16, 16, 16);
    }

    protected int getIconX(GunHudCache handCache, double textWidth) {
        if (handCache.hand == InteractionHand.OFF_HAND){
            return ICON_X - (int)textWidth - 20;
        }
        return ICON_X;
    }

    protected void updateCache(GunHudCache handCache, LocalPlayer player, ItemStack stack) {
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

    protected int getInventoryAmmoCount(ItemStack stack, Inventory inventory) {
        var inventoryAmmoCount = 0;
        for (int i = 0; i < inventory.getContainerSize(); i++) {
            var gunData = new GunData(stack, minecraft.player);
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
