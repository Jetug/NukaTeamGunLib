package com.nukateam.ntgl.common.network.message.weapon;

import com.mrcrayfish.framework.api.network.MessageContext;
import com.nukateam.ntgl.common.data.holders.AnimationType;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.InteractionHand;

import static com.nukateam.ntgl.client.handlers.ClientPlayHandler.*;

public class S2CMessagePlayerAnimation  {
    public static final StreamCodec<RegistryFriendlyByteBuf, S2CMessagePlayerAnimation> CODEC = StreamCodec.of(
            (buffer, message) -> encode(message, buffer),
            buffer -> decode(buffer));
    int entityId;
    AnimationType animation;
    InteractionHand hand;

    public S2CMessagePlayerAnimation() {}

    public S2CMessagePlayerAnimation(int entityId, AnimationType animation, InteractionHand hand) {
        this.entityId = entityId;
        this.animation = animation;
        this.hand = hand;
    }

    public static void encode(S2CMessagePlayerAnimation message, FriendlyByteBuf buf) {
        buf.writeInt(message.entityId);
        buf.writeUtf(message.animation.toString());
        buf.writeEnum(message.hand);
    }

    public static S2CMessagePlayerAnimation decode(FriendlyByteBuf buf) {
        return new S2CMessagePlayerAnimation(
                buf.readInt(),
                AnimationType.getType(buf.readUtf()),
                buf.readEnum(InteractionHand.class)
        );
    }

    public static void handle(S2CMessagePlayerAnimation message, MessageContext supplier) {
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
