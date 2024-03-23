package com.nukateam.gunscore.common.network.message;

import com.mrcrayfish.framework.api.network.PlayMessage;
import com.nukateam.gunscore.common.event.GunReloadEvent;
import com.nukateam.gunscore.common.foundation.init.ModSyncedDataKeys;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

/**
 * Author: MrCrayfish
 */
public class C2SMessageCharge extends PlayMessage<C2SMessageCharge> {
    private boolean charge;
    private boolean isRightHand;

    public C2SMessageCharge() {}

    public C2SMessageCharge(boolean charge, HumanoidArm arm) {
        this.charge = charge;
        this.isRightHand = arm == HumanoidArm.RIGHT;
    }

    public C2SMessageCharge(boolean charge, boolean isRightHand) {
        this.charge = charge;
        this.isRightHand = isRightHand;
    }

    @Override
    public void encode(C2SMessageCharge message, FriendlyByteBuf buffer) {
        buffer.writeBoolean(message.charge);
        buffer.writeBoolean(message.isRightHand);
    }

    @Override
    public C2SMessageCharge decode(FriendlyByteBuf buffer) {
        return new C2SMessageCharge(buffer.readBoolean(), buffer.readBoolean());
    }

    @Override
    public void handle(C2SMessageCharge message, Supplier<NetworkEvent.Context> supplier) {
        supplier.get().enqueueWork(() ->
        {
            ServerPlayer player = supplier.get().getSender();
            if (player != null && !player.isSpectator()) {

                var dataKey = message.isRightHand ?
                        ModSyncedDataKeys.CHARGING_RIGHT:
                        ModSyncedDataKeys.CHARGING_LEFT;
                dataKey.setValue(player, message.charge);

            }
        });
        supplier.get().setPacketHandled(true);
    }
}
