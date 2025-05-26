package com.nukateam.ntgl.common.foundation.item.attachment;

import com.nukateam.geo.interfaces.IResourceProvider;
import com.nukateam.ntgl.common.base.NetworkManager;
import com.nukateam.ntgl.common.base.holders.AttachmentType;
import com.nukateam.ntgl.common.data.attachment.IAttachment;
import com.nukateam.ntgl.common.data.attachment.impl.Attachment;
import com.nukateam.ntgl.common.data.config.attachment.AttachmentConfig;
import com.nukateam.ntgl.common.data.config.gun.Gun;
import com.nukateam.ntgl.common.foundation.item.IConfigConsumer;
import com.nukateam.ntgl.common.foundation.item.interfaces.IColored;
import com.nukateam.ntgl.common.foundation.item.interfaces.IMeta;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraftforge.common.util.Lazy;
import net.minecraftforge.registries.ForgeRegistries;

import static com.nukateam.ntgl.common.util.util.ResourceUtils.getResourceName;

/**
 * A basic barrel attachment item implementation with color support
 * <p>
 * Author: MrCrayfish
 */
public class AttachmentItem<T extends Attachment> extends Item implements IAttachment<T>, IMeta, IColored, IResourceProvider, IConfigConsumer<AttachmentConfig> {
    private final Lazy<String> name = Lazy.of(() -> getResourceName(ForgeRegistries.ITEMS.getKey(this)));
    private final AttachmentType type;
    private final T attachmentData;
    private final boolean colored;
    private AttachmentConfig config = new AttachmentConfig();

    public AttachmentItem(AttachmentType type, T attachmentData, Properties properties) {
        super(properties);
        this.type = type;
        this.attachmentData = attachmentData;
        this.colored = true;
    }

    @Override
    public void setConfig(NetworkManager.Supplier<AttachmentConfig> supplier) {
        this.config = supplier.getConfig();
//        config.onCreated(getName());
    }

    @Override
    public AttachmentType getType() {
        return type;
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
    public String getName() {
        return name.get();
    }

    @Override
    public String getNamespace() {
        return ForgeRegistries.ITEMS.getKey(this).getNamespace();
    }

    public AttachmentConfig getConfig() {
        return config;
    }
}
