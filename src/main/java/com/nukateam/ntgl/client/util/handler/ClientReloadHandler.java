package com.nukateam.ntgl.client.util.handler;

import com.nukateam.ntgl.common.data.GunData;
import com.nukateam.ntgl.common.foundation.item.interfaces.IWeapon;
import com.nukateam.ntgl.common.util.util.GunStateHelper;
import com.nukateam.ntgl.common.util.util.InventoryUtil;
import com.nukateam.ntgl.modules.enchantment.GunEnchantmentHelper;
import com.nukateam.ntgl.common.util.util.GunModifierHelper;
import com.nukateam.ntgl.common.event.*;
import com.nukateam.ntgl.common.foundation.init.ModSyncedDataKeys;
import com.nukateam.ntgl.common.network.PacketHandler;
import com.nukateam.ntgl.common.network.message.*;
import net.minecraft.client.Minecraft;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

/**
 * Author: MrCrayfish
 */
public class ClientReloadHandler {
    private static ClientReloadHandler instance;

    private int reloadTimer;
    private int prevReloadTimer;
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
    public void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END)
            return;

        if(reloadTicks > 0) reloadTicks--;

        this.prevReloadTimer = this.reloadTimer;

        var player = Minecraft.getInstance().player;
        if (player != null) {
            if (ModSyncedDataKeys.RELOADING_RIGHT.getValue(player)) {
                if (this.reloadingSlot != player.getInventory().selected) {
                    this.setReloading(false, InteractionHand.MAIN_HAND);
                }
            }

            this.updateReloadTimer(player);
        }
    }

    public void unloadAmmo(InteractionHand hand) {
        this.setReloading(false, hand);
        PacketHandler.getPlayChannel().sendToServer(new C2SMessageUnload(hand));
    }

    public void startReloading(){
        var player = Minecraft.getInstance().player;
        if (player == null) return;

        var mainHandItem = player.getMainHandItem();
        var offhandItem = player.getOffhandItem();

        if (mainHandItem.getItem() instanceof IWeapon
                && !GunModifierHelper.isWeaponFull(new GunData(mainHandItem, player))
                && !isReloading(player, InteractionHand.MAIN_HAND)){
            setReloading(!ModSyncedDataKeys.RELOADING_RIGHT.getValue(player), InteractionHand.MAIN_HAND);
        }
        else if (offhandItem.getItem() instanceof IWeapon
                && GunModifierHelper.canUseOffhandWeapon(player)
                && !GunModifierHelper.isWeaponFull(new GunData(offhandItem, player))
                && !isReloading(player, InteractionHand.OFF_HAND)){
            setReloading(!ModSyncedDataKeys.RELOADING_LEFT.getValue(player), InteractionHand.OFF_HAND);
        }
    }

    public void setReloading(boolean reloading, InteractionHand hand) {
        var player = Minecraft.getInstance().player;
        if (player == null) return;

        var dataKey = ModSyncedDataKeys.getReloadKey(hand);
        var stack = player.getItemInHand(hand);

        if (reloading) {
            if (stack.getItem() instanceof IWeapon) {
                var isAmmoIgnored = GunStateHelper.isAmmoIgnored(stack);
                var hasAmmo = InventoryUtil.hasAmmo(player, stack);
                var data = new GunData(stack, player);
                var isMaxAmmo = GunStateHelper.isMaxAmmo(data);

                if (!isAmmoIgnored && hasAmmo && !isMaxAmmo) {
                    reloadTicks = GunModifierHelper.getReloadTime(data);

                    if (GunStateHelper.getAmmoCount(data) >= GunEnchantmentHelper.getAmmoCapacity(data))
                        return;
                    if (MinecraftForge.EVENT_BUS.post(new GunReloadEvent.Pre(player, stack, hand)))
                        return;

                    dataKey.setValue(player, true);
                    PacketHandler.getPlayChannel().sendToServer(new C2SMessageReload(true, hand));
                    this.reloadingSlot = player.getInventory().selected;
                    reloadTimer = GunModifierHelper.getReloadTime(data);

                    MinecraftForge.EVENT_BUS.post(new GunReloadEvent.Post(player, stack, hand));
                }
            }
        } else {
            stopReloading(hand);
        }
    }

    private void stopReloading(InteractionHand arm){
        var player = Minecraft.getInstance().player;

        var dataKey = arm == InteractionHand.MAIN_HAND ?
                ModSyncedDataKeys.RELOADING_RIGHT :
                ModSyncedDataKeys.RELOADING_LEFT;

        dataKey.setValue(player, false);
        PacketHandler.getPlayChannel().sendToServer(new C2SMessageReload(false, arm));
        this.reloadingSlot = -1;
        reloadTicks = -1;
    }

    private void updateReloadTimer(Player player) {
        if(reloadTimer > 0){
            reloadTimer--;
        }
//        else PlayerAnimationHelper.stopAnim(player, reloadArm == HumanoidArm.LEFT);

//        if (ModSyncedDataKeys.RELOADING_RIGHT.getValue(player)) {
//            if (this.startReloadTick == -1) {
//                this.startReloadTick = player.tickCount + 5;
//            }
//            if (this.reloadTimer < 5) {
//                this.reloadTimer++;
//            }
//        } else {
//            if (this.startReloadTick != -1) {
//                this.startReloadTick = -1;
//            }
//            if (this.reloadTimer > 0) {
//                this.reloadTimer--;
//            }
//        }
    }

    public int getReloadTimer() {
        return this.reloadTimer;
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

    public int getReloadingTicks() {
        return reloadTicks;
    }

    public float getReloadProgress(float partialTicks) {
        return (this.prevReloadTimer + (this.reloadTimer - this.prevReloadTimer) * partialTicks) / 5F;
    }
}
