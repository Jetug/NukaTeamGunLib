package com.nukateam.ntgl.common.network.message.weapon;

import com.mrcrayfish.framework.api.network.MessageContext;
import com.nukateam.ntgl.common.network.ServerPlayHandler;
import com.nukateam.ntgl.common.network.message.chassis.C2SActionPacket;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.server.level.ServerPlayer;

/**
 * Author: MrCrayfish
 */
public class C2SMessageAttachments {
    public static final StreamCodec<RegistryFriendlyByteBuf, C2SMessageAttachments> STREAM_CODEC = StreamCodec.of(
            (buffer, message) -> encode(message, buffer),
            buffer -> decode(buffer));
    public C2SMessageAttachments() {
    }

    public static void encode(C2SMessageAttachments message, FriendlyByteBuf buffer) {
    }

    public static C2SMessageAttachments decode(FriendlyByteBuf buffer) {
        return new C2SMessageAttachments();
    }

    public static void handle(C2SMessageAttachments message, MessageContext supplier) {
        supplier.execute((() -> {
            var player = supplier.getPlayer().get();
            if (player != null) {
                ServerPlayHandler.handleAttachments(player);
            }
        }));
        supplier.setHandled(true);
    }
}
