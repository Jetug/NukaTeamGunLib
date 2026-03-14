package com.nukateam.ntgl.common.network.message.weapon;

import net.minecraftforge.network.NetworkEvent;
import com.nukateam.ntgl.client.handlers.ClientPlayHandler;
import com.nukateam.ntgl.modules.network.IMessage;
import net.minecraft.network.FriendlyByteBuf;

public class S2CMessageStunGrenade implements IMessage<S2CMessageStunGrenade> {
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
    public void handle(S2CMessageStunGrenade message, NetworkEvent.Context supplier) {
        supplier.enqueueWork(() -> ClientPlayHandler.handleExplosionStunGrenade(message));
        supplier.setPacketHandled(true);
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