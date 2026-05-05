package com.nukateam.ntgl.common.network.message.weapon;

import net.neoforged.neoforge.network.handling.IPayloadContext;
import com.nukateam.ntgl.Ntgl;
import com.nukateam.ntgl.common.network.ServerPlayHandler;
import com.nukateam.ntgl.common.network.enums.HandAction;
import com.nukateam.ntgl.common.network.message.chassis.S2CMessageUpdateEquipmentConfig;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;

public class C2SMessageHandAction implements CustomPacketPayload {
    public static final Type<C2SMessageHandAction> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(Ntgl.MOD_ID, "c2s_message_hand_action"));

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

    public static void handle(C2SMessageHandAction message, IPayloadContext context) {
        context.enqueueWork(() -> {
            var player = context.player();
            if (player != null) {
                ServerPlayHandler.handleHandAction(message, player);
            }
        });
    }

    public InteractionHand getHand() {
        return hand;
    }

    public HandAction getHandAction() {
        return handAction;
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
