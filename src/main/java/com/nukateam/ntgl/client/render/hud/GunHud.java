package com.nukateam.ntgl.client.render.hud;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.nukateam.ntgl.client.event.*;
import com.nukateam.ntgl.client.render.hud.cache.GunHudCache;
import com.nukateam.ntgl.client.util.ClientDebug;
import com.nukateam.ntgl.client.util.util.RgbUtils;
import com.nukateam.ntgl.client.util.util.render.Figures;
import com.nukateam.ntgl.common.data.config.gun.WeaponConfig;
import com.nukateam.ntgl.common.data.holders.WeaponMode;
import com.nukateam.ntgl.common.data.holders.CounterType;
import com.nukateam.ntgl.common.foundation.item.interfaces.IWeapon;
import com.nukateam.ntgl.common.util.util.FuelUtils;
import com.nukateam.ntgl.common.data.WeaponData;
import com.nukateam.ntgl.common.util.util.*;
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
import net.minecraftforge.common.MinecraftForge;

import java.text.DecimalFormat;
import java.util.Map;

public class GunHud implements IGuiOverlay {
    public static final float COUNTER_SCALE = 0.9f;
    public static final int INVENTORY_AMMO_COUNT_COLOR = 0xAAAAAA;
    public static final int DEFAULT_AMMO_COLOR = 0xFFFFFF;
    public static final int LOW_AMMO_COLOR = 0xFF5555;
    public static final Colors DEFAULT_COLORS = new Colors(DEFAULT_AMMO_COLOR, INVENTORY_AMMO_COUNT_COLOR, DEFAULT_AMMO_COLOR, LOW_AMMO_COLOR);
    protected static final DecimalFormat CURRENT_AMMO_FORMAT = new DecimalFormat("000");
    protected static final DecimalFormat CURRENT_AMMO_FORMAT_PERCENT = new DecimalFormat("000%");
    protected static final DecimalFormat INVENTORY_AMMO_FORMAT = new DecimalFormat("0000");
    private static final int ICON_X = 115;
    private static final int OFFHAND_X_OFFSET = 110;
    private static final int BAR_WIDTH = 35;
    private static final int BAR_HEIGHT = 6;

    public static final int COUNTER_POS_X = 70;
    public static final int ICON_SIZE = 16;
    public static final int COUNTER_POS_Y = 36;

    private static final int BAR_START_X = COUNTER_POS_X;
    private static final int BAR_START_Y = 57;
    protected static final Map<InteractionHand, GunHudCache> cache = Map.of(
            InteractionHand.MAIN_HAND, new GunHudCache(InteractionHand.MAIN_HAND),
            InteractionHand.OFF_HAND, new GunHudCache(InteractionHand.OFF_HAND)
    );
    public static final int INVENTORY_AMMO_POS_Y = 25;

    protected final Minecraft minecraft = Minecraft.getInstance();
    private Colors colors = DEFAULT_COLORS;
    public static final IGuiOverlay AMMO_HUD = new GunHud();

    public void setHudColor(Colors hudColor) {
        colors = hudColor;
    }

    public void resetHudColor() {
        colors = DEFAULT_COLORS;
    }

    @Override
    public void render(ForgeGui gui, GuiGraphics graphics, float partialTick, int width, int height) {
        if (minecraft.player == null) return;
        var player = minecraft.player;

        cache.forEach((hand, cache) -> {
            var heldItem = player.getItemInHand(hand);
            var x = hand == InteractionHand.OFF_HAND ? OFFHAND_X_OFFSET : width;

            if (heldItem.getItem() instanceof IWeapon && shouldRender(hand, player)) {
                updateCache(cache, player, heldItem);
                if (!MinecraftForge.EVENT_BUS.post(new GunHudEvent(this, hand, graphics, cache, GunHudEvent.Phase.START))) {
                    renderAmmoCounter(graphics, cache, heldItem, x, height);
                    MinecraftForge.EVENT_BUS.post(new GunHudEvent(this, hand, graphics, cache, GunHudEvent.Phase.END));
                }
            }
        });
    }

    private static boolean shouldRender(InteractionHand hand, LocalPlayer player) {
        return hand == InteractionHand.MAIN_HAND || WeaponModifierHelper.canUseOffhandWeapon(player);
    }

