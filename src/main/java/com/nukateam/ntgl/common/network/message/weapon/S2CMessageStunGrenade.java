package com.nukateam.ntgl.common.network.message.weapon;

import net.neoforged.neoforge.network.handling.IPayloadContext;
import com.nukateam.ntgl.Ntgl;
import com.nukateam.ntgl.client.handlers.ClientPlayHandler;
import com.nukateam.ntgl.common.network.message.chassis.S2CMessageUpdateEquipmentConfig;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public class S2CMessageStunGrenade implements CustomPacketPayload {
    public static final Type<S2CMessageStunGrenade> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(Ntgl.MOD_ID, "s2c_message_stun_grenade"));

    public static final StreamCodec<RegistryFriendlyByteBuf, S2CMessageStunGrenade> CODEC = StreamCodec.of(
            (buffer, message) -> encode(message, buffer),
            buffer -> decode(buffer));

    private double x, y, z;

    public S2CMessageStunGrenade() {
    }

    public S2CMessageStunGrenade(double x, double y, double z) {
        this.z = z;
        this.y = y;
        this.x = x;
    }

    public static void encode(S2CMessageStunGrenade message, FriendlyByteBuf buffer) {
        buffer.writeDouble(message.x);
        buffer.writeDouble(message.y);
        buffer.writeDouble(message.z);
    }

    public static S2CMessageStunGrenade decode(FriendlyByteBuf buffer) {
        double x = buffer.readDouble();
        double y = buffer.readDouble();
        double z = buffer.readDouble();
        return new S2CMessageStunGrenade(x, y, z);
    }

    public static void handle(S2CMessageStunGrenade message, IPayloadContext supplier) {
        supplier.enqueueWork(() -> ClientPlayHandler.handleExplosionStunGrenade(message));
    }

    public double getX() {
        return x;
    }

    public double getY() {
        return y;
    }

    public double getZ() {
        return z;
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}