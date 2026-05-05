package com.nukateam.ntgl.common.network.message.weapon;

import net.neoforged.neoforge.network.handling.IPayloadContext;
import com.nukateam.ntgl.Ntgl;
import com.nukateam.ntgl.common.data.holders.AnimationType;
import com.nukateam.ntgl.common.network.message.chassis.S2CMessageUpdateEquipmentConfig;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;

import static com.nukateam.ntgl.client.handlers.ClientPlayHandler.*;

public class S2CMessagePlayerAnimation implements CustomPacketPayload {
    public static final Type<S2CMessagePlayerAnimation> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(Ntgl.MOD_ID, "s2c_message_player_animation"));

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

    public static void handle(S2CMessagePlayerAnimation message, IPayloadContext supplier) {
        supplier.enqueueWork((() -> {
            handleMessageAnimation(message);
        }));
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

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
