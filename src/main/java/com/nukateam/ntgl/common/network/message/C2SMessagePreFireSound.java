package com.nukateam.ntgl.common.network.message;

import com.mrcrayfish.framework.api.network.MessageContext;
import com.nukateam.ntgl.common.network.ServerPlayHandler;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;

/**
 * Author: MrCrayfish
 */
public class C2SMessagePreFireSound  {

    public C2SMessagePreFireSound() {
    }

    public C2SMessagePreFireSound(Player player) {
    }

    public static void encode(C2SMessagePreFireSound message, FriendlyByteBuf buffer) {

    }

    public static C2SMessagePreFireSound decode(FriendlyByteBuf buffer) {
        return new C2SMessagePreFireSound();
    }

    public static void handle(C2SMessagePreFireSound message, MessageContext context) {
        context.execute(() ->
        {
            ServerPlayer player = context.getPlayer();
            if (player != null) {
                ServerPlayHandler.handlePreFireSound(message, player);
            }
        });
        context.setHandled(true);
    }
}