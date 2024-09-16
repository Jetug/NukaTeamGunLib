package com.nukateam.ntgl.client.render.hud;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.nukateam.ntgl.Ntgl;
import com.nukateam.ntgl.common.base.gun.FireMode;
import com.nukateam.ntgl.common.base.gun.Gun;
import com.nukateam.ntgl.common.data.util.GunEnchantmentHelper;
import com.nukateam.ntgl.common.data.util.GunModifierHelper;
import com.nukateam.ntgl.common.foundation.item.AmmoBoxItem;
import com.nukateam.ntgl.common.foundation.item.AmmoItem;
import com.nukateam.ntgl.common.foundation.item.GunItem;
import com.nukateam.ntgl.common.foundation.item.interfaces.IAmmo;
import net.minecraft.SharedConstants;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.NameTagItem;
import net.minecraftforge.client.gui.overlay.ForgeGui;
import net.minecraftforge.client.gui.overlay.IGuiOverlay;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.registries.ForgeRegistries;

import java.text.DecimalFormat;

public class AmmoHud {
    private static final int BAR_OFFSET_X = 95;

    public static final IGuiOverlay AMMO_HUD = new GunHudOverlay();
//    ((gui, graphics, partialTick, width, height) -> {
//        var minecraft = Minecraft.getInstance();
//        if(minecraft.player == null) return;
//        var mainHandItem = minecraft.player.getMainHandItem();
//        var offhandItem  = minecraft.player.getOffhandItem();
//        var y = height - 39;
//        var xCenter = width / 2;
//
//        if(mainHandItem.getItem() instanceof GunItem gunItem) {
//            var x = xCenter + BAR_OFFSET_X;
//            renderAmmoCounter(graphics, gunItem, mainHandItem, x, y);
//        }
//        if(offhandItem.getItem() instanceof GunItem gunItem) {
//            var x = xCenter - BAR_OFFSET_X - 26;
//            renderAmmoCounter(graphics, gunItem, offhandItem, x, y);
//        }
//    });

    private static void renderAmmoCounter(GuiGraphics graphics, GunItem gunItem, ItemStack heldStack, int x, int y) {
        var gun = gunItem.getModifiedGun(heldStack);
        var currentAmmo = Gun.getAmmo(heldStack);
        var maxAmmo = GunEnchantmentHelper.getAmmoCapacity(heldStack);
        var text = currentAmmo + "/" + maxAmmo;

        graphics.drawString(Minecraft.getInstance().font, text, x, y, 0xFFFFFFFF, true);
    }
}