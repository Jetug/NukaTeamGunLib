package com.nukateam.ntgl.client.render.hud;

import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.nukateam.ntgl.client.event.*;
import com.nukateam.ntgl.client.input.NtglKeyBinds;
import com.nukateam.ntgl.client.input.WeaponModeBindings;
import com.nukateam.ntgl.client.render.hud.cache.GunHudCache;
import com.nukateam.ntgl.client.settings.NtglOptions;
import com.nukateam.ntgl.client.util.ClientDebug;
import com.nukateam.ntgl.client.util.helpers.KeyIcons;
import com.nukateam.ntgl.client.util.helpers.RgbHelper;
import com.nukateam.ntgl.client.util.helpers.render.Figures;
import com.nukateam.ntgl.common.data.config.weapon.WeaponConfig;
import com.nukateam.ntgl.common.data.holders.WeaponAction;
import com.nukateam.ntgl.common.data.holders.CounterType;
import com.nukateam.ntgl.common.data.holders.WeaponMode;
import com.nukateam.ntgl.common.foundation.item.interfaces.IWeapon;
import com.nukateam.ntgl.common.util.util.FuelUtils;
import com.nukateam.ntgl.common.data.WeaponData;
import com.nukateam.ntgl.common.util.util.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.client.gui.overlay.ForgeGui;
import net.minecraftforge.client.gui.overlay.IGuiOverlay;
import net.minecraftforge.common.MinecraftForge;

import java.text.DecimalFormat;
import java.util.LinkedHashMap;
import java.util.Map;

public class WeaponHud implements IGuiOverlay {
    public static final float COUNTER_SCALE = 0.9f;
    public static final float BINDING_SCALE = 0.6f;
    public static final float WEAPON_MODE_SCALE = 0.7f;
    public static final int INVENTORY_AMMO_COUNT_COLOR = 0xAAAAAA;
    public static final int DEFAULT_AMMO_COLOR = 0xFFFFFF;
    public static final int LOW_AMMO_COLOR = 0xFF5555;
    public static final Colors DEFAULT_COLORS = new Colors(DEFAULT_AMMO_COLOR, INVENTORY_AMMO_COUNT_COLOR, DEFAULT_AMMO_COLOR, LOW_AMMO_COLOR);
    protected static final DecimalFormat CURRENT_AMMO_FORMAT = new DecimalFormat("000");
    protected static final DecimalFormat CURRENT_AMMO_FORMAT_PERCENT = new DecimalFormat("000%");
    protected static final DecimalFormat INVENTORY_AMMO_FORMAT = new DecimalFormat("0000");
    private static final int OFFHAND_X_OFFSET = 110;
    private static final int BAR_WIDTH = 35;
    private static final int BAR_HEIGHT = 6;
    private static final int COUNTER_POS_X = 70;
    private static final int COUNTER_POS_Y = 36;
    private static final int ICON_SIZE = 16;
    private static final int BAR_START_X = COUNTER_POS_X;
    private static final int BAR_START_Y = 57;
    protected static final Map<InteractionHand, GunHudCache> cache = Map.of(
            InteractionHand.MAIN_HAND, new GunHudCache(InteractionHand.MAIN_HAND),
            InteractionHand.OFF_HAND, new GunHudCache(InteractionHand.OFF_HAND)
    );
    public static final int INVENTORY_AMMO_POS_Y = 25;

