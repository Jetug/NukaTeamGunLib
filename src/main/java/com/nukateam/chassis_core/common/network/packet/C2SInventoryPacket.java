package com.nukateam.chassis_core.common.network.packet;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class C2SInventoryPacket {
    public int chassisId = -1;

    public C2SInventoryPacket(int chassisId) {
        this.chassisId = chassisId;
    }

    public C2SInventoryPacket() {
    }

    public static void write(C2SInventoryPacket message, FriendlyByteBuf buffer) {
        buffer.writeInt(message.chassisId);
    }

    public static C2SInventoryPacket read(FriendlyByteBuf buffer) {
        var entityId = buffer.readInt();
        return new C2SInventoryPacket(entityId);
    }

    public static void handle(C2SInventoryPacket message, Supplier<NetworkEvent.Context> context) {
        var player = context.get().getSender();
    }
}