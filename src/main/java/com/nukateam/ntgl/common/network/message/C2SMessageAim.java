package com.nukateam.ntgl.common.network.message;

import net.minecraftforge.network.NetworkEvent;
import com.nukateam.ntgl.common.foundation.init.ModSyncedDataKeys;
import com.nukateam.ntgl.modules.network.IMessage;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;

public class C2SMessageAim implements IMessage<C2SMessageAim> {
    private boolean aiming;

    public C2SMessageAim() {}

    public C2SMessageAim(boolean aiming) {
        this.aiming = aiming;
    }

    @Override
    public void encode(C2SMessageAim message, FriendlyByteBuf buffer) {
        buffer.writeBoolean(message.aiming);
    }

    @Override
    public C2SMessageAim decode(FriendlyByteBuf buffer) {
        return new C2SMessageAim(buffer.readBoolean());
    }

    @Override
    public void handle(C2SMessageAim message, NetworkEvent.Context supplier) {
        supplier.enqueueWork((() ->
        {
            ServerPlayer player = supplier.getSender();
            if (player != null && !player.isSpectator()) {
                ModSyncedDataKeys.AIMING.setValue(player, message.aiming);
            }
        }));
        supplier.setPacketHandled(true);
    }
}
