package com.nukateam.ntgl.common.foundation.item.attachment;

import com.nukateam.ntgl.common.foundation.item.BarrelItem;
import com.nukateam.ntgl.common.foundation.item.attachment.impl.Barrel;
import net.minecraft.resources.ResourceLocation;

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
    default ResourceLocation getType() {
        return Type.BARREL;
    }
}
