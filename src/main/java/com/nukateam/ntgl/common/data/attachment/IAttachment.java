package com.nukateam.ntgl.common.data.attachment;

import com.nukateam.ntgl.common.base.holders.AttachmentType;
import com.nukateam.ntgl.common.data.attachment.impl.Attachment;
import com.nukateam.ntgl.common.data.config.attachment.AttachmentConfig;
import com.nukateam.ntgl.common.foundation.item.interfaces.IConfigConsumer;
import net.minecraft.world.item.ItemStack;

/**
 * The base attachment interface
 * <p>
 * Author: MrCrayfish
 */
public interface IAttachment<T extends Attachment> extends IConfigConsumer<AttachmentConfig> {
    /**
     * @return The type of this attachment
     */
    AttachmentType getType();

    /**
     * @return The additional properties about this attachment
     */
    T getProperties();

    /**
     * @param stack Weapon stack
     * @return If attachment can be attached to gun
     */
    default boolean canAttachTo(ItemStack stack) {
        return true;
    }

    AttachmentConfig getAttachmentConfig();
}
