package com.nukateam.ntgl.common.foundation.item.attachment;

import com.nukateam.ntgl.common.base.holders.AttachmentType;
import com.nukateam.ntgl.common.data.attachment.impl.UnderBarrel;

/**
 * A basic under barrel attachment item implementation with color support
 * <p>
 * Author: MrCrayfish
 */
@Deprecated
public class UnderBarrelItem extends AttachmentItem<UnderBarrel> {
    public UnderBarrelItem(UnderBarrel data, Properties properties) {
        super(AttachmentType.UNDER_BARREL, data, properties);
    }
}
