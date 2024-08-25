package com.nukateam.ntgl.common.foundation.item;

import com.nukateam.ntgl.common.foundation.item.attachment.IBarrel;
import com.nukateam.ntgl.common.foundation.item.attachment.IMagazine;
import com.nukateam.ntgl.common.foundation.item.attachment.impl.Magazine;
import com.nukateam.ntgl.common.foundation.item.interfaces.IColored;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.Enchantments;

/**
 * A basic magazine attachment item implementation
 * <p>
 * Author: Jetug
 */
public class MagazineItem extends AttachmentItem implements IMagazine, IColored {
    private final Magazine magazine;

    public MagazineItem(Magazine barrel, Properties properties) {
        super(properties);
        this.magazine = barrel;
    }

    public MagazineItem(Magazine barrel, Properties properties, boolean colored) {
        super(properties);
        this.magazine = barrel;
    }

    @Override
    public Magazine getProperties() {
        return this.magazine;
    }

    @Override
    public boolean canColor(ItemStack stack) {
        return false;
    }

    @Override
    public boolean canApplyAtEnchantingTable(ItemStack stack, Enchantment enchantment) {
        return enchantment == Enchantments.BINDING_CURSE || super.canApplyAtEnchantingTable(stack, enchantment);
    }
}
