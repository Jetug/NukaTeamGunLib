
package com.nukateam.ntgl.common.foundation.item;

import com.nukateam.ntgl.common.foundation.item.attachment.IGrip;
import com.nukateam.ntgl.common.foundation.item.attachment.IStock;
import com.nukateam.ntgl.common.foundation.item.attachment.impl.Grip;
import com.nukateam.ntgl.common.foundation.item.interfaces.IColored;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.Enchantments;

/**
 * A basic stock attachment item implementation with color support
 * <p>
 * Author: MrCrayfish
 */
public class GripItem extends AttachmentItem implements IGrip, IColored {
    private final Grip stock;
    private final boolean colored;

    public GripItem(Grip stock, Properties properties) {
        super(properties);
        this.stock = stock;
        this.colored = true;
    }

    public GripItem(Grip stock, Properties properties, boolean colored) {
        super(properties);
        this.stock = stock;
        this.colored = colored;
    }

    @Override
    public Grip getProperties() {
        return this.stock;
    }

    @Override
    public boolean canColor(ItemStack stack) {
        return this.colored;
    }

    @Override
    public boolean canApplyAtEnchantingTable(ItemStack stack, Enchantment enchantment) {
        return enchantment == Enchantments.BINDING_CURSE || super.canApplyAtEnchantingTable(stack, enchantment);
    }
}
