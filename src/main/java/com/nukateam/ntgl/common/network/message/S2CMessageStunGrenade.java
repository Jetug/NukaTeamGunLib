package com.nukateam.ntgl.common.network.message;

import com.mrcrayfish.framework.api.network.MessageContext;
import com.nukateam.ntgl.client.ClientPlayHandler;
import com.mrcrayfish.framework.api.network.message.PlayMessage;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class S2CMessageStunGrenade extends PlayMessage<S2CMessageStunGrenade> {
    private double x, y, z;

    public S2CMessageStunGrenade() {
    }

    public S2CMessageStunGrenade(double x, double y, double z) {
        this.z = z;
        this.y = y;
        this.x = x;
    }

    @Override
    public void encode(S2CMessageStunGrenade message, FriendlyByteBuf buffer) {
        buffer.writeDouble(message.x);
        buffer.writeDouble(message.y);
        buffer.writeDouble(message.z);
    }

    @Override
    public S2CMessageStunGrenade decode(FriendlyByteBuf buffer) {
        double x = buffer.readDouble();
        double y = buffer.readDouble();
        double z = buffer.readDouble();
        return new S2CMessageStunGrenade(x, y, z);
    }

    @Override
    public void handle(S2CMessageStunGrenade message, MessageContext supplier) {
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