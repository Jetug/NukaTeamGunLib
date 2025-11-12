package com.nukateam.ntgl.common.network.message;

import com.mrcrayfish.framework.api.network.MessageContext;
import com.nukateam.ntgl.common.foundation.init.ModSyncedDataKeys;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class C2SMessageAim  {
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
            ServerPlayer player = supplier.getPlayer().get();
            if (player != null && !player.isSpectator()) {
                ModSyncedDataKeys.AIMING.setValue(player, message.aiming);
            }
        }));
        supplier.setHandled(true);
    }
}