    protected void renderAmmoCounter(GuiGraphics graphics, GunHudCache handCache, ItemStack stack, int x, int y) {
        if(!WeaponModifierHelper.shouldRenderHud(new WeaponData(stack, minecraft.player))) return;


        var poseStack = graphics.pose();
        poseStack.pushPose();
        {
            RenderSystem.enableDepthTest();
            RenderSystem.setShader(GameRenderer::getPositionTexShader);
            RenderSystem.enableBlend();
            RenderSystem.defaultBlendFunc();


            var fontHeight = minecraft.font.lineHeight;

            renderAmmoTypeIcon(graphics, handCache, x - COUNTER_POS_X - ICON_SIZE - 2, y - COUNTER_POS_Y - 11);
            renderCurrentAmmo(graphics,  handCache, x - COUNTER_POS_X, y - COUNTER_POS_Y - fontHeight, poseStack);

            Figures.drawLine(graphics, x - COUNTER_POS_X, y - 31, 27, 2, RgbUtils.toRgba(colors.hud));

            if(isThrowable(stack))
                renderThrowModeIcon(graphics, handCache, x - COUNTER_POS_X - ICON_SIZE - 2 , y - INVENTORY_AMMO_POS_Y - 6);
            else renderFireModeIcon(graphics, handCache, x - COUNTER_POS_X - ICON_SIZE - 2 , y - INVENTORY_AMMO_POS_Y - 6);
            renderInventoryAmmo(graphics, handCache, x - COUNTER_POS_X + 3, y - INVENTORY_AMMO_POS_Y, poseStack, minecraft.font);

            renderFuelCounters(graphics, handCache, stack, x - BAR_START_X + 8 + ClientDebug.X, y - BAR_START_Y - 3 + ClientDebug.Y);
        }
        poseStack.popPose();
    }

    protected void renderCurrentAmmo(GuiGraphics graphics, GunHudCache handCache,
                                     int x, int y,
                                     PoseStack poseStack) {
        var currentAmmoCountText = "";
        if(handCache.ammoConfig.getCounter() == CounterType.NUMBER) {
            currentAmmoCountText = CURRENT_AMMO_FORMAT.format(handCache.ammoCount);
            renderCounter(graphics, handCache, x, y, poseStack, currentAmmoCountText);
        }
        else if(handCache.ammoConfig.getCounter() == CounterType.PERCENT){
            var percent = (((float)handCache.ammoCount / (float)handCache.maxAmmoCount));
            currentAmmoCountText = CURRENT_AMMO_FORMAT_PERCENT.format(percent);
            renderCounter(graphics, handCache, x, y, poseStack, currentAmmoCountText);
        }
        else if(handCache.ammoConfig.getCounter() == CounterType.BAR){
            var percent = (((float)handCache.ammoCount / (float)handCache.maxAmmoCount));
            renderBarCounter(graphics, percent, x, y);
        }
    }

    protected void renderFuelCounters(GuiGraphics graphics, GunHudCache handCache, ItemStack stack, int x, int y) {
        var barOffsetY = 0;

        for (var entry : handCache.fuels.entrySet()) {
            var gunData = new WeaponData(stack, minecraft.player);
            var fuelPercent = FuelUtils.getFuelPercent(stack, entry.getKey(), gunData);

            renderIcon(graphics, entry.getValue().getAmmo().getAmmoType().getIcon(), x - ICON_SIZE - 2, y - 5 - barOffsetY);
            renderBarCounter(graphics, fuelPercent, x, y - barOffsetY);
            barOffsetY += 16;
        }
    }

