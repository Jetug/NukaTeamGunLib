package com.nukateam.ntgl.modules.data.message;
import com.mrcrayfish.framework.api.network.MessageContext;
import com.nukateam.ntgl.modules.data.DataEntry;
import com.nukateam.ntgl.modules.data.DataKeyManager;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;

import java.util.HashMap;
import java.util.Map;

public class S2CMessageUpdateEntityData {
    public static final StreamCodec<RegistryFriendlyByteBuf, S2CMessageUpdateEntityData> STREAM_CODEC = StreamCodec.of(
            (buffer, message) -> encode(message, buffer),
            buffer -> decode(buffer));

    private Map<DataEntry, EntityData> entries = new HashMap<>();

    public S2CMessageUpdateEntityData() {}

    public S2CMessageUpdateEntityData(Map<DataEntry, EntityData> entries) {
        this.entries = entries;
    }

    public static void encode(S2CMessageUpdateEntityData message, FriendlyByteBuf buffer) {
        buffer.writeVarInt(message.entries.size());
        message.entries.forEach((entry, data) -> {
            buffer.writeBoolean(entry.getValue());
            buffer.writeInt(data.entityId);
            buffer.writeInt(data.dataKeyId);
        });
    }

    public static S2CMessageUpdateEntityData decode(FriendlyByteBuf buffer) {
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

    public static void handle(S2CMessageUpdateEntityData message, MessageContext supplier) {
        supplier.execute((() -> {
            message.entries.forEach(((dataEntry, entityData) -> {
                DataKeyManager.getInstance().setData(dataEntry.getValue(), entityData.dataKeyId, entityData.entityId);
            }));
        }));
        supplier.setHandled(true);
    }

    public record EntityData(int entityId, int dataKeyId){}
}