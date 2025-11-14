package com.nukateam.ntgl.common.network.message.weapon;

import com.mrcrayfish.framework.api.network.MessageContext;
import com.nukateam.ntgl.common.network.ServerPlayHandler;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.InteractionHand;

/**
 * Author: MrCrayfish
 */
public class C2SMessageUnload  {
    public static final StreamCodec<RegistryFriendlyByteBuf, C2SMessageUnload> STREAM_CODEC = StreamCodec.of(
            (buffer, message) -> encode(message, buffer),
            buffer -> decode(buffer));
    private InteractionHand hand = InteractionHand.MAIN_HAND;

    public C2SMessageUnload(){}

    public C2SMessageUnload(InteractionHand hand) {
        this.hand = hand;
    }

    public static void encode(C2SMessageUnload message, FriendlyByteBuf buffer) {
        buffer.writeEnum(message.hand);
    }

    public static C2SMessageUnload decode(FriendlyByteBuf buffer) {
        return new C2SMessageUnload(buffer.readEnum(InteractionHand.class));
    }

    public static void handle(C2SMessageUnload message, MessageContext supplier) {
        supplier.execute((() -> {
            var player = supplier.getPlayer().get();
            if (player != null && !player.isSpectator()) {
                ServerPlayHandler.handleUnload(player, message.hand);
            }
        }));
        supplier.setHandled(true);
    }
}
