package com.nukateam.ntgl.common.network.message.weapon;

import com.mrcrayfish.framework.api.network.MessageContext;
import com.nukateam.ntgl.common.network.ServerPlayHandler;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;

/**
 * Author: MrCrayfish
 */
public class C2SMessagePreFireSound  {
    public static final StreamCodec<RegistryFriendlyByteBuf, C2SMessagePreFireSound> STREAM_CODEC = StreamCodec.of(
            (buffer, message) -> encode(message, buffer),
            buffer -> decode(buffer));

    private InteractionHand hand;

    public C2SMessagePreFireSound() {}

    public C2SMessagePreFireSound(InteractionHand hand) {
        this.hand = hand;
    }

    public static void encode(C2SMessagePreFireSound message, FriendlyByteBuf buffer) {
        buffer.writeEnum(message.hand);
    }

    public static C2SMessagePreFireSound decode(FriendlyByteBuf buffer) {
        return new C2SMessagePreFireSound(buffer.readEnum(InteractionHand.class));
    }

    public static void handle(C2SMessagePreFireSound message, MessageContext context) {
        context.execute(() ->
            context.getPlayer().ifPresent(player ->
                ServerPlayHandler.handlePreFireSound(message, (ServerPlayer)player)
            )
        );
        context.setHandled(true);
    }

    public InteractionHand getHand() {
        return hand;
    }
}