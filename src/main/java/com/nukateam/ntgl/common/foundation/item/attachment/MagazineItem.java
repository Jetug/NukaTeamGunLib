package com.nukateam.ntgl.common.foundation.item.attachment;

import com.nukateam.ntgl.common.base.holders.AttachmentType;
import com.nukateam.ntgl.common.data.attachment.IMagazine;
import com.nukateam.ntgl.common.data.attachment.impl.Grip;
import com.nukateam.ntgl.common.data.attachment.impl.Magazine;
import com.nukateam.ntgl.common.foundation.item.interfaces.IColored;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.Enchantments;

/**
 * A basic magazine attachment item implementation
 * <p>
 * Author: Jetug
 */
public class MagazineItem extends AttachmentItem<Magazine>{
    public MagazineItem(Magazine data, Properties properties) {
        super(AttachmentType.MAGAZINE, data, properties);
    }
}