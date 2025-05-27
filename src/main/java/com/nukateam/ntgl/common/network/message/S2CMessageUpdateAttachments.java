package com.nukateam.ntgl.common.network.message;

import com.google.common.collect.ImmutableMap;
import com.mrcrayfish.framework.api.network.MessageContext;
import com.mrcrayfish.framework.api.network.message.PlayMessage;
import com.nukateam.ntgl.client.handlers.ClientPlayHandler;
import com.nukateam.ntgl.common.base.NetworkAttachmentManager;
import com.nukateam.ntgl.common.data.config.attachment.AttachmentConfig;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import org.apache.commons.lang3.Validate;

/**
 * Author: MrCrayfish
 */
public class S2CMessageUpdateAttachments extends PlayMessage<S2CMessageUpdateAttachments> {
    private ImmutableMap<ResourceLocation, AttachmentConfig> registered;
//    private ImmutableMap<ResourceLocation, CustomGun> customGuns;

    public S2CMessageUpdateAttachments() {
    }

    @Override
    public void encode(S2CMessageUpdateAttachments message, FriendlyByteBuf buffer) {
        Validate.notNull(NetworkAttachmentManager.get());
//        Validate.notNull(CustomGunLoader.get());
        NetworkAttachmentManager.get().writeRegistered(buffer);
//        CustomGunLoader.get().writeCustomGuns(buffer);
    }

    @Override
    public S2CMessageUpdateAttachments decode(FriendlyByteBuf buffer) {
        var message = new S2CMessageUpdateAttachments();
        message.registered = NetworkAttachmentManager.readRegistered(buffer);
//        message.customGuns = CustomGunLoader.readCustomGuns(buffer);
        return message;
    }

    @Override
    public void handle(S2CMessageUpdateAttachments message, MessageContext supplier) {
        supplier.execute((() -> ClientPlayHandler.handleUpdateAttachments(message)));
        supplier.setHandled(true);
    }

    public ImmutableMap<ResourceLocation, AttachmentConfig> getRegistered() {
        return this.registered;
    }

//    public ImmutableMap<ResourceLocation, CustomGun> getCustomGuns() {
//        return this.customGuns;
//    }
}
