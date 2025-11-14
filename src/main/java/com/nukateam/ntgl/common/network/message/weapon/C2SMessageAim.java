package com.nukateam.ntgl.common.network.message.weapon;

import com.mrcrayfish.framework.api.network.MessageContext;
import com.nukateam.ntgl.common.foundation.init.ModSyncedDataKeys;
import com.nukateam.ntgl.common.network.message.chassis.C2SActionPacket;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.server.level.ServerPlayer;

public class C2SMessageAim  {
    public static final StreamCodec<RegistryFriendlyByteBuf, C2SMessageAim> STREAM_CODEC = StreamCodec.of(
            (buffer, message) -> encode(message, buffer),
            buffer -> decode(buffer));

    private boolean aiming;

    public C2SMessageAim() {
    }

    public C2SMessageAim(boolean aiming) {
        this.aiming = aiming;
    }

    public static void encode(C2SMessageAim message, FriendlyByteBuf buffer) {
        buffer.writeBoolean(message.aiming);
    }

    public static C2SMessageAim decode(FriendlyByteBuf buffer) {
        return new C2SMessageAim(buffer.readBoolean());
    }

    public static void handle(C2SMessageAim message, MessageContext supplier) {
        supplier.execute((() ->
        {
            var player = supplier.getPlayer().get();
            if (player != null && !player.isSpectator()) {
                ModSyncedDataKeys.AIMING.setValue(player, message.aiming);
            }
        }));
        supplier.setHandled(true);
    }
}
