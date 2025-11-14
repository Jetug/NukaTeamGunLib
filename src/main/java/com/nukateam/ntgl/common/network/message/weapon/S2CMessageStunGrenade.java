package com.nukateam.ntgl.common.network.message.weapon;

import com.mrcrayfish.framework.api.network.MessageContext;
import com.nukateam.ntgl.client.handlers.ClientPlayHandler;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;

public class S2CMessageStunGrenade  {
    public static final StreamCodec<RegistryFriendlyByteBuf, S2CMessageStunGrenade> STREAM_CODEC = StreamCodec.of(
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

    public static void handle(S2CMessageStunGrenade message, MessageContext supplier) {
        supplier.execute(() -> ClientPlayHandler.handleExplosionStunGrenade(message));
        supplier.setHandled(true);
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
}