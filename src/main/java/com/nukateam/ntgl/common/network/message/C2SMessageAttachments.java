package com.nukateam.ntgl.common.network.message;

import com.mrcrayfish.framework.api.network.MessageContext;
import com.mrcrayfish.framework.api.network.message.PlayMessage;
import com.nukateam.ntgl.common.base.network.ServerPlayHandler;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

/**
 * Author: MrCrayfish
 */
public class C2SMessageAttachments extends PlayMessage<C2SMessageAttachments> {
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
    public void handle(C2SMessageAttachments message, MessageContext supplier) {
        supplier.execute((() -> {
            ServerPlayer player = supplier.getPlayer();
            if (player != null) {
                ServerPlayHandler.handleAttachments(player);
            }
        }));
        supplier.setHandled(true);
    }
}
