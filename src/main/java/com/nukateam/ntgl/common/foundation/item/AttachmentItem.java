package com.nukateam.ntgl.common.foundation.item;

import com.nukateam.ntgl.common.foundation.item.attachment.IBarrel;
import com.nukateam.ntgl.common.foundation.item.attachment.impl.Barrel;
import com.nukateam.ntgl.common.foundation.item.interfaces.IColored;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.Enchantments;

/**
 * A basic barrel attachment item implementation with color support
 * <p>
 * Author: MrCrayfish
 */
public class AttachmentItem extends AttachmentItemBase implements IBarrel, IColored {
    private final Barrel barrel;
    private final boolean colored;

    public AttachmentItem(Barrel barrel, Properties properties) {
        super(properties);
        this.barrel = barrel;
        this.colored = true;
    }

    public AttachmentItem(Barrel barrel, Properties properties, boolean colored) {
        super(properties);
        this.barrel = barrel;
        this.colored = colored;
    }

    @Override
    public Barrel getProperties() {
        return this.barrel;
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
