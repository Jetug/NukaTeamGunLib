package com.nukateam.ntgl.common.foundation.item.attachment;

import com.nukateam.ntgl.common.base.holders.AttachmentType;
import com.nukateam.ntgl.common.foundation.item.UnderBarrelItem;
import com.nukateam.ntgl.common.foundation.item.attachment.impl.Magazine;

/**
 * An interface to turn an any item into a under barrel attachment. This is useful if your item
 * extends a custom item class otherwise {@link UnderBarrelItem} can be
 * used instead of this interface.
 * <p>
 * Author: MrCrayfish
 */
public interface IMagazine extends IAttachment<Magazine> {
    /**
     * @return The type of this attachment
     */
    @Override
    default AttachmentType getType() {
        return AttachmentType.MAGAZINE;
    }
}
