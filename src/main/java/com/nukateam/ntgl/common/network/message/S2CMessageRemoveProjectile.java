package com.nukateam.ntgl.common.network.message;

import com.mrcrayfish.framework.api.network.MessageContext;
import com.nukateam.ntgl.client.ClientPlayHandler;
import com.mrcrayfish.framework.api.network.message.PlayMessage;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

/**
 * Author: MrCrayfish
 */
public class S2CMessageRemoveProjectile extends PlayMessage<S2CMessageRemoveProjectile> {
    private int entityId;

    public S2CMessageRemoveProjectile() {
    }

    public S2CMessageRemoveProjectile(int entityId) {
        this.entityId = entityId;
    }

    @Override
    public void encode(S2CMessageRemoveProjectile message, FriendlyByteBuf buffer) {
        buffer.writeInt(message.entityId);
    }

    @Override
    public S2CMessageRemoveProjectile decode(FriendlyByteBuf buffer) {
        return new S2CMessageRemoveProjectile(buffer.readInt());
    }

    @Override
    public void handle(S2CMessageRemoveProjectile message, MessageContext supplier) {
        supplier.execute((() -> ClientPlayHandler.handleRemoveProjectile(message)));
        supplier.setHandled(true);
    }

    public int getEntityId() {
        return this.entityId;
    }
}
