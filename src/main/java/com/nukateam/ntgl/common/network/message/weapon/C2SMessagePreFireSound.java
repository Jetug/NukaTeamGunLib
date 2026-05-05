package com.nukateam.ntgl.common.network.message.weapon;

import net.neoforged.neoforge.network.handling.IPayloadContext;
import com.nukateam.ntgl.Ntgl;
import com.nukateam.ntgl.common.network.ServerPlayHandler;
import com.nukateam.ntgl.common.network.message.chassis.S2CMessageUpdateEquipmentConfig;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;

/**
 * Author: MrCrayfish
 */
public class C2SMessagePreFireSound implements CustomPacketPayload {
    public static final Type<C2SMessagePreFireSound> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(Ntgl.MOD_ID,"c2s_message_pre_fire_sound"));

    public static final StreamCodec<RegistryFriendlyByteBuf, C2SMessagePreFireSound> CODEC = StreamCodec.of(
            (buffer, message) -> encode(message, buffer),
            buffer -> decode(buffer));

    private InteractionHand hand;

    public C2SMessagePreFireSound() {}

    public C2SMessagePreFireSound(InteractionHand hand) {
        this.hand = hand;
    }

    public static void encode(C2SMessagePreFireSound message, FriendlyByteBuf buffer) {
        buffer.writeEnum(message.hand);
    }

    public static C2SMessagePreFireSound decode(FriendlyByteBuf buffer) {
        return new C2SMessagePreFireSound(buffer.readEnum(InteractionHand.class));
    }

    public static void handle(C2SMessagePreFireSound message, IPayloadContext context) {
        context.enqueueWork(() ->
            ServerPlayHandler.handlePreFireSound(message, (ServerPlayer)context.player())
        );
    }

    public InteractionHand getHand() {
        return hand;
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}