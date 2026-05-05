package com.nukateam.ntgl.common.network.message.weapon;

import com.mrcrayfish.framework.api.network.MessageContext;
import com.nukateam.ntgl.common.foundation.init.ModSyncedDataKeys;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;

/**
 * Author: MrCrayfish
 */
public class C2SMessageShooting  {
    public static final StreamCodec<RegistryFriendlyByteBuf, C2SMessageShooting> CODEC = StreamCodec.of(
            (buffer, message) -> encode(message, buffer),
            buffer -> decode(buffer));
    private boolean shooting;

    public C2SMessageShooting() {}

    public C2SMessageShooting(boolean shooting) {
        this.shooting = shooting;
    }

    public static void encode(C2SMessageShooting message, FriendlyByteBuf buffer) {
        buffer.writeBoolean(message.shooting);
    }

    public static C2SMessageShooting decode(FriendlyByteBuf buffer) {
        return new C2SMessageShooting(buffer.readBoolean());
    }

    public static void handle(C2SMessageShooting message, MessageContext supplier) {
        supplier.execute((() -> {
            var player = supplier.getPlayer().get();
            if (player != null) {
                ModSyncedDataKeys.SHOOTING_RIGHT.setValue(player, message.shooting);
            }
        }));
        supplier.setHandled(true);
    }
}
