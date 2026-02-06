package com.nukateam.chassis_core.common.network.packet;

import com.nukateam.chassis_core.common.network.ActionRegistry;
import com.nukateam.chassis_core.common.network.actions.Action;
import com.nukateam.ntgl.modules.network.IMessage;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

@SuppressWarnings("ALL")
public class C2SGenericPacket implements IMessage<C2SGenericPacket> {
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

    public void handle(C2SGenericPacket message, NetworkEvent.Context context) {
        message.action.doServerAction(message.action, context, message.entityId);
    }
}