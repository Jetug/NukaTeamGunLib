package com.nukateam.ntgl.common.foundation.item.attachment;

import com.nukateam.ntgl.common.base.holders.AttachmentType;
import com.nukateam.ntgl.common.data.attachment.impl.Scope;

/**
 * A basic scope attachment item implementation with color support
 * <p>
 * Author: MrCrayfish
 */
public class ScopeItem extends AttachmentItem<Scope> {
    public ScopeItem(Scope data, Properties properties) {
        super(AttachmentType.SCOPE, data, properties);
    }
}
