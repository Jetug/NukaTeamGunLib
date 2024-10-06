package com.nukateam.ntgl.common.network.message;

import com.mrcrayfish.framework.api.network.MessageContext;
import com.nukateam.ntgl.common.network.ServerPlayHandler;
import com.mrcrayfish.framework.api.network.message.PlayMessage;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;

/**
 * Author: MrCrayfish
 */
public class C2SMessageCraft extends PlayMessage<C2SMessageCraft> {
    private ResourceLocation id;
    private BlockPos pos;

    public C2SMessageCraft() {
    }

    public C2SMessageCraft(ResourceLocation id, BlockPos pos) {
        this.id = id;
        this.pos = pos;
    }

    @Override
    public void encode(C2SMessageCraft message, FriendlyByteBuf buffer) {
        buffer.writeResourceLocation(message.id);
        buffer.writeBlockPos(message.pos);
    }

    @Override
    public C2SMessageCraft decode(FriendlyByteBuf buffer) {
        return new C2SMessageCraft(buffer.readResourceLocation(), buffer.readBlockPos());
    }

    @Override
    public void handle(C2SMessageCraft message, MessageContext supplier) {
        supplier.execute(() ->
        {
            ServerPlayer player = supplier.getPlayer();
            if (player != null) {
                ServerPlayHandler.handleCraft(player, message.id, message.pos);
            }
        });
        supplier.setHandled(true);
    }
}
