package com.nukateam.ntgl.common.network.message.weapon;

import com.mrcrayfish.framework.api.network.MessageContext;
import com.nukateam.ntgl.common.network.ServerPlayHandler;
import com.nukateam.ntgl.common.network.message.chassis.C2SActionPacket;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;

/**
 * Author: MrCrayfish
 */
public class C2SMessageAttachments {
    public static final StreamCodec<RegistryFriendlyByteBuf, C2SMessageAttachments> STREAM_CODEC = StreamCodec.of(
            (buffer, message) -> encode(message, buffer),
            buffer -> decode(buffer));

    private InteractionHand hand = InteractionHand.MAIN_HAND;

    public C2SMessageAttachments() {}

    public C2SMessageAttachments(InteractionHand hand) {
        this.hand = hand;
    }

    public static void encode(C2SMessageAttachments message, FriendlyByteBuf buffer) {
        buffer.writeEnum(message.hand);
    }

    public static C2SMessageAttachments decode(FriendlyByteBuf buffer) {
        return new C2SMessageAttachments(buffer.readEnum(InteractionHand.class));
    }

    public static void handle(C2SMessageAttachments message, MessageContext supplier) {
        supplier.execute((() -> {
            var player = supplier.getPlayer().get();
            if (player != null) {
                ServerPlayHandler.handleAttachments(player, message.hand);
            }
        }));
        supplier.setHandled(true);
    }
}
