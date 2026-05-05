package com.nukateam.ntgl.common.network.message.weapon;

import com.mrcrayfish.framework.api.network.MessageContext;
import com.nukateam.ntgl.common.network.ServerPlayHandler;
import com.nukateam.ntgl.common.network.enums.HandAction;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.InteractionHand;

public class C2SMessageHandAction  {
    public static final StreamCodec<RegistryFriendlyByteBuf, C2SMessageHandAction> CODEC = StreamCodec.of(
            (buffer, message) -> encode(message, buffer),
            buffer -> decode(buffer));
    private InteractionHand hand = InteractionHand.MAIN_HAND;
    private HandAction handAction;

    public C2SMessageHandAction() {}

    public C2SMessageHandAction(InteractionHand hand, HandAction handAction) {
        this.hand = hand;
        this.handAction = handAction;
    }

    public static void encode(C2SMessageHandAction message, FriendlyByteBuf buffer) {
        buffer.writeEnum(message.hand);
        buffer.writeEnum(message.handAction);
    }

    public static C2SMessageHandAction decode(FriendlyByteBuf buffer) {
        return new C2SMessageHandAction(buffer.readEnum(InteractionHand.class), buffer.readEnum(HandAction.class));
    }

    public static void handle(C2SMessageHandAction message, MessageContext context) {
        context.execute(() -> {
            var player = context.getPlayer().get();
            if (player != null) {
                ServerPlayHandler.handleHandAction(message, player);
            }
        });
        context.setHandled(true);
    }

    public InteractionHand getHand() {
        return hand;
    }

    public HandAction getHandAction() {
        return handAction;
    }
}
