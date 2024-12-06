package com.nukateam.ntgl.common.foundation.item.attachment;

import com.nukateam.ntgl.common.base.holders.AttachmentType;
import com.nukateam.ntgl.common.data.attachment.IUnderBarrel;
import com.nukateam.ntgl.common.data.attachment.impl.Stock;
import com.nukateam.ntgl.common.data.attachment.impl.UnderBarrel;
import com.nukateam.ntgl.common.foundation.item.interfaces.IColored;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.Enchantments;

/**
 * A basic under barrel attachment item implementation with color support
 * <p>
 * Author: MrCrayfish
 */
public class UnderBarrelItem extends AttachmentItem<UnderBarrel> {
    public UnderBarrelItem(UnderBarrel data, Properties properties) {
        super(AttachmentType.SCOPE, data, properties);
    }
}
