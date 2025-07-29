package com.nukateam.ntgl.common.foundation.item.attachment;

import com.nukateam.ntgl.common.data.holders.AttachmentType;
import com.nukateam.ntgl.common.data.attachment.impl.Barrel;
import net.minecraft.world.item.Item;

/**
 * A basic barrel attachment item implementation with color support
 * <p>
 * Author: MrCrayfish
 */
public class BarrelItem extends AttachmentItem<Barrel>{
    public BarrelItem(Barrel barrel, Item.Properties properties) {
        super(AttachmentType.BARREL, barrel, properties);
    }
}
