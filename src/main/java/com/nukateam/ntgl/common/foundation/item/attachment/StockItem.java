package com.nukateam.ntgl.common.foundation.item.attachment;

import com.nukateam.ntgl.common.base.holders.AttachmentType;
import com.nukateam.ntgl.common.data.attachment.impl.Stock;

/**
 * A basic stock attachment item implementation with color support
 * <p>
 * Author: MrCrayfish
 */
@Deprecated
public class StockItem extends AttachmentItem<Stock> {
    public StockItem(Stock data, Properties properties) {
        super(AttachmentType.STOCK, data, properties);
    }
}
