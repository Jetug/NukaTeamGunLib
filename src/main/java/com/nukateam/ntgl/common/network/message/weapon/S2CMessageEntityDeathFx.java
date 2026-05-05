package com.nukateam.ntgl.common.network.message.weapon;

import net.neoforged.neoforge.network.handling.IPayloadContext;
import com.nukateam.ntgl.Ntgl;
import com.nukateam.ntgl.client.handlers.ClientPlayHandler;
import com.nukateam.ntgl.common.foundation.entity.projectile.GoreData;
import com.nukateam.ntgl.common.network.message.chassis.S2CMessageUpdateEquipmentConfig;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public class S2CMessageEntityDeathFx implements CustomPacketPayload {
    public static final Type<S2CMessageEntityDeathFx> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(Ntgl.MOD_ID, "s2c_message_entity_death_fx"));

    public static final StreamCodec<RegistryFriendlyByteBuf, S2CMessageEntityDeathFx> CODEC = StreamCodec.of(
            (buffer, message) -> encode(message, buffer),
            buffer -> decode(buffer));
    private int entityId = -1;
    private GoreData data;

    public S2CMessageEntityDeathFx() {}

    public S2CMessageEntityDeathFx(int entityId, GoreData data) {
        this.entityId = entityId;
        this.data = data;
    }

    public static void encode(S2CMessageEntityDeathFx message, FriendlyByteBuf buffer) {
        buffer.writeInt(message.entityId);
        buffer.writeNbt(message.data.serializeNBT(null));
    }

    public static S2CMessageEntityDeathFx decode(FriendlyByteBuf buffer) {
        var entityId = buffer.readInt();
        var buff = buffer.readNbt();
        var data = new GoreData();
        data.deserializeNBT(null, buff);
        return new S2CMessageEntityDeathFx(entityId, data);
    }

    public static void handle(S2CMessageEntityDeathFx message, IPayloadContext supplier) {
        supplier.enqueueWork((() -> ClientPlayHandler.handleEntityDeathFx(message)));
    }

    public int getEntityId() {
        return this.entityId;
    }

    public GoreData getData() {
        return this.data;
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
