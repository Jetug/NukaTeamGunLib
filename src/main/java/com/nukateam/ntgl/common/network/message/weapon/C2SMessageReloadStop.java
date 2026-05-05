package com.nukateam.ntgl.common.network.message.weapon;

import net.neoforged.neoforge.network.handling.IPayloadContext;
import com.nukateam.ntgl.Ntgl;
import com.nukateam.ntgl.common.network.ServerPlayHandler;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;

/**
 * Author: MrCrayfish
 */
public class C2SMessageReloadStop implements CustomPacketPayload {
    public static final Type<C2SMessageReloadStop> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(Ntgl.MOD_ID, "c2s_message_reload_stop"));

    public static final StreamCodec<RegistryFriendlyByteBuf, C2SMessageReloadStop> CODEC = StreamCodec.of(
            (buffer, message) -> encode(message, buffer),
            buffer -> decode(buffer));

    private InteractionHand hand = InteractionHand.MAIN_HAND;

    public C2SMessageReloadStop() {}

    public C2SMessageReloadStop(InteractionHand hand) {
        this.hand = hand;
    }

    public static void encode(C2SMessageReloadStop message, FriendlyByteBuf buffer) {
        buffer.writeEnum(message.hand);
    }

    public static C2SMessageReloadStop decode(FriendlyByteBuf buffer) {
        return new C2SMessageReloadStop(
                buffer.readEnum(InteractionHand.class));
    }

    public static void handle(C2SMessageReloadStop message, IPayloadContext supplier) {
        supplier.enqueueWork((() ->
        {
            var player = supplier.player();
            if (player != null && !player.isSpectator()) {
                ServerPlayHandler.handleStopReload(message, player);
            }
        }));
    }

    public InteractionHand getHand() {
        return hand;
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
