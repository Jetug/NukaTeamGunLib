package com.nukateam.ntgl.common.network.message;

import com.mrcrayfish.framework.api.network.MessageContext;
import com.mrcrayfish.framework.api.network.message.PlayMessage;
import com.nukateam.ntgl.common.data.holders.AnimationType;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.InteractionHand;

import static com.nukateam.ntgl.client.handlers.ClientPlayHandler.*;

public class S2CMessagePlayerAnimation extends PlayMessage<S2CMessagePlayerAnimation> {
    int entityId;
    AnimationType animation;
    InteractionHand hand;

    public S2CMessagePlayerAnimation() {}

    public S2CMessagePlayerAnimation(int entityId, AnimationType animation, InteractionHand hand) {
        this.entityId = entityId;
        this.animation = animation;
        this.hand = hand;
    }

    @Override
    public void encode(S2CMessagePlayerAnimation message, FriendlyByteBuf buf) {
        buf.writeInt(message.entityId);
        buf.writeUtf(message.animation.toString());
        buf.writeEnum(message.hand);
    }

    @Override
    public S2CMessagePlayerAnimation decode(FriendlyByteBuf buf) {
        return new S2CMessagePlayerAnimation(
                buf.readInt(),
                AnimationType.getType(buf.readUtf()),
                buf.readEnum(InteractionHand.class)
        );
    }

    @Override
    public void handle(S2CMessagePlayerAnimation message, MessageContext supplier) {
        supplier.execute((() -> {
            handleMessageAnimation(message);
        }));
        supplier.setHandled(true);
    }

    public int getEntityId() {
        return entityId;
    }

    public AnimationType getAnimation() {
        return animation;
    }

    public InteractionHand getHand() {
        return hand;
    }
}
