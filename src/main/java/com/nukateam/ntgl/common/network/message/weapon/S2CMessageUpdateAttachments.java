package com.nukateam.ntgl.common.network.message.weapon;

import com.google.common.collect.ImmutableMap;
import net.minecraftforge.network.NetworkEvent;
import com.nukateam.ntgl.modules.network.IMessage;
import com.nukateam.ntgl.client.handlers.ClientPlayHandler;
import com.nukateam.ntgl.modules.datapack.managers.NetworkAttachmentManager;
import com.nukateam.ntgl.common.data.config.attachment.AttachmentConfig;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import org.apache.commons.lang3.Validate;

public class S2CMessageUpdateAttachments implements IMessage<S2CMessageUpdateAttachments> {
    private ImmutableMap<ResourceLocation, AttachmentConfig> registered;

    public S2CMessageUpdateAttachments() {}

    @Override
    public void encode(S2CMessageUpdateAttachments message, FriendlyByteBuf buffer) {
        Validate.notNull(NetworkAttachmentManager.get());
        NetworkAttachmentManager.get().writeRegistered(buffer);
    }

    @Override
    public S2CMessageUpdateAttachments decode(FriendlyByteBuf buffer) {
        var message = new S2CMessageUpdateAttachments();
        message.registered = NetworkAttachmentManager.readRegistered(buffer);
        return message;
    }

    @Override
    public void handle(S2CMessageUpdateAttachments message, NetworkEvent.Context supplier) {
        supplier.enqueueWork((() -> ClientPlayHandler.handleUpdateAttachments(message)));
        supplier.setPacketHandled(true);
    }

    public ImmutableMap<ResourceLocation, AttachmentConfig> getRegistered() {
        return this.registered;
    }
}
