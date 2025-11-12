package com.nukateam.ntgl.common.network.message;

import com.mrcrayfish.framework.api.network.MessageContext;
import com.nukateam.ntgl.common.foundation.init.ModSyncedDataKeys;
import net.minecraft.network.FriendlyByteBuf;

/**
 * Author: MrCrayfish
 */
public class C2SMessageShooting  {
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
