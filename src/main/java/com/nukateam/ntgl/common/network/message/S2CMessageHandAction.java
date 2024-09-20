package com.nukateam.ntgl.common.network.message;

import com.mrcrayfish.framework.api.network.MessageContext;
import com.mrcrayfish.framework.api.network.message.PlayMessage;
import com.nukateam.ntgl.common.base.network.ServerPlayHandler;
import com.nukateam.ntgl.common.network.HandAction;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.HumanoidArm;

public class S2CMessageHandAction extends PlayMessage<S2CMessageHandAction> {
    private InteractionHand hand = InteractionHand.MAIN_HAND;
    private HandAction handAction;

    public S2CMessageHandAction() {}

    public S2CMessageHandAction(InteractionHand hand, HandAction handAction) {
        this.hand = hand;
        this.handAction = handAction;
    }

    @Override
    public void encode(S2CMessageHandAction message, FriendlyByteBuf buffer) {
        buffer.writeEnum(message.hand);
        buffer.writeEnum(message.handAction);
    }

    @Override
    public S2CMessageHandAction decode(FriendlyByteBuf buffer) {
        return new S2CMessageHandAction(buffer.readEnum(InteractionHand.class), buffer.readEnum(HandAction.class));
    }

    @Override
    public void handle(S2CMessageHandAction message, MessageContext context) {
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
