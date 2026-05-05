package com.nukateam.ntgl.common.network.message.weapon;

import net.neoforged.neoforge.network.handling.IPayloadContext;
import com.nukateam.ntgl.Ntgl;
import com.nukateam.ntgl.common.network.ServerPlayHandler;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;

/**
 * Author: MrCrayfish
 */
public class C2SMessageCraft implements CustomPacketPayload {
    public static final Type<C2SMessageCraft> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(Ntgl.MOD_ID, "c2s_message_craft"));

    public static final StreamCodec<RegistryFriendlyByteBuf, C2SMessageCraft> CODEC = StreamCodec.of(
            (buffer, message) -> encode(message, buffer),
            buffer -> decode(buffer));

    private ResourceLocation id;
    private BlockPos pos;

    public C2SMessageCraft() {
    }

    public C2SMessageCraft(ResourceLocation id, BlockPos pos) {
        this.id = id;
        this.pos = pos;
    }

    public static void encode(C2SMessageCraft message, FriendlyByteBuf buffer) {
        buffer.writeResourceLocation(message.id);
        buffer.writeBlockPos(message.pos);
    }

    public static C2SMessageCraft decode(FriendlyByteBuf buffer) {
        return new C2SMessageCraft(buffer.readResourceLocation(), buffer.readBlockPos());
    }

    public static void handle(C2SMessageCraft message, IPayloadContext supplier) {
        supplier.enqueueWork(() ->
        {
            supplier.enqueueWork(() ->
            {
//                    ServerPlayHandler.handleCraft((ServerPlayer)supplier.player(), message.id, message.pos);
            });
        });
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
