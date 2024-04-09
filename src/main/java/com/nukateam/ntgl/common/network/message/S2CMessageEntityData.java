package com.nukateam.ntgl.common.network.message;

import com.mrcrayfish.framework.api.network.MessageContext;
import com.mrcrayfish.framework.api.network.message.PlayMessage;
import com.nukateam.ntgl.client.ClientPlayHandler;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;

public class S2CMessageEntityData extends PlayMessage<S2CMessageEntityData> {
    private int entityId = -1;
    private CompoundTag data;

    public S2CMessageEntityData() {}

    public S2CMessageEntityData(int entityId, CompoundTag data) {
        this.entityId = entityId;
        this.data = data;
    }

    @Override
    public void encode(S2CMessageEntityData message, FriendlyByteBuf buffer) {
        buffer.writeInt(message.entityId);
        buffer.writeNbt(message.data);
    }

    @Override
    public S2CMessageEntityData decode(FriendlyByteBuf buffer) {
        var entityId = buffer.readInt();
        var buff = buffer.readNbt();
        return new S2CMessageEntityData(entityId, buff);
    }

    @Override
    public void handle(S2CMessageEntityData message, MessageContext supplier) {
        supplier.execute((() -> ClientPlayHandler.handleEntityData(message)));
        supplier.setHandled(true);
    }

    public int getEntityId() {
        return this.entityId;
    }

    public CompoundTag getData() {
        return this.data;
    }
}
