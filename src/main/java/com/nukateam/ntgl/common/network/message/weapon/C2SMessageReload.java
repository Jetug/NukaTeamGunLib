package com.nukateam.ntgl.common.network.message.weapon;

import com.nukateam.ntgl.Ntgl;
import com.nukateam.ntgl.common.data.holders.WeaponMode;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import com.nukateam.ntgl.common.network.ServerPlayHandler;
import com.nukateam.ntgl.common.network.message.chassis.S2CMessageUpdateEquipmentConfig;
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
public class C2SMessageReload implements CustomPacketPayload {
    public static final Type<C2SMessageReload> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(Ntgl.MOD_ID, "c2s_message_reload"));

    public static final StreamCodec<RegistryFriendlyByteBuf, C2SMessageReload> CODEC = StreamCodec.of(
            (buffer, message) -> encode(message, buffer),
            buffer -> decode(buffer));
    private boolean reload;
    private InteractionHand hand = InteractionHand.MAIN_HAND;
    WeaponMode weaponMode;

    public C2SMessageReload() {}

    public C2SMessageReload(InteractionHand hand, WeaponMode weaponMode) {
        this.hand = hand;
        this.weaponMode = weaponMode;
    }

    public static void encode(C2SMessageReload message, FriendlyByteBuf buffer) {
        buffer.writeEnum(message.hand);
        buffer.writeUtf(message.weaponMode.toString());
    }

    public static C2SMessageReload decode(FriendlyByteBuf buffer) {
        return new C2SMessageReload(
                buffer.readEnum(InteractionHand.class),
                WeaponMode.getType(buffer.readUtf()));
    }

    public static void handle(C2SMessageReload message, IPayloadContext supplier) {
        supplier.enqueueWork((() ->
        {
            var player = supplier.player();
            if (player != null && !player.isSpectator()) {
                ServerPlayHandler.handleReload(message, (ServerPlayer)player);
            }
        }));
    }

    public InteractionHand getHand() {
        return hand;
    }

    public WeaponMode getWeaponMode() {
        return weaponMode;
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
