package com.nukateam.ntgl.common.foundation.item.attachment;

import com.nukateam.ntgl.common.base.gun.AttachmentType;
import com.nukateam.ntgl.common.foundation.item.BarrelItem;
import com.nukateam.ntgl.common.foundation.item.attachment.impl.Barrel;

/**
 * An interface to turn an any item into a barrel attachment. This is useful if your item extends a
 * custom item class otherwise {@link BarrelItem} can be used instead of
 * this interface.
 * <p>
 * Author: Ocelot, MrCrayfish
 */
public interface IBarrel extends IAttachment<Barrel> {
    /**
     * @return The type of this attachment
     */
    @Override
    default AttachmentType getType() {
        return AttachmentType.BARREL;
    }
}
