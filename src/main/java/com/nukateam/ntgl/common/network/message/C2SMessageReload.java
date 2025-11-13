package com.nukateam.ntgl.common.network.message;

import net.minecraftforge.network.NetworkEvent;
import com.nukateam.ntgl.common.network.ServerPlayHandler;
import com.nukateam.ntgl.common.network.IMessage;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;

/**
 * Author: MrCrayfish
 */
public class C2SMessageReload implements IMessage<C2SMessageReload> {
    private boolean reload;
    private InteractionHand hand = InteractionHand.MAIN_HAND;

    public C2SMessageReload() {}

    public C2SMessageReload(boolean reload, InteractionHand hand) {
        this.reload = reload;
        this.hand = hand;
    }

    @Override
    public void encode(C2SMessageReload message, FriendlyByteBuf buffer) {
        buffer.writeBoolean(message.reload);
        buffer.writeEnum(message.hand);
    }

    @Override
    public C2SMessageReload decode(FriendlyByteBuf buffer) {
        return new C2SMessageReload(buffer.readBoolean(), buffer.readEnum(InteractionHand.class));
    }

    @Override
    public void handle(C2SMessageReload message, NetworkEvent.Context supplier) {
        supplier.enqueueWork((() ->
        {
            ServerPlayer player = supplier.getSender();
            if (player != null && !player.isSpectator()) {
                ServerPlayHandler.handleReload(message, player);
            }
        }));
        supplier.setPacketHandled(true);
    }

    public boolean isReload() {
        return reload;
    }

    public InteractionHand getHand() {
        return hand;
    }
}
