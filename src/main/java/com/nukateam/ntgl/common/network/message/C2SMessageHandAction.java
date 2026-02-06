package com.nukateam.ntgl.common.network.message;

import net.minecraftforge.network.NetworkEvent;
import com.nukateam.ntgl.modules.network.IMessage;
import com.nukateam.ntgl.common.network.ServerPlayHandler;
import com.nukateam.ntgl.common.network.enums.HandAction;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.InteractionHand;

public class C2SMessageHandAction implements IMessage<C2SMessageHandAction> {
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
    public void handle(C2SMessageHandAction message, NetworkEvent.Context context) {
        context.enqueueWork(() -> {
            var player = context.getSender();
            if (player != null) {
                ServerPlayHandler.handleHandAction(message, player);
            }
        });
        context.setPacketHandled(true);
    }

    public InteractionHand getHand() {
        return hand;
    }

    public HandAction getHandAction() {
        return handAction;
    }
}
