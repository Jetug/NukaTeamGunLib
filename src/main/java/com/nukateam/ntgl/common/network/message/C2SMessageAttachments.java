package com.nukateam.ntgl.common.network.message;

import net.minecraftforge.network.NetworkEvent;
import com.nukateam.ntgl.common.network.IMessage;
import com.nukateam.ntgl.common.network.ServerPlayHandler;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;

/**
 * Author: MrCrayfish
 */
public class C2SMessageAttachments implements IMessage<C2SMessageAttachments> {
    public C2SMessageAttachments() {
    }

    @Override
    public void encode(C2SMessageAttachments message, FriendlyByteBuf buffer) {
    }

    @Override
    public C2SMessageAttachments decode(FriendlyByteBuf buffer) {
        return new C2SMessageAttachments();
    }

    @Override
    public void handle(C2SMessageAttachments message, NetworkEvent.Context supplier) {
        supplier.enqueueWork((() -> {
            ServerPlayer player = supplier.getSender();
            if (player != null) {
                ServerPlayHandler.handleAttachments(player);
            }
        }));
        supplier.setPacketHandled(true);
    }
}
