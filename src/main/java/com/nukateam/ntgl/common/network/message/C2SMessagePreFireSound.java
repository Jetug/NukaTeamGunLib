package com.nukateam.ntgl.common.network.message;

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

    public C2SMessagePreFireSound() {
    }

    public C2SMessagePreFireSound(Player player) {
    }

    @Override
    public void encode(C2SMessagePreFireSound message, FriendlyByteBuf buffer) {

    }

    @Override
    public C2SMessagePreFireSound decode(FriendlyByteBuf buffer) {
        return new C2SMessagePreFireSound();
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
}