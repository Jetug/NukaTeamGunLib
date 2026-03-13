package com.nukateam.ntgl.common.network.message.weapon;

import com.mrcrayfish.framework.api.network.MessageContext;
import com.nukateam.ntgl.common.network.ServerPlayHandler;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;

/**
 * Author: MrCrayfish
 */
public class C2SMessageReloadStop{
    public static final StreamCodec<RegistryFriendlyByteBuf, C2SMessageReloadStop> STREAM_CODEC = StreamCodec.of(
            (buffer, message) -> encode(message, buffer),
            buffer -> decode(buffer));

    private InteractionHand hand = InteractionHand.MAIN_HAND;

    public C2SMessageReloadStop() {}

    public C2SMessageReloadStop(InteractionHand hand) {
        this.hand = hand;
    }

    public static void encode(C2SMessageReloadStop message, FriendlyByteBuf buffer) {
        buffer.writeEnum(message.hand);
    }

    public static C2SMessageReloadStop decode(FriendlyByteBuf buffer) {
        return new C2SMessageReloadStop(
                buffer.readEnum(InteractionHand.class));
    }

    public static void handle(C2SMessageReloadStop message, MessageContext supplier) {
        supplier.execute((() ->
        {
            supplier.getPlayer().ifPresent((player) -> {
                if (player != null && !player.isSpectator()) {
                    ServerPlayHandler.handleStopReload(message, player);
                }
            });

        }));
        supplier.setHandled(true);
    }

    public InteractionHand getHand() {
        return hand;
    }
}
