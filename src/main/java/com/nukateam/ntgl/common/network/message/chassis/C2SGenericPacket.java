package com.nukateam.ntgl.common.network.message.chassis;

import com.mrcrayfish.framework.api.network.MessageContext;
import com.nukateam.chassis_core.common.network.ActionRegistry;
import com.nukateam.ntgl.common.network.message.chassis.actions.Action;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;

@SuppressWarnings("ALL")
public class C2SGenericPacket  {
    public static final StreamCodec<RegistryFriendlyByteBuf, C2SGenericPacket> STREAM_CODEC = StreamCodec.of(
            (buffer, message) -> encode(message, buffer),
            buffer -> decode(buffer));

    int entityId = -1;
    Action action = null;

    public C2SGenericPacket(int entityId, Action action) {
        this.entityId = entityId;
        this.action = action;
    }

    public C2SGenericPacket() {
    }

    public static void encode(C2SGenericPacket message, FriendlyByteBuf buffer) {
        buffer.writeInt(message.entityId);
        buffer.writeInt(message.action.getId());
        message.action.write(buffer);
    }

    public static C2SGenericPacket decode(FriendlyByteBuf buffer) {
        var entityId = buffer.readInt();
        var action = ActionRegistry.getAction(buffer.readInt());
        return new C2SGenericPacket(entityId, action.read(buffer));
    }

    public static void handle(C2SGenericPacket message, MessageContext context) {
        message.action.doServerAction(message.action, context, message.entityId);
    }
}