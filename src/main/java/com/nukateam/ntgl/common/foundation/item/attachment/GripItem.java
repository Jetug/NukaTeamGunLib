
package com.nukateam.ntgl.common.foundation.item.attachment;

import com.nukateam.ntgl.common.base.holders.AttachmentType;
import com.nukateam.ntgl.common.data.attachment.IGrip;
import com.nukateam.ntgl.common.data.attachment.impl.Grip;
import com.nukateam.ntgl.common.foundation.item.interfaces.IColored;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.Enchantments;

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
