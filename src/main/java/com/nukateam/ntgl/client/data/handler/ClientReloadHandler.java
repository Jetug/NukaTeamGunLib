package com.nukateam.ntgl.client.data.handler;

import com.nukateam.ntgl.client.input.KeyBinds;
import com.nukateam.ntgl.common.base.gun.Gun;
import com.nukateam.ntgl.common.data.constants.Tags;
import com.nukateam.ntgl.common.data.util.GunEnchantmentHelper;
import com.nukateam.ntgl.common.data.util.GunModifierHelper;
import com.nukateam.ntgl.common.event.*;
import com.nukateam.ntgl.common.foundation.init.ModSyncedDataKeys;
import com.nukateam.ntgl.common.foundation.item.GunItem;
import com.nukateam.ntgl.common.network.PacketHandler;
import com.nukateam.ntgl.common.network.message.C2SMessageReload;
import com.nukateam.ntgl.common.network.message.C2SMessageUnload;
import net.minecraft.client.Minecraft;
import net.minecraft.nbt.Tag;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import org.lwjgl.glfw.GLFW;

/**
 * Author: MrCrayfish
 */
public class ClientReloadHandler {
    private static ClientReloadHandler instance;

    private int startReloadTick;
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

        Player player = Minecraft.getInstance().player;
        if (player != null) {
            if (ModSyncedDataKeys.RELOADING_RIGHT.getValue(player)) {
                if (this.reloadingSlot != player.getInventory().selected) {
                    this.setReloading(false, HumanoidArm.RIGHT);
                }
            }

            this.updateReloadTimer(player);
        }
    }

    @SubscribeEvent
    public void onKeyPressed(InputEvent.Key event) {
        Player player = Minecraft.getInstance().player;
        if (player == null)
            return;

        if(event.getAction() == GLFW.GLFW_PRESS) {
            if (KeyBinds.KEY_RELOAD.isDown()) {
//                this.setReloading(!ModSyncedDataKeys.RELOADING_RIGHT.getValue(player), ModSyncedDataKeys.RELOADING_RIGHT);
                startReloading();
            }
            if (KeyBinds.KEY_UNLOAD.consumeClick()) {
                this.setReloading(false, HumanoidArm.RIGHT);
                this.setReloading(false, HumanoidArm.LEFT );
                PacketHandler.getPlayChannel().sendToServer(new C2SMessageUnload());
            }
        }
    }

    public void startReloading(){
        var player = Minecraft.getInstance().player;
        if (player == null) return;

        var mainHandItem = player.getMainHandItem();
        var offhandItem = player.getOffhandItem();

        if (mainHandItem.getItem() instanceof GunItem
                && !GunModifierHelper.isWeaponFull(mainHandItem)){
            setReloading(!ModSyncedDataKeys.RELOADING_RIGHT.getValue(player), HumanoidArm.RIGHT);
        }
        else if (offhandItem.getItem() instanceof GunItem
                && GunModifierHelper.canRenderInOffhand(player)
                && !GunModifierHelper.isWeaponFull(offhandItem)){
            setReloading(!ModSyncedDataKeys.RELOADING_LEFT.getValue(player), HumanoidArm.LEFT);
        }
    }

    public void setReloading(boolean reloading, HumanoidArm arm) {
        var player = Minecraft.getInstance().player;
        if (player == null) return;

        var dataKey = arm == HumanoidArm.RIGHT ?
                ModSyncedDataKeys.RELOADING_RIGHT:
                ModSyncedDataKeys.RELOADING_LEFT;

        var stack = arm == HumanoidArm.RIGHT ?
                player.getMainHandItem():
                player.getOffhandItem();

        if (reloading) {
            if (stack.getItem() instanceof GunItem) {
                var tag = stack.getTag();

                if (tag != null && !tag.contains("IgnoreAmmo", Tag.TAG_BYTE)) {
                    Gun gun = ((GunItem) stack.getItem()).getModifiedGun(stack);
                    reloadTicks = gun.getGeneral().getReloadTime();

                    if (tag.getInt(Tags.AMMO_COUNT) >= GunEnchantmentHelper.getAmmoCapacity(stack, gun))
                        return;
                    if (MinecraftForge.EVENT_BUS.post(new GunReloadEvent.Pre(player, stack)))
                        return;

                    dataKey.setValue(player, true);
                    PacketHandler.getPlayChannel().sendToServer(new C2SMessageReload(true, arm));
                    this.reloadingSlot = player.getInventory().selected;
                    var reloadTime = gun.getGeneral().getReloadTime();
                    reloadTimer = reloadTime;

                    MinecraftForge.EVENT_BUS.post(new GunReloadEvent.Post(player, stack));
                }
            }
        } else {
            stopReloading(arm);
        }
    }

    private void stopReloading(HumanoidArm arm){
        Player player = Minecraft.getInstance().player;
        ModSyncedDataKeys.RELOADING_RIGHT.setValue(player, false);
        PacketHandler.getPlayChannel().sendToServer(new C2SMessageReload(false, arm));
        this.reloadingSlot = -1;
        reloadTicks = -1;
    }

    private void updateReloadTimer(Player player) {

        if(reloadTimer > 0){
            reloadTimer--;
        }

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

    public int getStartReloadTick() {
        return this.startReloadTick;
    }

    public int getReloadTimer() {
        return this.reloadTimer;
    }

    public boolean isReloading(LivingEntity entity, HumanoidArm arm) {
        return switch (arm) {
            case RIGHT -> isReloadingRight(entity);
            case LEFT -> isReloadingLeft(entity);
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
