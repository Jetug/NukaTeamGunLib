package com.nukateam.ntgl.common.network.message.weapon;

import com.google.common.collect.ImmutableMap;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import com.nukateam.ntgl.Ntgl;
import com.nukateam.ntgl.client.handlers.ClientPlayHandler;
import com.nukateam.ntgl.modules.datapack.managers.NetworkAttachmentManager;
import com.nukateam.ntgl.common.data.config.attachment.AttachmentConfig;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import org.apache.commons.lang3.Validate;

public class S2CMessageUpdateAttachments implements CustomPacketPayload {
    public static final Type<S2CMessageUpdateAttachments> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(Ntgl.MOD_ID, "s2c_message_update_attachments"));

    public static final StreamCodec<RegistryFriendlyByteBuf, S2CMessageUpdateAttachments> CODEC = StreamCodec.of(
            (buffer, message) -> encode(message, buffer),
            buffer -> decode(buffer));

    private ImmutableMap<ResourceLocation, AttachmentConfig> registered;

    public S2CMessageUpdateAttachments() {}

    public static void encode(S2CMessageUpdateAttachments message, FriendlyByteBuf buffer) {
        Validate.notNull(NetworkAttachmentManager.get());
        NetworkAttachmentManager.get().writeRegistered(buffer);
    }

    public static S2CMessageUpdateAttachments decode(FriendlyByteBuf buffer) {
        var message = new S2CMessageUpdateAttachments();
        message.registered = NetworkAttachmentManager.readRegistered(buffer);
        return message;
    }

    public static void handle(S2CMessageUpdateAttachments message, IPayloadContext supplier) {
        supplier.enqueueWork((() -> ClientPlayHandler.handleUpdateAttachments(message)));
    }

    public ImmutableMap<ResourceLocation, AttachmentConfig> getRegistered() {
        return this.registered;
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
