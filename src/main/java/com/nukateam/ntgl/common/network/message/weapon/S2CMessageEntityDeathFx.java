package com.nukateam.ntgl.common.network.message.weapon;

import net.minecraftforge.network.NetworkEvent;
import com.nukateam.ntgl.modules.network.IMessage;
import com.nukateam.ntgl.client.handlers.ClientPlayHandler;
import com.nukateam.ntgl.common.foundation.entity.projectile.GoreData;
import net.minecraft.network.FriendlyByteBuf;

public class S2CMessageEntityDeathFx implements IMessage<S2CMessageEntityDeathFx> {
    private int entityId = -1;
    private GoreData data;

    public S2CMessageEntityDeathFx() {}

    public S2CMessageEntityDeathFx(int entityId, GoreData data) {
        this.entityId = entityId;
        this.data = data;
    }

    @Override
    public void encode(S2CMessageEntityDeathFx message, FriendlyByteBuf buffer) {
        buffer.writeInt(message.entityId);
        buffer.writeNbt(message.data.serializeNBT());
    }

    @Override
    public S2CMessageEntityDeathFx decode(FriendlyByteBuf buffer) {
        var entityId = buffer.readInt();
        var buff = buffer.readNbt();
        var data = new GoreData();
        data.deserializeNBT(buff);
        return new S2CMessageEntityDeathFx(entityId, data);
    }

    @Override
    public void handle(S2CMessageEntityDeathFx message, NetworkEvent.Context supplier) {
        supplier.enqueueWork((() -> ClientPlayHandler.handleEntityDeathFx(message)));
        supplier.setPacketHandled(true);
    }

    public int getEntityId() {
        return this.entityId;
    }

    public GoreData getData() {
        return this.data;
    }
}
