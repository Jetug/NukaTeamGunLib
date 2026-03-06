package com.nukateam.ntgl.common.network.message;

import net.minecraft.world.InteractionHand;
import net.minecraftforge.network.NetworkEvent;
import com.nukateam.ntgl.common.network.IMessage;
import com.nukateam.ntgl.common.network.ServerPlayHandler;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;

/**
 * Author: MrCrayfish
 */
public class C2SMessagePreFireSound implements IMessage<C2SMessagePreFireSound> {
    private InteractionHand hand;

    public C2SMessagePreFireSound() {
    }

    public C2SMessagePreFireSound(InteractionHand hand) {
        this.hand = hand;
    }

    @Override
    public void encode(C2SMessagePreFireSound message, FriendlyByteBuf buffer) {
        buffer.writeEnum(message.hand);
    }

    @Override
    public C2SMessagePreFireSound decode(FriendlyByteBuf buffer) {
        return new C2SMessagePreFireSound(buffer.readEnum(InteractionHand.class));
    }

    @Override
    public void handle(C2SMessagePreFireSound message, NetworkEvent.Context context) {
        context.enqueueWork(() ->
        {
            ServerPlayer player = context.getSender();
            if (player != null) {
                ServerPlayHandler.handlePreFireSound(message, player);
            }
        });
        context.setPacketHandled(true);
    }

    public InteractionHand getHand() {
        return hand;
    }
}