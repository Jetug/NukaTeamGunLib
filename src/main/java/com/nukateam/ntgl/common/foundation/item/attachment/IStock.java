package com.nukateam.ntgl.common.foundation.item.attachment;

import com.nukateam.ntgl.common.foundation.item.StockItem;
import com.nukateam.ntgl.common.foundation.item.attachment.impl.Stock;
import net.minecraft.resources.ResourceLocation;

/**
 * An interface to turn an any item into a stock attachment. This is useful if your item extends a
 * custom item class otherwise {@link StockItem} can be used instead of
 * this interface.
 * <p>
 * Author: MrCrayfish
 */
public interface IStock extends IAttachment<Stock> {
    /**
     * @return The type of this attachment
     */
    @Override
    default ResourceLocation getType() {
        return Type.STOCK;
    }
}
