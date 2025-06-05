package com.nukateam.ntgl.common.foundation.item.attachment;

import com.nukateam.ntgl.common.base.holders.AttachmentType;
import com.nukateam.ntgl.common.data.attachment.impl.Attachment;

/**
 * A basic muzzle attachment item implementation
 * <p>
 * Author: Jetug
 */
@Deprecated
public class MuzzleItem extends AttachmentItem<Attachment> {
    public MuzzleItem(Attachment data, Properties properties) {
        super(AttachmentType.MUZZLE, data, properties);
    }
}
