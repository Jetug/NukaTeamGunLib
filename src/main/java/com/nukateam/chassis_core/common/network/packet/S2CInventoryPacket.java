package com.nukateam.chassis_core.common.network.packet;

import com.nukateam.chassis_core.common.foundation.entity.WearableChassis;
import com.nukateam.ntgl.modules.network.IMessage;
import net.minecraft.client.Minecraft;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

public class S2CInventoryPacket implements IMessage<S2CInventoryPacket> {
    public int chassisId = -1;
    public ListTag inventory;
    public static final String INVENTORY = "inventory";

    public S2CInventoryPacket() {}

    public S2CInventoryPacket(int chassisId, ListTag inventory) {
        this.chassisId = chassisId;
        this.inventory = inventory;
    }

    public void encode(S2CInventoryPacket message, FriendlyByteBuf buffer) {
        var nbt = new CompoundTag();
        nbt.put(INVENTORY, message.inventory);
        buffer.writeInt(message.chassisId);
        buffer.writeNbt(nbt);
    }

    public S2CInventoryPacket decode(FriendlyByteBuf buffer) {
        var entityId = buffer.readInt();
        var inventory = (ListTag) buffer.readNbt().get(INVENTORY);
        return new S2CInventoryPacket(entityId, inventory);
    }

    public void handle(S2CInventoryPacket message, NetworkEvent.Context supplier) {
        supplier.enqueueWork((() ->
        {
            var player = Minecraft.getInstance().player;
            if (player != null) {
                var entity = player.level().getEntity(message.chassisId);

                if (entity instanceof WearableChassis powerArmor)
                    powerArmor.setArmorData(message.inventory);
            }
        }));
        supplier.setPacketHandled(true);

    }
}