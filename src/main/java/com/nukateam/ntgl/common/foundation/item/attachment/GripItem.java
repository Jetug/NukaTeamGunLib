
package com.nukateam.ntgl.common.foundation.item.attachment;

import com.nukateam.ntgl.common.base.holders.AttachmentType;
import com.nukateam.ntgl.common.data.attachment.impl.Grip;

/**
 * A basic stock attachment item implementation with color support
 * <p>
 * Author: MrCrayfish
 */
public class GripItem extends AttachmentItem<Grip> {
    public GripItem(Grip grip, Properties properties) {
        super(AttachmentType.GRIP, grip, properties);
    }
}