    private void renderCounter(GuiGraphics graphics, GunHudCache handCache, int x, int y, PoseStack poseStack, String currentAmmoCountText) {
        var ammoCountColor = handCache.ammoCount < (handCache.maxAmmoCount * 0.25) ? colors.lowAmmo : colors.currentAmmo;
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

    protected void renderBarCounter(GuiGraphics graphics, float percent, int x, int y) {
        var color = percent < 0.25 ? colors.lowAmmo : colors.currentAmmo;
        Figures.drawBar(graphics, x, y, BAR_WIDTH, BAR_HEIGHT, percent, RgbUtils.toRgba(color));
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
                    colors.inventoryAmmo, true);
        }
        poseStack.popPose();
    }

    protected void renderAmmoTypeIcon(GuiGraphics graphics, GunHudCache handCache, int x, int y) {
        var ammoType = handCache.ammoConfig.getAmmoType();
        var icon = ammoType.getIcon();
//        var textWidth = minecraft.font.width(currentAmmoCountText) * 1.5;
//        var x = (int) (width - getIconX(handCache, textWidth) + textWidth);

        renderIcon(graphics, icon, x, y);
    }

    protected void renderThrowModeIcon(GuiGraphics graphics, GunHudCache handCache, int x, int y) {
        var mode = handCache.throwMode;
        var icon = mode.getIcon();

        renderIcon(graphics, icon, x, y);
    }

    protected void renderFireModeIcon(GuiGraphics graphics, GunHudCache handCache, int x, int y) {
        var fireMode = handCache.fireMode;
        var icon = fireMode.getIcon();

        renderIcon(graphics, icon, x, y);
    }

    protected void renderIcon(GuiGraphics graphics, ResourceLocation icon, int x, int y) {
        var iconColor = RgbUtils.rgbToFloatRgba(colors.hud);
        RenderSystem.setShaderColor(iconColor[0], iconColor[1], iconColor[2], iconColor[3]);
        graphics.blit(icon, x, y, 0F, 0F, ICON_SIZE, ICON_SIZE, ICON_SIZE, ICON_SIZE);
        RenderSystem.setShaderColor(1, 1, 1, 1);
    }

    protected int getIconX(GunHudCache handCache, double textWidth) {
        if (handCache.hand == InteractionHand.OFF_HAND){
            return ICON_X - (int)textWidth - 20;
        }
        return ICON_X;
    }

    protected boolean isThrowable(ItemStack stack){
        return WeaponModifierHelper.getConfig(stack).getGeneral().getWeaponMode() == WeaponMode.THROWABLE;
    }

    protected WeaponConfig getConfig(ItemStack stack){
        var weapon = (IWeapon)stack.getItem();
        return weapon.getModifiedConfig(stack);
    }

    protected void updateCache(GunHudCache handCache, LocalPlayer player, ItemStack weapon) {
        if ((System.currentTimeMillis() - handCache.checkAmmoTimestamp) > 200) {
            var data = new WeaponData(weapon, player);
            handCache.checkAmmoTimestamp = System.currentTimeMillis();
            handCache.maxAmmoCount = WeaponModifierHelper.getMaxAmmo(data);
            handCache.fireMode = WeaponStateHelper.getFireMode(data);

            if(isThrowable(weapon)){
                handCache.throwMode = ThrowableStateHelper.getThrowMode(weapon);
                handCache.ammoCount = weapon.getCount();
                handCache.ammoConfig = WeaponModifierHelper.getConfig(weapon).getThrowable().getAmmo();
            }
            else {
                handCache.ammoCount = WeaponStateHelper.getAmmoCount(data);
                handCache.ammoConfig = WeaponModifierHelper.getConfig(weapon).getAmmoConfig(WeaponStateHelper.getCurrentAmmo(data).getId());
            }
            var fuels = WeaponModifierHelper.getAllFuel(data);
            handCache.fuels.clear();
            for (var id : fuels) {
                var value = WeaponModifierHelper.getFuel(id.getId(), data);
                handCache.fuels.put(id, value);
            }

            if (!player.isCreative()) {
                if(isThrowable(weapon))
                    handCache.inventoryAmmoCount = getInventoryThrowableCount(weapon, player.getInventory());
                else handCache.inventoryAmmoCount = getInventoryAmmoCount(weapon, player.getInventory());
            } else {
                handCache.inventoryAmmoCount = 9999;
            }
        }
    }

    protected int getInventoryAmmoCount(ItemStack weapon, Inventory inventory) {
        var inventoryAmmoCount = 0;
        var gunData = new WeaponData(weapon, minecraft.player);
        var ammoHolder = WeaponStateHelper.getCurrentAmmo(gunData);

        for (int i = 0; i < inventory.getContainerSize(); i++) {
            var inventoryStack = inventory.getItem(i);

            if (ammoHolder.isAcceptable(inventoryStack)) {
                inventoryAmmoCount += inventoryStack.getCount() * ammoHolder.getValue(inventoryStack);
            }
        }
        return inventoryAmmoCount;
    }

    protected int getInventoryThrowableCount(ItemStack stack, Inventory inventory) {
        var inventoryAmmoCount = 0;
        for (int i = 0; i < inventory.getContainerSize(); i++) {
            var inventoryStack = inventory.getItem(i);
            var inventoryItem = inventoryStack.getItem();

            if (inventoryItem == stack.getItem() && inventoryStack != stack) {
                inventoryAmmoCount += inventoryStack.getCount();
            }
        }
        return inventoryAmmoCount;
    }

    public record Colors(int hud, int inventoryAmmo, int currentAmmo, int lowAmmo){}
}
