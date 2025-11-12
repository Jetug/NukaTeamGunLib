package com.nukateam.chassis_core.common.network.packet;

import com.nukateam.chassis_core.common.foundation.entity.WearableChassis;
import com.mrcrayfish.framework.api.network.MessageContext;
import com.mrcrayfish.framework.api.network.message.PlayMessage;
import net.minecraft.client.Minecraft;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.FriendlyByteBuf;

public class S2CInventoryPacket extends PlayMessage<S2CInventoryPacket> {
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

    public void handle(S2CInventoryPacket message, MessageContext supplier) {
        supplier.execute((() ->
        {
            var player = Minecraft.getInstance().player;
            if (player != null) {
                var entity = player.level().getEntity(message.chassisId);

                if (entity instanceof WearableChassis powerArmor)
                    powerArmor.setArmorData(message.inventory);
            }
        }));
        supplier.setHandled(true);

    }
}