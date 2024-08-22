package com.nukateam.ntgl.common.foundation.item.attachment;

import com.nukateam.ntgl.Ntgl;
import com.nukateam.ntgl.common.foundation.item.attachment.impl.Attachment;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

import javax.annotation.Nullable;

/**
 * The base attachment interface
 * <p>
 * Author: MrCrayfish
 */
public interface IAttachment<T extends Attachment> {
    /**
     * @return The type of this attachment
     */
    ResourceLocation getType();

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

    class Type {
        public static ResourceLocation SCOPE        = new ResourceLocation (Ntgl.MOD_ID, "scope");
        public static ResourceLocation BARREL       = new ResourceLocation (Ntgl.MOD_ID, "barrel");
        public static ResourceLocation STOCK        = new ResourceLocation (Ntgl.MOD_ID, "stock");
        public static ResourceLocation UNDER_BARREL = new ResourceLocation (Ntgl.MOD_ID, "under_barrel");

        private final String translationKey;
        private final String tagKey;
        private final String serializeKey;

        Type(String translationKey, String tagKey, String serializeKey) {
            this.translationKey = translationKey;
            this.tagKey = tagKey;
            this.serializeKey = serializeKey;
        }

        public String getTranslationKey() {
            return this.translationKey;
        }

        public String getTagKey() {
            return this.tagKey;
        }

        public String getSerializeKey() {
            return this.serializeKey;
        }

        @Nullable
        public static Type byTagKey(String s) {
            for (Type type : values()) {
                if (type.tagKey.equalsIgnoreCase(s)) {
                    return type;
                }
            }
            return null;
        }
    }
}
