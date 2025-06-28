package com.nukateam.ntgl.common.network.message;

import com.mrcrayfish.framework.api.network.MessageContext;
import com.mrcrayfish.framework.api.network.message.PlayMessage;
import com.nukateam.ntgl.common.network.ServerPlayHandler;
import com.nukateam.ntgl.common.network.HandAction;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.InteractionHand;

public class C2SMessageHandAction extends PlayMessage<C2SMessageHandAction> {
    private InteractionHand hand = InteractionHand.MAIN_HAND;
    private HandAction handAction;

    public C2SMessageHandAction() {}

    public C2SMessageHandAction(InteractionHand hand, HandAction handAction) {
        this.hand = hand;
        this.handAction = handAction;
    }

    @Override
    public void encode(C2SMessageHandAction message, FriendlyByteBuf buffer) {
        buffer.writeEnum(message.hand);
        buffer.writeEnum(message.handAction);
    }

    @Override
    public C2SMessageHandAction decode(FriendlyByteBuf buffer) {
        return new C2SMessageHandAction(buffer.readEnum(InteractionHand.class), buffer.readEnum(HandAction.class));
    }

    @Override
    public void handle(C2SMessageHandAction message, MessageContext context) {
        context.execute(() -> {
            var player = context.getPlayer();
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
