/*
 * Copyright (c) 2019-2022 Team Galacticraft
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package com.nukateam.ntgl.mixin.chassis.client;

import com.nukateam.chassis_core.Global;
import com.nukateam.chassis_core.client.render.utils.GuiUtils;
import com.nukateam.chassis_core.common.data.enums.ActionType;
import com.nukateam.chassis_core.common.util.helpers.PlayerUtils;
import com.mojang.blaze3d.platform.Lighting;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.EffectRenderingInventoryScreen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.awt.*;

import static com.nukateam.chassis_core.common.data.constants.Gui.*;
import static com.nukateam.chassis_core.common.data.constants.Resources.PLAYER_INVENTORY_TABS;
import static com.nukateam.chassis_core.common.network.PacketSender.doServerAction;
import static net.minecraft.world.item.Items.CRAFTING_TABLE;

@Mixin(InventoryScreen.class)
@OnlyIn(Dist.CLIENT)
public abstract class InventoryScreenMixin extends EffectRenderingInventoryScreen<InventoryMenu> {
    public InventoryScreenMixin(InventoryMenu screenHandler, Inventory playerInventory, Component textComponent) {
        super(screenHandler, playerInventory, textComponent);
    }

    @Shadow
    public abstract boolean mouseClicked(double pMouseX, double pMouseY, int pButton);

    @Inject(method = "mouseClicked(DDI)Z", at = @At("HEAD"))
    public void mouseClicked(double mouseX, double mouseY, int button, CallbackInfoReturnable<Boolean> ci) {
        if (!PlayerUtils.isLocalWearingChassis()) return;

        var rect = new Rectangle(leftPos + 30, topPos - TAB_HEIGHT, TAB_WIDTH, TAB_HEIGHT);
        if (rect.contains(mouseX, mouseY)) {
            Global.saveMousePos();
            doServerAction(ActionType.OPEN_GUI);
        }
    }

    @Inject(method = "renderBg(Lnet/minecraft/client/gui/GuiGraphics;FII)V", at = @At("TAIL"))
    public void drawBackground(GuiGraphics graphics, float pPartialTick, int pMouseX, int pMouseY, CallbackInfo callbackInfo) {
        if (!PlayerUtils.isLocalWearingChassis()) return;

        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        graphics.blit(PLAYER_INVENTORY_TABS, this.leftPos, this.topPos - 28, 0, 0, 57, 32);
    }

    @Inject(method = "render(Lnet/minecraft/client/gui/GuiGraphics;IIF)V", at = @At("TAIL"))
    public void render(GuiGraphics graphics, int pMouseX, int pMouseY, float pPartialTick, CallbackInfo callbackInfo) {
        if (!PlayerUtils.isLocalWearingChassis()) return;

        Lighting.setupFor3DItems();
        graphics.renderItem(CRAFTING_TABLE.getDefaultInstance(), leftPos + TOP_TAB_ICON_POS_1.x,
                topPos + TOP_TAB_ICON_POS_1.y);
        Lighting.setupForFlatItems();
        GuiUtils.drawChassisIcon(graphics, leftPos + TOP_TAB_ICON_POS_2.x, topPos + TOP_TAB_ICON_POS_2.y);
    }
}
