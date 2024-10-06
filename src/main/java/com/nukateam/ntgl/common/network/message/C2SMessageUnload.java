package com.nukateam.ntgl.common.network.message;

import com.mrcrayfish.framework.api.network.MessageContext;
import com.nukateam.ntgl.common.network.ServerPlayHandler;
import com.mrcrayfish.framework.api.network.message.PlayMessage;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.InteractionHand;

/**
 * Author: MrCrayfish
 */
public class C2SMessageUnload extends PlayMessage<C2SMessageUnload> {
    private InteractionHand hand = InteractionHand.MAIN_HAND;

    public C2SMessageUnload(){

    }

    public C2SMessageUnload(InteractionHand hand) {
        this.hand = hand;
    }


    @Override
    public void encode(C2SMessageUnload message, FriendlyByteBuf buffer) {
        buffer.writeEnum(message.hand);
    }

    @Override
    public C2SMessageUnload decode(FriendlyByteBuf buffer) {
        return new C2SMessageUnload(buffer.readEnum(InteractionHand.class));
    }

    @Override
    public void handle(C2SMessageUnload message, MessageContext supplier) {
        supplier.execute((() -> {
            var player = supplier.getPlayer();
            if (player != null && !player.isSpectator()) {
                ServerPlayHandler.handleUnload(player, message.hand);
            }
        }));
        supplier.setHandled(true);
    }
}
