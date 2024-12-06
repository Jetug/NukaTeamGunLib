package com.nukateam.ntgl.common.foundation.item.attachment;

import com.nukateam.ntgl.common.base.holders.AttachmentType;
import com.nukateam.ntgl.common.data.attachment.IAttachment;
import com.nukateam.ntgl.common.data.attachment.impl.GenericAttachment;
import com.nukateam.ntgl.common.foundation.item.interfaces.IColored;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.Enchantments;

/**
 * A basic barrel attachment item implementation with color support
 * <p>
 * Author: MrCrayfish
 */
public class AttachmentItem extends AttachmentItemBase implements IAttachment<GenericAttachment>, IColored {
    private final AttachmentType type;
    private final GenericAttachment attachmentData;
    private final boolean colored;

    public AttachmentItem(AttachmentType type, GenericAttachment attachmentData, Properties properties) {
        super(properties);
        this.type = type;
        this.attachmentData = attachmentData;
        this.colored = true;
    }

    @Override
    public AttachmentType getType() {
        return type;
    }

    @Override
    public GenericAttachment getProperties() {
        return this.attachmentData;
    }

    @Override
    public boolean canColor(ItemStack stack) {
        return this.colored;
    }

    @Override
    public boolean canApplyAtEnchantingTable(ItemStack stack, Enchantment enchantment) {
        return enchantment == Enchantments.BINDING_CURSE || super.canApplyAtEnchantingTable(stack, enchantment);
    }
}
