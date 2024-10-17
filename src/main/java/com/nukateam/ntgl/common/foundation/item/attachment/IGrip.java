 package com.nukateam.ntgl.common.foundation.item.attachment;

import com.nukateam.ntgl.common.base.holders.AttachmentType;
import com.nukateam.ntgl.common.foundation.item.StockItem;
import com.nukateam.ntgl.common.foundation.item.attachment.impl.Grip;

 /**
 * An interface to turn an any item into a stock attachment. This is useful if your item extends a
 * custom item class otherwise {@link StockItem} can be used instead of
 * this interface.
 * <p>
 * Author: MrCrayfish
 */
public interface IGrip extends IAttachment<Grip> {
    /**
     * @return The type of this attachment
     */
    @Override
    default AttachmentType getType() {
        return AttachmentType.STOCK;
    }
}
