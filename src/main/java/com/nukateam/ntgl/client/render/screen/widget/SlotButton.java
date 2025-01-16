package com.nukateam.ntgl.client.render.screen.widget;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

public class SlotButton extends Button {
    private final ItemStack stack;

    public SlotButton(int x, int y, ItemStack stack, OnPress onPress) {
        super(x, y, 16, 16, CommonComponents.EMPTY, onPress, DEFAULT_NARRATION);
        this.stack = stack;
    }

    @Override
    public void renderWidget(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {}

    public ItemStack getStack() {
        return stack;
    }
}
