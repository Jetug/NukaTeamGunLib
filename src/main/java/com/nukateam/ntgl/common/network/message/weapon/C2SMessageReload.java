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
public class C2SMessageReload  {
    public static final StreamCodec<RegistryFriendlyByteBuf, C2SMessageReload> STREAM_CODEC = StreamCodec.of(
            (buffer, message) -> encode(message, buffer),
            buffer -> decode(buffer));
    private boolean reload;
    private InteractionHand hand = InteractionHand.MAIN_HAND;

    public C2SMessageReload() {}

    public C2SMessageReload(boolean reload, InteractionHand hand) {
        this.reload = reload;
        this.hand = hand;
    }

    public static void encode(C2SMessageReload message, FriendlyByteBuf buffer) {
        buffer.writeBoolean(message.reload);
        buffer.writeEnum(message.hand);
    }

    public static C2SMessageReload decode(FriendlyByteBuf buffer) {
        return new C2SMessageReload(buffer.readBoolean(), buffer.readEnum(InteractionHand.class));
    }

    public static void handle(C2SMessageReload message, MessageContext supplier) {
        supplier.execute((() ->
        {
            var player = supplier.getPlayer().get();
            if (player != null && !player.isSpectator()) {
                ServerPlayHandler.handleReload(message, player);
            }
        }));
        supplier.setHandled(true);
    }

    public boolean isReload() {
        return reload;
    }

    public InteractionHand getHand() {
        return hand;
    }
}
