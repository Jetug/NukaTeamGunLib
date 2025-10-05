package com.nukateam.chassis_core.common.network.packet;

import com.nukateam.chassis_core.common.network.ActionRegistry;
import com.nukateam.chassis_core.common.network.actions.Action;
import com.mrcrayfish.framework.api.network.MessageContext;
import com.mrcrayfish.framework.api.network.message.PlayMessage;
import net.minecraft.network.FriendlyByteBuf;

@SuppressWarnings("ALL")
public class C2SGenericPacket extends PlayMessage<C2SGenericPacket> {
    int entityId = -1;
    Action action = null;

    public C2SGenericPacket(int entityId, Action action) {
        this.entityId = entityId;
        this.action = action;
    }

    public C2SGenericPacket() {
    }

    public void encode(C2SGenericPacket message, FriendlyByteBuf buffer) {
        buffer.writeInt(message.entityId);
        buffer.writeInt(message.action.getId());
        message.action.write(buffer);
    }

    public C2SGenericPacket decode(FriendlyByteBuf buffer) {
        var entityId = buffer.readInt();
        var action = ActionRegistry.getAction(buffer.readInt());
        return new C2SGenericPacket(entityId, action.read(buffer));
    }

    public void handle(C2SGenericPacket message, MessageContext context) {
        message.action.doServerAction(message.action, context, message.entityId);
    }
}