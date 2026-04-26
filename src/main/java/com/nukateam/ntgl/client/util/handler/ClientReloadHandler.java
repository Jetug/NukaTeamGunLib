package com.nukateam.ntgl.client.util.handler;

import com.nukateam.ntgl.common.data.WeaponData;
import com.nukateam.ntgl.common.data.holders.WeaponMode;
import com.nukateam.ntgl.common.foundation.item.interfaces.IWeapon;
import com.nukateam.ntgl.common.network.message.weapon.C2SMessageReload;
import com.nukateam.ntgl.common.network.message.weapon.C2SMessageReloadStop;
import com.nukateam.ntgl.common.network.message.weapon.C2SMessageUnload;
import com.nukateam.ntgl.common.util.util.WeaponStateHelper;
import com.nukateam.ntgl.common.util.util.InventoryUtil;
import com.nukateam.ntgl.common.util.util.WeaponModifierHelper;
import com.nukateam.ntgl.common.event.*;
import com.nukateam.ntgl.common.foundation.init.ModSyncedDataKeys;
import com.nukateam.ntgl.common.network.PacketHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.bus.api.SubscribeEvent;

import java.util.HashMap;

/**
 * Author: MrCrayfish
 */
public class ClientReloadHandler {
    private static ClientReloadHandler instance;
    private static final HashMap<InteractionHand, WeaponData> RELOAD_DATA = new HashMap<>();

    private int reloadingSlot;
    private int reloadTicks;

    private ClientReloadHandler() {}

    public static ClientReloadHandler get() {
        if (instance == null) {
            instance = new ClientReloadHandler();
        }
        return instance;
    }

    @SubscribeEvent
    public void onClientTick(ClientTickEvent.Post event) {
        if(reloadTicks > 0) reloadTicks--;

        if(reloadTicks > 0) reloadTicks--;

        var player = Minecraft.getInstance().player;
        if (player != null) {
            if (ModSyncedDataKeys.RELOADING_RIGHT.getValue(player)) {
                if (this.reloadingSlot != player.getInventory().selected) {
                    stopReloading(InteractionHand.MAIN_HAND);
                }
            }
        }
    }

    public void unloadAmmo(InteractionHand hand, WeaponMode weaponMode) {
        stopReloading(hand);
        PacketHandler.getPlayChannel().sendToServer(new C2SMessageUnload(hand, weaponMode));
    }

    public void startReloading(WeaponMode mode){
        var player = Minecraft.getInstance().player;
        if (player == null) return;

        var mainHandItem = player.getMainHandItem();
        var offhandItem = player.getOffhandItem();
        var mainData = new WeaponData(mainHandItem, player).setWeaponMode(mode);
        var offData = new WeaponData(offhandItem, player).setWeaponMode(mode);

        if (mainHandItem.getItem() instanceof IWeapon
                && !WeaponStateHelper.isWeaponFull(mainData)
                && !isReloading(player, InteractionHand.MAIN_HAND)){
            RELOAD_DATA.put(InteractionHand.MAIN_HAND, mainData);
            setReloading(mainData, !ModSyncedDataKeys.RELOADING_RIGHT.getValue(player), InteractionHand.MAIN_HAND);
        }
        else if (offhandItem.getItem() instanceof IWeapon
                && WeaponModifierHelper.canUseOffhandWeapon(player)
                && !WeaponStateHelper.isWeaponFull(offData)
                && !isReloading(player, InteractionHand.OFF_HAND)){
            RELOAD_DATA.put(InteractionHand.OFF_HAND, offData);
            setReloading(offData, !ModSyncedDataKeys.RELOADING_LEFT.getValue(player), InteractionHand.OFF_HAND);
        }
    }

    public static WeaponData getReloadData(InteractionHand hand){
        return RELOAD_DATA.get(hand);
    }

    public void setReloading(WeaponData data, boolean reloading, InteractionHand hand) {
        var player = Minecraft.getInstance().player;
        if (player == null) return;

        var dataKey = ModSyncedDataKeys.getReloadKey(hand);
        var stack = data.weapon;
        if (stack == null) return;

        if (reloading) {
            if (stack.getItem() instanceof IWeapon) {
                var isAmmoIgnored = WeaponStateHelper.isAmmoIgnored(data);
                var hasAmmo = InventoryUtil.hasAmmo(data);
                var isMaxAmmo = WeaponStateHelper.isMaxAmmo(data);

                if (!isAmmoIgnored && hasAmmo && !isMaxAmmo) {
                    reloadTicks = WeaponModifierHelper.getReloadTime(data);

                    if (WeaponStateHelper.getAmmoCount(data) >= WeaponModifierHelper.getMaxAmmo(data))
                        return;
                    if (NeoForge.EVENT_BUS.post(new GunReloadEvent.Pre(data, hand)).isCanceled())
                        return;

                    dataKey.setValue(player, true);
                    PacketHandler.getPlayChannel().sendToServer(new C2SMessageReload(hand, data.weaponMode));
                    this.reloadingSlot = player.getInventory().selected;

                    NeoForge.EVENT_BUS.post(new GunReloadEvent.Post(data, hand));
                }
            }
        } else {
            stopReloading(hand);
        }
    }

    public void stopReloading(InteractionHand arm){
        var player = Minecraft.getInstance().player;

        var dataKey = arm == InteractionHand.MAIN_HAND ?
                ModSyncedDataKeys.RELOADING_RIGHT :
                ModSyncedDataKeys.RELOADING_LEFT;

        dataKey.setValue(player, false);
        PacketHandler.getPlayChannel().sendToServer(new C2SMessageReloadStop(arm));
        this.reloadingSlot = -1;
        reloadTicks = -1;
    }

    public boolean isReloading(LivingEntity entity, InteractionHand arm) {
        return switch (arm) {
            case MAIN_HAND -> isReloadingRight(entity);
            case OFF_HAND -> isReloadingLeft(entity);
        };
    }

    public boolean isReloadingRight(LivingEntity entity) {
        return ModSyncedDataKeys.RELOADING_RIGHT.getValue(entity);
    }

    public boolean isReloadingLeft(LivingEntity entity) {
        return ModSyncedDataKeys.RELOADING_LEFT.getValue(entity);
    }
}
