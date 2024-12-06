package com.nukateam.ntgl.common.foundation.item.attachment;

import com.nukateam.ntgl.common.base.holders.AttachmentType;
import com.nukateam.ntgl.common.data.attachment.impl.GenericAttachment;

/**
 * A basic muzzle attachment item implementation
 * <p>
 * Author: Jetug
 */
public class MuzzleItem extends AttachmentItem {
    public MuzzleItem(GenericAttachment data, Properties properties) {
        super(AttachmentType.MAGAZINE, data, properties);
    }
}