    protected final Minecraft minecraft = Minecraft.getInstance();
    private Colors colors = DEFAULT_COLORS;
    public static final IGuiOverlay AMMO_HUD = new WeaponHud();

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
                    renderAmmoInfo(graphics, cache, heldItem, x, height);
                    MinecraftForge.EVENT_BUS.post(new GunHudEvent(this, hand, graphics, cache, GunHudEvent.Phase.END));
                }
            }
        });
    }

    public void setHudColor(Colors hudColor) {
        colors = hudColor;
    }

    public void resetHudColor() {
        colors = DEFAULT_COLORS;
    }

    protected void renderAmmoInfo(GuiGraphics graphics, GunHudCache handCache, ItemStack stack, int x, int y) {
        if(!WeaponModifierHelper.shouldRenderHud(new WeaponData(stack, minecraft.player))) return;

        var poseStack = graphics.pose();
        poseStack.pushPose();
        {
            RenderSystem.enableDepthTest();
            RenderSystem.setShader(GameRenderer::getPositionTexShader);
            RenderSystem.enableBlend();
            RenderSystem.defaultBlendFunc();

            var fontHeight = minecraft.font.lineHeight;

            renderAmmoTypeIcon(graphics, poseStack, handCache, WeaponMode.PRIMARY, x - COUNTER_POS_X - ICON_SIZE - 2, y - COUNTER_POS_Y - 11);
            if(handCache.ammoTypeKey) {
                renderKey(graphics, poseStack, NtglKeyBinds.KEY_AMMO_SELECT.getKey(), x - 6, y + 6);
            }
            renderCurrentAmmo (graphics, poseStack, handCache.weaponModes.get(WeaponMode.PRIMARY), x - COUNTER_POS_X, y - COUNTER_POS_Y - fontHeight);

            Figures.drawLine(graphics, x - COUNTER_POS_X, y - 31, 27, 2, RgbHelper.toRgba(colors.hud));

            if(handCache.isThrowable)
                renderThrowModeIcon(graphics, poseStack, handCache, x - COUNTER_POS_X - ICON_SIZE - 2 , y - INVENTORY_AMMO_POS_Y - 6);
            else renderFireModeIcon(graphics, poseStack, handCache,  x - COUNTER_POS_X - ICON_SIZE - 2 , y - INVENTORY_AMMO_POS_Y - 6);
            renderInventoryAmmo(graphics, poseStack, handCache.weaponModes.get(WeaponMode.PRIMARY), x - COUNTER_POS_X + 3, y - INVENTORY_AMMO_POS_Y);

            renderFuelCounters(graphics, handCache, stack, x - BAR_START_X + 8, y - BAR_START_Y - 3 );
            renderWeaponModes(graphics, poseStack, handCache, x - COUNTER_POS_X + 38 , y - INVENTORY_AMMO_POS_Y  - 2);

            drawAltCounters(graphics, handCache, x - COUNTER_POS_X, y - COUNTER_POS_Y - fontHeight + 32, poseStack);


        }
        poseStack.popPose();
    }

    private void drawAltCounters(GuiGraphics graphics, GunHudCache handCache, int x, int y, PoseStack poseStack) {
        poseStack.pushPose();
        {
            var scale = 0.5f;
            poseStack.scale(scale, scale, scale);

            var xOffset = x;
            for (var entry : handCache.weaponModes.entrySet()) {
                var key = entry.getKey();
                var modeInfo = entry.getValue();
                var mode = handCache.weaponModes.get(key);

                if (key != WeaponMode.PRIMARY && modeInfo.maxAmmoCount > 0) {
                    renderAmmoTypeIcon(graphics, poseStack, handCache, key, (int) ((xOffset - 11) / scale), (int) ((y - 2) / scale));
                    Figures.drawFrame(graphics, (int) ((xOffset - 2) / scale), (int) ((y - 2) / scale), 34, 16, RgbHelper.toRgba(colors.hud));
                    renderCurrentAmmo(graphics, poseStack, mode, xOffset / scale, y / scale);

                    renderKey(graphics, poseStack, WeaponModeBindings.getKey(key).getKey(), (int)((xOffset - 14 - ClientDebug.X) / scale), (int) ((y - 2) / scale), false);

                    xOffset += 34;
                }
            }
        }
        poseStack.popPose();
    }

    protected void renderCurrentAmmo(GuiGraphics graphics, PoseStack poseStack, GunHudCache.ModeInfo handCache, float x, float y) {
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
            renderBarCounter(graphics, percent, (int)x, (int)y);
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

    private void renderCounter(GuiGraphics graphics, GunHudCache.ModeInfo handCache, float x, float y, PoseStack poseStack, String currentAmmoCountText) {
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
        Figures.drawBar(graphics, x, y, BAR_WIDTH, BAR_HEIGHT, percent, RgbHelper.toRgba(color));
    }

    protected void renderInventoryAmmo(GuiGraphics graphics, PoseStack poseStack, GunHudCache.ModeInfo handCache, int x, int y) {
        var inventoryAmmoCountText = INVENTORY_AMMO_FORMAT.format(handCache.inventoryAmmoCount);
        poseStack.pushPose();
        {
            poseStack.scale(COUNTER_SCALE, COUNTER_SCALE, 1);
            graphics.drawString(minecraft.font, inventoryAmmoCountText,
                    x / COUNTER_SCALE,
                    y / COUNTER_SCALE,
                    colors.inventoryAmmo, true);
        }
        poseStack.popPose();
    }

    protected void renderWeaponModes(GuiGraphics graphics, PoseStack poseStack, GunHudCache handCache, int x, int y) {
        var iconPosY = y;
        for (var entry : handCache.weaponModes.entrySet()) {
            var mode = entry.getKey();
            var action = entry.getValue().action;
            if(entry.getValue().maxAmmoCount == 0) {
                renderIcon(graphics, poseStack, action.getIcon(), x, iconPosY, WEAPON_MODE_SCALE);
                renderKey(graphics, poseStack, WeaponModeBindings.getKey(mode).getKey(), x + 16, iconPosY + 3, false);
                iconPosY -= 12;
            }
        }
    }

    protected void renderAmmoTypeIcon(GuiGraphics graphics, PoseStack poseStack, GunHudCache handCache, WeaponMode mode, int x, int y) {
        var ammoType = handCache.weaponModes.get(mode).ammoConfig.getAmmoType();
        var icon = ammoType.getIcon();
        renderIcon(graphics, icon, x, y);
    }

    protected void renderThrowModeIcon(GuiGraphics graphics, PoseStack poseStack, GunHudCache handCache, int x, int y) {
        var mode = handCache.throwMode;
        var icon = mode.getIcon();

        renderIcon(graphics, icon, x, y);
        if(handCache.fireModeKey) {
            renderKey(graphics, poseStack, NtglKeyBinds.KEY_FIRE_SELECT.getKey(), x - 6, y + 6);
        }
    }

    protected void renderFireModeIcon(GuiGraphics graphics, PoseStack poseStack, GunHudCache handCache, int x, int y) {
        var fireMode = handCache.fireMode;
        var icon = fireMode.getIcon();

        renderIcon(graphics, icon, x, y);
        if(handCache.fireModeKey) {
            renderKey(graphics, poseStack, NtglKeyBinds.KEY_FIRE_SELECT.getKey(), x - 6, y + 6);
        }
    }

    protected void renderKey(GuiGraphics graphics, PoseStack poseStack, InputConstants.Key key, int x, int y) {
        renderKey(graphics, poseStack, key, x, y, true);
    }

    protected void renderKey(GuiGraphics graphics, PoseStack poseStack, InputConstants.Key key, int x, int y, boolean isLeft) {
        if(!NtglOptions.getInstance().isShowTips()) return;
        var icon = KeyIcons.getIcon(key.getValue());

        if (icon != null) {
            renderIcon(graphics, poseStack, icon, x - 5, y - 3, WEAPON_MODE_SCALE);
        } else {
            var side = isLeft ? 1 : -1;
            renderKeyName(graphics, poseStack, key, x + 3 * side, y, BINDING_SCALE, isLeft);
        }
    }

    private void renderKeyName(GuiGraphics graphics, PoseStack poseStack, InputConstants.Key key, int x, int y, float scale, boolean isLeft) {
        var name = key.getDisplayName().getVisualOrderText();
        poseStack.pushPose();
        {
            var side = isLeft ? -1 : 0;
            var textOffset = minecraft.font.width(name) / 2 * side;
            poseStack.scale(scale, scale, 1);
            graphics.drawString(minecraft.font, name,
                    (x + textOffset) / scale,
                    y / scale,
                    colors.inventoryAmmo, true);
        }
        poseStack.popPose();
    }

    protected void renderIcon(GuiGraphics graphics, PoseStack poseStack, ResourceLocation icon, int x, int y, float scale) {
        poseStack.pushPose();
        {
            poseStack.scale(scale, scale, 1);
            renderIcon(graphics, icon, (int) (x / scale), (int) (y / scale));
        }
        poseStack.popPose();
    }

    protected void renderIcon(GuiGraphics graphics, ResourceLocation icon, int x, int y) {
        var iconColor = RgbHelper.rgbToFloatRgba(colors.hud);
        RenderSystem.setShaderTexture(0, icon);
        RenderSystem.setShaderColor(iconColor[0], iconColor[1], iconColor[2], iconColor[3]);
        graphics.blit(icon, x, y, 0F, 0F, ICON_SIZE, ICON_SIZE, ICON_SIZE, ICON_SIZE);
        RenderSystem.setShaderColor(1, 1, 1, 1);
    }

    protected WeaponConfig getConfig(ItemStack stack){
        var weapon = (IWeapon)stack.getItem();
        return weapon.getModifiedConfig(stack);
    }

    protected void updateCache(GunHudCache handCache, LocalPlayer player, ItemStack weapon) {
        if ((System.currentTimeMillis() - handCache.checkAmmoTimestamp) > 200) {
            var data = new WeaponData(weapon, player);
            handCache.checkAmmoTimestamp = System.currentTimeMillis();
            handCache.fireMode = WeaponStateHelper.getFireMode(data);
            handCache.isThrowable = WeaponModifierHelper.getWeaponAction(data) == WeaponAction.THROW;
            handCache.weaponModes = new LinkedHashMap<>();

            if(handCache.isThrowable){
                handCache.throwMode = ThrowableStateHelper.getThrowMode(data);
                handCache.fireModeKey = WeaponModifierHelper.getThrowModes(data).size() > 1;
            }
            else {
                handCache.fireModeKey = WeaponModifierHelper.getFireModes(data).size() > 1;
                handCache.ammoTypeKey = WeaponModifierHelper.getAmmoItems(data).size() > 1;
            }
            var fuels = WeaponModifierHelper.getAllFuel(data);
            handCache.fuels.clear();
            for (var id : fuels) {
                var value = WeaponModifierHelper.getFuel(id.getId(), data);
                handCache.fuels.put(id, value);
            }

            addAction(handCache, data);
            var weaponModes = WeaponModifierHelper.getWeaponModes(data).keySet();

            for(var mode : weaponModes){
                data.weaponMode = mode;
                addAction(handCache, data);
            }
        }
    }

    private void addAction(GunHudCache handCache, WeaponData data) {
        var mode = data.weaponMode;
        var action = WeaponModifierHelper.getWeaponAction(data.clone().setWeaponMode(mode));
        var player = (Player)data.wielder;
        var weapon = data.weapon;
        var modeInfo = new GunHudCache.ModeInfo();
        modeInfo.action = action;
        modeInfo.maxAmmoCount = WeaponModifierHelper.getMaxAmmo(data);

        if(handCache.isThrowable){
            modeInfo.ammoCount = data.weapon.getCount();
            modeInfo.ammoConfig = WeaponModifierHelper.getConfig(data).getThrowable().getAmmo();
        }
        else {
            modeInfo.ammoCount = WeaponStateHelper.getAmmoCount(data);
            var ammoId = WeaponStateHelper.getCurrentAmmo(data).getId();
            modeInfo.ammoConfig = WeaponModifierHelper.getAmmoConfig(ammoId, data);
        }

        if(action != WeaponAction.NONE) {
            handCache.weaponModes.put(mode, modeInfo);
        }

        if (!player.isCreative()) {
            if(handCache.isThrowable)
                modeInfo.inventoryAmmoCount = getInventoryThrowableCount(weapon, player.getInventory());
            else modeInfo.inventoryAmmoCount = getInventoryAmmoCount(weapon, player.getInventory());
        } else {
            modeInfo.inventoryAmmoCount = 9999;
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

    private static boolean shouldRender(InteractionHand hand, LocalPlayer player) {
        return hand == InteractionHand.MAIN_HAND || WeaponModifierHelper.canUseOffhandWeapon(player);
    }

    public record Colors(int hud, int inventoryAmmo, int currentAmmo, int lowAmmo){}
}
