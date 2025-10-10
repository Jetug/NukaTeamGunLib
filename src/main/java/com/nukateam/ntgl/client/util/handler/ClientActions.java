package com.nukateam.ntgl.client.util.handler;

import com.nukateam.ntgl.client.handlers.ClientHandler;
import com.nukateam.ntgl.common.data.GunData;
import com.nukateam.ntgl.common.data.holders.AttackMode;
import com.nukateam.ntgl.common.data.holders.WeaponMode;
import com.nukateam.ntgl.common.foundation.item.WeaponItem;
import com.nukateam.ntgl.common.foundation.item.interfaces.IThrowable;
import com.nukateam.ntgl.common.foundation.item.interfaces.IWeapon;
import com.nukateam.ntgl.common.network.HandAction;
import com.nukateam.ntgl.common.network.PacketHandler;
import com.nukateam.ntgl.common.network.message.C2SMessageHandAction;
import com.nukateam.ntgl.common.util.util.GunModifierHelper;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.InteractionHand;

import static com.nukateam.ntgl.common.foundation.init.ModSyncedDataKeys.getReloadKey;

public class ClientActions {
    public static void inspectWeapon(LocalPlayer player) {
        var mainGun = player.getMainHandItem();
        var offGun = player.getOffhandItem();

        if((mainGun.getItem() instanceof WeaponItem || offGun.getItem() instanceof WeaponItem
                || mainGun.getItem() instanceof IThrowable || offGun.getItem() instanceof IThrowable)
                && !ClientHandler.isInspecting()){
            ClientHandler.resetInspectionTimer();
        }
    }

    public static void switchFireMode(InteractionHand hand) {
        PacketHandler.getPlayChannel().sendToServer(new C2SMessageHandAction(hand, HandAction.SWITCH_FIRE_MODE));
    }

    public static void switchAmmo(InteractionHand hand, LocalPlayer player) {
        if (!getReloadKey(hand).getValue(player)) {

            PacketHandler.getPlayChannel().sendToServer(new C2SMessageHandAction(hand, HandAction.SWITCH_AMMO));
        }
    }

    public static void meleeAttack(LocalPlayer player) {
        var weapon = player.getMainHandItem();
        if(weapon.getItem() instanceof IWeapon){
            var gunData = new GunData(weapon, player).setWeaponAction(AttackMode.ATTACK);
            var weaponMode = GunModifierHelper.getWeaponMode(gunData);

            if(weaponMode == WeaponMode.GUN) {
                ClientShootingHandler.get().fire(gunData);
            }
            else if(weaponMode == WeaponMode.MELEE) {
                ClientMeleeHandler.addTracker(gunData, InteractionHand.MAIN_HAND);
            }
            else if(weaponMode == WeaponMode.THROWABLE) {
                ClientThrowableHandler.addTracker(gunData, InteractionHand.MAIN_HAND);
            }
        }
    }
}
