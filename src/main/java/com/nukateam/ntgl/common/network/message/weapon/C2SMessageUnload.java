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
import net.minecraft.world.InteractionHand;

/**
 * Author: MrCrayfish
 */
public class C2SMessageUnload implements CustomPacketPayload {
    public static final Type<C2SMessageUnload> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(Ntgl.MOD_ID, "c2s_message_unload"));

    public static final StreamCodec<RegistryFriendlyByteBuf, C2SMessageUnload> CODEC = StreamCodec.of(
            (buffer, message) -> encode(message, buffer),
            C2SMessageUnload::decode);
    private InteractionHand hand = InteractionHand.MAIN_HAND;
    private WeaponMode weaponMode;

    public C2SMessageUnload(){}

    public C2SMessageUnload(InteractionHand hand, WeaponMode weaponMode) {
        this.hand = hand;
        this.weaponMode = weaponMode;
    }

    public static void encode(C2SMessageUnload message, FriendlyByteBuf buffer) {
        buffer.writeEnum(message.hand);
        buffer.writeUtf(message.weaponMode.toString());
    }

    public static C2SMessageUnload decode(FriendlyByteBuf buffer) {
        return new C2SMessageUnload(
                buffer.readEnum(InteractionHand.class),
                WeaponMode.getType(buffer.readUtf()));
    }

    public static void handle(C2SMessageUnload message, IPayloadContext supplier) {
        supplier.enqueueWork(() ->
            ServerPlayHandler.handleUnload(supplier.player(), message)
        );
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
