package com.nukateam.ntgl.common.network.message;

import com.nukateam.ntgl.common.network.IMessage;
import com.nukateam.ntgl.common.network.ServerPlayHandler;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraftforge.network.NetworkEvent;

/**
 * Author: MrCrayfish
 */
public class C2SMessageReloadStop implements IMessage<C2SMessageReloadStop> {
    private InteractionHand hand = InteractionHand.MAIN_HAND;

    public C2SMessageReloadStop() {}

    public C2SMessageReloadStop(InteractionHand hand) {
        this.hand = hand;
    }

    @Override
    public void encode(C2SMessageReloadStop message, FriendlyByteBuf buffer) {
        buffer.writeEnum(message.hand);
    }

    @Override
    public C2SMessageReloadStop decode(FriendlyByteBuf buffer) {
        return new C2SMessageReloadStop(
                buffer.readEnum(InteractionHand.class));
    }

    @Override
    public void handle(C2SMessageReloadStop message, NetworkEvent.Context supplier) {
        supplier.enqueueWork((() ->
        {
            ServerPlayer player = supplier.getSender();
            if (player != null && !player.isSpectator()) {
                ServerPlayHandler.handleStopReload(message, player);
            }
        }));
        supplier.setPacketHandled(true);
    }

    public InteractionHand getHand() {
        return hand;
    }
}
