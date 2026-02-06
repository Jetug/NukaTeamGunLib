package com.nukateam.ntgl.common.network.message;

import net.minecraftforge.network.NetworkEvent;
import com.nukateam.ntgl.modules.network.IMessage;
import com.nukateam.ntgl.common.data.holders.AnimationType;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.InteractionHand;

import static com.nukateam.ntgl.client.handlers.ClientPlayHandler.*;

public class S2CMessagePlayerAnimation implements IMessage<S2CMessagePlayerAnimation> {
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
    public void handle(S2CMessagePlayerAnimation message, NetworkEvent.Context supplier) {
        supplier.enqueueWork((() -> {
            handleMessageAnimation(message);
        }));
        supplier.setPacketHandled(true);
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
