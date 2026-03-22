package com.nukateam.ntgl.modules.data.message;

import com.nukateam.ntgl.modules.data.DataEntry;
import com.nukateam.ntgl.modules.data.DataKeyManager;
import com.nukateam.ntgl.modules.network.IMessage;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import java.util.HashMap;
import java.util.Map;

public class S2CMessageUpdateEntityData implements IMessage<S2CMessageUpdateEntityData> {
    private Map<DataEntry, EntityData> entries = new HashMap<>();

    public S2CMessageUpdateEntityData() {}

    public S2CMessageUpdateEntityData(Map<DataEntry, EntityData> entries) {
        this.entries = entries;
    }

    @Override
    public void encode(S2CMessageUpdateEntityData message, FriendlyByteBuf buffer) {
        buffer.writeVarInt(message.entries.size());
        message.entries.forEach((entry, data) -> {
            buffer.writeBoolean(entry.getValue());
            buffer.writeInt(data.entityId);
            buffer.writeInt(data.dataKeyId);
        });
    }

    @Override
    public S2CMessageUpdateEntityData decode(FriendlyByteBuf buffer) {
        var size = buffer.readVarInt();
        var entries = new HashMap<DataEntry, EntityData>();

        for (int i = 0; i < size; i++) {
            var value = buffer.readBoolean();
            var entityId = buffer.readInt();
            var dataKeyId = buffer.readInt();
            var dataEntry = new DataEntry();

            dataEntry.setValue(value);
            entries.put(dataEntry, new EntityData(entityId, dataKeyId));
        }
        return new S2CMessageUpdateEntityData(entries);
    }

    @Override
    public void handle(S2CMessageUpdateEntityData message, NetworkEvent.Context supplier) {
        supplier.enqueueWork((() -> {
            message.entries.forEach(((dataEntry, entityData) -> {
                DataKeyManager.getInstance().setData(dataEntry.getValue(), entityData.dataKeyId, entityData.entityId);
            }));
        }));
        supplier.setPacketHandled(true);
    }

    public record EntityData(int entityId, int dataKeyId){}
}