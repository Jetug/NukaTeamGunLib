package com.nukateam.ntgl.common.foundation.item.attachment;

import com.nukateam.ntgl.common.data.holders.AttachmentType;
import com.nukateam.ntgl.common.data.attachment.impl.Magazine;

/**
 * A basic magazine attachment item implementation
 * <p>
 * Author: Jetug
 */
@Deprecated
public class MagazineItem extends AttachmentItem<Magazine> {
    public MagazineItem(Magazine data, Properties properties) {
        super(AttachmentType.MAGAZINE, data, properties);
    }
}