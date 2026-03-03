package com.nukateam.ntgl.common.network.message.weapon;

import com.mrcrayfish.framework.api.network.MessageContext;
import com.nukateam.ntgl.common.network.ServerPlayHandler;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;

/**
 * Author: MrCrayfish
 */
public class C2SMessagePreFireSound  {
    public static final StreamCodec<RegistryFriendlyByteBuf, C2SMessagePreFireSound> STREAM_CODEC = StreamCodec.of(
            (buffer, message) -> encode(message, buffer),
            buffer -> decode(buffer));

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
             context.getPlayer().ifPresent(player -> {
                 ServerPlayHandler.handlePreFireSound(message, (ServerPlayer)player);
            });
        });
        context.setHandled(true);
    }
}