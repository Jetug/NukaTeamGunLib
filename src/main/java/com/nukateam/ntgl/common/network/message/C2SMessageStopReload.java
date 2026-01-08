package com.nukateam.ntgl.common.network.message;

import com.nukateam.ntgl.common.data.holders.WeaponMode;
import com.nukateam.ntgl.common.network.IMessage;
import com.nukateam.ntgl.common.network.ServerPlayHandler;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraftforge.network.NetworkEvent;

/**
 * Author: MrCrayfish
 */
public class C2SMessageStopReload implements IMessage<C2SMessageStopReload> {
    private InteractionHand hand = InteractionHand.MAIN_HAND;

    public C2SMessageStopReload() {}

    public C2SMessageStopReload(InteractionHand hand) {
        this.hand = hand;
    }

    @Override
    public void encode(C2SMessageStopReload message, FriendlyByteBuf buffer) {
        buffer.writeEnum(message.hand);
    }

    @Override
    public C2SMessageStopReload decode(FriendlyByteBuf buffer) {
        return new C2SMessageStopReload(
                buffer.readEnum(InteractionHand.class));
    }

    @Override
    public void handle(C2SMessageStopReload message, NetworkEvent.Context supplier) {
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
