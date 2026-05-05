package com.nukateam.ntgl.common.network.message.weapon;

import com.mrcrayfish.framework.api.network.MessageContext;
import com.nukateam.ntgl.common.network.ServerPlayHandler;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;

/**
 * Author: MrCrayfish
 */
public class C2SMessageCraft  {
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

    public static void handle(C2SMessageCraft message, MessageContext supplier) {
        supplier.execute(() ->
        {
            supplier.execute(() ->
            {
                supplier.getPlayer().ifPresent(player -> {
//                    ServerPlayHandler.handleCraft((ServerPlayer)player, message.id, message.pos);
                });
            });
        });
        supplier.setHandled(true);
    }
}
