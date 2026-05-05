package com.nukateam.ntgl.common.network.message.chassis;

import net.neoforged.neoforge.network.handling.IPayloadContext;
import com.nukateam.chassis_core.common.network.ActionRegistry;
import com.nukateam.ntgl.Ntgl;
import com.nukateam.ntgl.common.network.message.chassis.actions.Action;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

@SuppressWarnings("ALL")
public class C2SGenericPacket implements CustomPacketPayload {
    public static final Type<C2SGenericPacket> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(Ntgl.MOD_ID, "c2s_generic_packet"));

    public static final StreamCodec<RegistryFriendlyByteBuf, C2SGenericPacket> CODEC = StreamCodec.of(
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

    public static void handle(C2SGenericPacket message, IPayloadContext context) {
        message.action.doServerAction(message.action, context, message.entityId);
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}