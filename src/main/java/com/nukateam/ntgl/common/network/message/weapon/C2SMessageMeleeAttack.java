package com.nukateam.ntgl.common.network.message.weapon;

import net.neoforged.neoforge.network.handling.IPayloadContext;
import com.nukateam.ntgl.Ntgl;
import com.nukateam.ntgl.common.data.holders.WeaponMode;
import com.nukateam.ntgl.common.network.ServerPlayHandler;
import com.nukateam.ntgl.common.network.message.chassis.S2CMessageUpdateEquipmentConfig;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;

public class C2SMessageMeleeAttack implements CustomPacketPayload {
    public static final Type<C2SMessageMeleeAttack> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(Ntgl.MOD_ID, "c2s_message_melee_attack"));

    public static final StreamCodec<RegistryFriendlyByteBuf, C2SMessageMeleeAttack> CODEC = StreamCodec.of(
            (buffer, message) -> encode(message, buffer),
            buffer -> decode(buffer));

    InteractionHand hand;
    WeaponMode action;

    public C2SMessageMeleeAttack() {}

    public C2SMessageMeleeAttack(InteractionHand hand, WeaponMode action) {
        this.hand = hand;
        this.action = action;
    }

    public static void encode(C2SMessageMeleeAttack message, FriendlyByteBuf buffer) {
        buffer.writeEnum(message.hand);
        buffer.writeUtf(message.action.toString());
    }

    public static C2SMessageMeleeAttack decode(FriendlyByteBuf buffer) {
        return new C2SMessageMeleeAttack(
                buffer.readEnum(InteractionHand.class),
                WeaponMode.getType(buffer.readUtf())
        );
    }

    public static void handle(C2SMessageMeleeAttack message, IPayloadContext context) {
        context.enqueueWork(() -> {
            context.enqueueWork(() ->
                ServerPlayHandler.handleMeleeAttack(message, context.player())
            );
        });
    }

    public InteractionHand getHand() {
        return hand;
    }

    public WeaponMode getAction() {
        return action;
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
