package com.nukateam.ntgl.common.foundation.item.attachment;

import com.nukateam.ntgl.common.base.holders.AttachmentType;
import com.nukateam.ntgl.common.foundation.item.ScopeItem;
import com.nukateam.ntgl.common.foundation.item.attachment.impl.Scope;

/**
 * An interface to turn an any item into a scope attachment. This is useful if your item extends a
 * custom item class otherwise {@link ScopeItem} can be used instead of
 * this interface.
 * <p>
 * Author: Ocelot
 */
public interface IScope extends IAttachment<Scope> {
    /**
     * @return The type of this attachment
     */
    @Override
    default AttachmentType getType() {
        return AttachmentType.SCOPE;
    }
}
