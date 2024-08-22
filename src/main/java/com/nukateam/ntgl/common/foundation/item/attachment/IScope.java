package com.nukateam.ntgl.common.foundation.item.attachment;

import com.nukateam.ntgl.common.foundation.item.ScopeItem;
import com.nukateam.ntgl.common.foundation.item.attachment.impl.Scope;
import net.minecraft.resources.ResourceLocation;

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
    default ResourceLocation getType() {
        return Type.SCOPE;
    }
}
