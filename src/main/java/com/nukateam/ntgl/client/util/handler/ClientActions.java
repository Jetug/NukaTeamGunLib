package com.nukateam.ntgl.client.util.handler;

import com.nukateam.ntgl.client.handlers.ClientHandler;
import com.nukateam.ntgl.common.data.WeaponData;
import com.nukateam.ntgl.common.data.holders.WeaponMode;
import com.nukateam.ntgl.common.foundation.item.interfaces.IThrowable;
import com.nukateam.ntgl.common.foundation.item.interfaces.IWeapon;
import com.nukateam.ntgl.common.network.enums.HandAction;
import com.nukateam.ntgl.common.network.PacketHandler;
import com.nukateam.ntgl.common.network.message.C2SMessageChangeAmmo;
import com.nukateam.ntgl.common.network.message.C2SMessageHandAction;
import com.nukateam.ntgl.common.util.util.WeaponModifierHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;

import static com.nukateam.ntgl.common.foundation.init.ModSyncedDataKeys.getReloadKey;

public class ClientActions {
    public static void inspectWeapon(LocalPlayer player) {
        var mainGun = player.getMainHandItem();
        var offGun = player.getOffhandItem();

        if((mainGun.getItem() instanceof IWeapon || offGun.getItem() instanceof IWeapon
                || mainGun.getItem() instanceof IThrowable || offGun.getItem() instanceof IThrowable)
                && !ClientHandler.isInspecting()){
            ClientHandler.resetInspectionTimer();
        }
    }

    public static void switchFireMode(InteractionHand hand) {
        PacketHandler.getPlayChannel().sendToServer(new C2SMessageHandAction(hand, HandAction.SWITCH_FIRE_MODE));
    }

    public static void switchAmmo(InteractionHand hand, WeaponData data, ResourceLocation ammo) {
        if (!getReloadKey(hand).getValue(data.wielder)) {
            PacketHandler.getPlayChannel().sendToServer(new C2SMessageChangeAmmo(hand, ammo, data.weaponMode));
        }
    }

    public static void meleeAttack() {
        doAttack(InteractionHand.MAIN_HAND, WeaponMode.ADDITIONAL);
    }

    public static void alternativeAttack() {
        doAttack(InteractionHand.MAIN_HAND, WeaponMode.ALTERNATIVE);
    }

    public static void doAttack(InteractionHand hand, WeaponMode mode) {
        assert Minecraft.getInstance().player != null;
        var player = Minecraft.getInstance().player;
        var weapon = player.getItemInHand(hand);

        if(weapon.getItem() instanceof IWeapon){
            var gunData = new WeaponData(weapon, player).setWeaponMode(mode);
            var weaponMode = WeaponModifierHelper.getWeaponAction(gunData);

//            if(weaponMode == WeaponMode.GUN) {
//                ClientShootingHandler.get().fire(gunData);
//            }
//            else if(weaponMode == WeaponMode.MELEE) {
//                ClientMeleeHandler.addTracker(gunData, hand);
//            }
//            if(weaponMode == WeaponMode.THROW) {
//                ClientThrowHandler.addTracker(gunData, hand);
//            }
        }
    }
}
