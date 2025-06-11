package com.nukateam.ntgl.client.util.handler;

import com.nukateam.ntgl.client.handlers.ClientHandler;
import com.nukateam.ntgl.common.foundation.item.GunItem;
import com.nukateam.ntgl.common.network.HandAction;
import com.nukateam.ntgl.common.network.PacketHandler;
import com.nukateam.ntgl.common.network.message.S2CMessageHandAction;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.InteractionHand;

import static com.nukateam.ntgl.common.foundation.init.ModSyncedDataKeys.getReloadKey;

public class ClientActions {
    public static void inspectWeapon(LocalPlayer player) {
        var mainGun = player.getMainHandItem();
        var offGun = player.getOffhandItem();

        if((mainGun.getItem() instanceof GunItem || offGun.getItem() instanceof GunItem)
                && !ClientHandler.isInspecting()){
            ClientHandler.resetInspectionTimer();
        }
    }

    public static void switchFireMode(InteractionHand hand) {
        PacketHandler.getPlayChannel().sendToServer(new S2CMessageHandAction(hand, HandAction.SWITCH_FIRE_MODE));
    }

    public static void switchAmmo(InteractionHand hand, LocalPlayer player) {
        if (!getReloadKey(hand).getValue(player)) {
            PacketHandler.getPlayChannel().sendToServer(new S2CMessageHandAction(hand, HandAction.SWITCH_AMMO));
        }
    }
}
