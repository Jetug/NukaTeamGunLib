package com.nukateam.ntgl.common.foundation.item.attachment;

import com.nukateam.ntgl.Ntgl;
import com.nukateam.ntgl.common.base.holders.AttachmentType;
import com.nukateam.ntgl.common.foundation.item.attachment.impl.Attachment;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

/**
 * The base attachment interface
 * <p>
 * Author: MrCrayfish
 */
public interface IAttachment<T extends Attachment> {
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
}
