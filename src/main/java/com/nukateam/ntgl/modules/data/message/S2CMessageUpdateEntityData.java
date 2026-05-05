package com.nukateam.ntgl.modules.data.message;
import com.nukateam.ntgl.Ntgl;
import com.nukateam.ntgl.modules.data.DataEntry;
import com.nukateam.ntgl.modules.data.DataKeyManager;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import java.util.HashMap;
import java.util.Map;

public class S2CMessageUpdateEntityData implements CustomPacketPayload {
    public static final Type<S2CMessageUpdateEntityData> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(Ntgl.MOD_ID, "s2c_message_update_entity_data"));

    public static final StreamCodec<RegistryFriendlyByteBuf, S2CMessageUpdateEntityData> CODEC = StreamCodec.of(
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

    public static void handle(S2CMessageUpdateEntityData message, IPayloadContext supplier) {
        supplier.enqueueWork((() -> {
            message.entries.forEach(((dataEntry, entityData) -> {
                DataKeyManager.getInstance().setData(dataEntry.getValue(), entityData.dataKeyId, entityData.entityId);
            }));
        }));
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public record EntityData(int entityId, int dataKeyId){}
}