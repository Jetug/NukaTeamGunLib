package com.nukateam.ntgl.common.network.message;

import com.mrcrayfish.framework.api.network.MessageContext;
import com.mrcrayfish.framework.api.network.message.PlayMessage;
import com.nukateam.ntgl.client.handlers.ClientPlayHandler;
import com.nukateam.ntgl.common.foundation.entity.projectile.GoreData;
import net.minecraft.network.FriendlyByteBuf;

public class S2CMessageEntityDeathFx extends PlayMessage<S2CMessageEntityDeathFx> {
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
    public void handle(S2CMessageEntityDeathFx message, MessageContext supplier) {
        supplier.execute((() -> ClientPlayHandler.handleEntityDeathFx(message)));
        supplier.setHandled(true);
    }

    public int getEntityId() {
        return this.entityId;
    }

    public GoreData getData() {
        return this.data;
    }
}
