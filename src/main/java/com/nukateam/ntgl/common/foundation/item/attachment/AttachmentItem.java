package com.nukateam.ntgl.common.foundation.item.attachment;

import com.nukateam.ntgl.modules.datapack.ConfigSupplier;
import com.nukateam.ntgl.common.data.holders.AttachmentType;
import com.nukateam.ntgl.common.data.attachment.IAttachment;
import com.nukateam.ntgl.common.data.attachment.impl.Attachment;
import com.nukateam.ntgl.common.data.config.attachment.AttachmentConfig;
import com.nukateam.ntgl.common.foundation.item.interfaces.IColored;
import com.nukateam.ntgl.common.foundation.item.interfaces.IMeta;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.Enchantments;

public class AttachmentItem<T extends Attachment> extends Item implements IAttachment<T>, IMeta, IColored{
    private final T attachmentData;
    private final boolean colored;
    private AttachmentConfig config;

    public AttachmentItem(AttachmentType type, T attachmentData, Properties properties) {
        super(properties);
        this.config = AttachmentConfig.Builder.create().setType(type).build();
        this.attachmentData = attachmentData;
        this.colored = true;
    }

    @Override
    public AttachmentConfig getAttachmentConfig() {
        return config;
    }

    @Override
    public void setConfig(ConfigSupplier<AttachmentConfig> supplier) {
        this.config = supplier.getConfig();
    }

    @Override
    public AttachmentType getType() {
        return config.getType();
    }

    @Override
    public T getProperties() {
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
