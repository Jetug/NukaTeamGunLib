package com.nukateam.ntgl.common.network.message.weapon;

import net.neoforged.neoforge.network.handling.IPayloadContext;
import com.nukateam.ntgl.Ntgl;
import com.nukateam.ntgl.common.data.holders.WeaponMode;
import com.nukateam.ntgl.common.network.ServerPlayHandler;
import com.nukateam.ntgl.common.network.enums.KeyAction;
import com.nukateam.ntgl.common.network.message.chassis.S2CMessageUpdateEquipmentConfig;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;

public class C2SMessageGrenade implements CustomPacketPayload {
    public static final Type<C2SMessageGrenade> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(Ntgl.MOD_ID, "c2s_message_grenade"));

    public static final StreamCodec<RegistryFriendlyByteBuf, C2SMessageGrenade> CODEC = StreamCodec.of(
            (buffer, message) -> encode(message, buffer),
            buffer -> decode(buffer));
    private KeyAction action;
    private InteractionHand hand = InteractionHand.MAIN_HAND;
    WeaponMode weaponMode;

    public C2SMessageGrenade() {}

    public C2SMessageGrenade(KeyAction reload, InteractionHand hand, WeaponMode weaponMode) {
        this.action = reload;
        this.hand = hand;
        this.weaponMode = weaponMode;
    }

    public static void encode(C2SMessageGrenade message, FriendlyByteBuf buffer) {
        buffer.writeEnum(message.action);
        buffer.writeEnum(message.hand);
        buffer.writeUtf(message.weaponMode.toString());

    }

    public static C2SMessageGrenade decode(FriendlyByteBuf buffer) {
        return new C2SMessageGrenade(
                buffer.readEnum(KeyAction.class),
                buffer.readEnum(InteractionHand.class),
                WeaponMode.getType(buffer.readUtf()));
    }

    public static void handle(C2SMessageGrenade message, IPayloadContext supplier) {
        supplier.enqueueWork((() ->
        {
            var player = supplier.player();
            if (player != null && !player.isSpectator()) {
                ServerPlayHandler.handleGrenade(message, player);
            }
        }));
    }

    public KeyAction getAction() {
        return action;
    }

    public InteractionHand getHand() {
        return hand;
    }

    public WeaponMode getAttackMode() {
        return weaponMode;
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
