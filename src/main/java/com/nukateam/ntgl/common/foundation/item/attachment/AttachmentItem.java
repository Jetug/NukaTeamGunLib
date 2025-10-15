package com.nukateam.ntgl.common.foundation.item.attachment;

import com.nukateam.ntgl.client.tooltip.ItemsTooltipData;
import com.nukateam.ntgl.modules.datapack.ConfigSupplier;
import com.nukateam.ntgl.common.data.holders.AttachmentType;
import com.nukateam.ntgl.common.data.attachment.IAttachment;
import com.nukateam.ntgl.common.data.attachment.impl.Attachment;
import com.nukateam.ntgl.common.data.config.attachment.AttachmentConfig;
import com.nukateam.ntgl.common.foundation.item.interfaces.IColored;
import com.nukateam.ntgl.common.foundation.item.interfaces.IMeta;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Optional;

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

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level pLevel, List<Component> tooltip, TooltipFlag pIsAdvanced) {
        var perks = getProperties().getPerks(stack);
        if (perks != null && !perks.isEmpty()) {
            tooltip.add(Component.translatable("perk.ntgl.title").withStyle(ChatFormatting.GRAY, ChatFormatting.BOLD));
            tooltip.addAll(perks);
        }
    }

    @Override
    public Optional<TooltipComponent> getTooltipImage(ItemStack stack) {
//        var weapons = Arrays.asList(
//                new ItemStack(Items.DIAMOND),
//                new ItemStack(Items.EMERALD)
//        );

        var weapons = getProperties().getWeapons(this);
        return Optional.of(new ItemsTooltipData(weapons));
    }
}
