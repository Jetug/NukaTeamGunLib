package com.nukateam.ntgl.client.tooltip;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.world.item.ItemStack;

import java.util.List;

public class ItemsClientTooltipComponent implements ClientTooltipComponent {
    private final List<ItemStack> items;

    public ItemsClientTooltipComponent(ItemsTooltipData data) {
        this.items = data.items();
    }

    @Override
    public int getHeight() {
        return 20;
    }

    @Override
    public int getWidth(Font font) {
        return items.size() * 18;
    }

    @Override
    public void renderImage(Font font, int x, int y, GuiGraphics guiGraphics) {
        for (int i = 0; i < items.size(); i++) {
            ItemStack item = items.get(i);
            guiGraphics.renderItem(item, x + i * 18, y);
            guiGraphics.renderItemDecorations(font, item, x + i * 18, y);
        }
    }
}