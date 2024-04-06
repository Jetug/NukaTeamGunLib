package com.nukateam.ntgl.common.network.message;

import com.mrcrayfish.framework.api.network.MessageContext;
import com.nukateam.ntgl.common.event.*;
import com.nukateam.ntgl.common.foundation.init.ModSyncedDataKeys;
import com.mrcrayfish.framework.api.network.message.PlayMessage;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

/**
 * Author: MrCrayfish
 */
public class C2SMessageReload extends PlayMessage<C2SMessageReload> {
    private boolean reload;
    private boolean isRightHand;

    public C2SMessageReload() {}

    public C2SMessageReload(boolean reload, HumanoidArm arm) {
        this.reload = reload;
        this.isRightHand = arm == HumanoidArm.RIGHT;
    }

    public C2SMessageReload(boolean reload, boolean isRightHand) {
        this.reload = reload;
        this.isRightHand = isRightHand;
    }

    @Override
    public void encode(C2SMessageReload message, FriendlyByteBuf buffer) {
        buffer.writeBoolean(message.reload);
        buffer.writeBoolean(message.isRightHand);
    }

    @Override
    public C2SMessageReload decode(FriendlyByteBuf buffer) {
        return new C2SMessageReload(buffer.readBoolean(), buffer.readBoolean());
    }

    @Override
    public void handle(C2SMessageReload message, MessageContext supplier) {
        supplier.execute((() ->
        {
            ServerPlayer player = supplier.getPlayer();
            if (player != null && !player.isSpectator()) {

                var dataKey = message.isRightHand ?
                        ModSyncedDataKeys.RELOADING_RIGHT:
                        ModSyncedDataKeys.RELOADING_LEFT;

//                var dataKey = ModSyncedDataKeys.RELOADING_RIGHT;

                dataKey.setValue(player, message.reload); // This has to be set in order to verify the packet is sent if the event is cancelled
                if (!message.reload)
                    return;

                var gun = isRightHand ? player.getMainHandItem(): player.getOffhandItem();

                if (MinecraftForge.EVENT_BUS.post(new GunReloadEvent.Pre(player, gun))) {
                    dataKey.setValue(player, false);
                    return;
                }
                MinecraftForge.EVENT_BUS.post(new GunReloadEvent.Post(player, gun));
            }
        }));
        supplier.setHandled(true);
    }
}
