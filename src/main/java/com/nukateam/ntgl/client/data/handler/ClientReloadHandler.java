package com.nukateam.ntgl.client.data.handler;

import com.nukateam.ntgl.Ntgl;
import com.nukateam.ntgl.client.ClientHandler;
import com.nukateam.ntgl.client.input.KeyBinds;
import com.nukateam.ntgl.common.base.config.gun.Gun;
import com.nukateam.ntgl.common.base.holders.LoadingType;
import com.nukateam.ntgl.common.data.constants.Tags;
import com.nukateam.ntgl.common.data.util.GunEnchantmentHelper;
import com.nukateam.ntgl.common.data.util.GunModifierHelper;
import com.nukateam.ntgl.common.event.*;
import com.nukateam.ntgl.common.foundation.init.ModSyncedDataKeys;
import com.nukateam.ntgl.common.foundation.item.GunItem;
import com.nukateam.ntgl.common.helpers.PlayerAnimationHelper;
import com.nukateam.ntgl.common.network.PacketHandler;
import com.nukateam.ntgl.common.network.message.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.nbt.Tag;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import org.lwjgl.glfw.GLFW;

import static com.nukateam.ntgl.client.data.handler.ShootingHandler.isInGame;

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
                    this.setReloading(false, InteractionHand.MAIN_HAND);
                }
            }

            this.updateReloadTimer(player);
        }
    }

    @SubscribeEvent
    public void onKeyPressed(InputEvent.Key event) {
        var minecraft = Minecraft.getInstance();
        var player = minecraft.player;

        if (player == null || !isInGame())
            return;

        if(event.getAction() == GLFW.GLFW_PRESS) {
            if (KeyBinds.KEY_RELOAD.isDown()) {
//                this.setReloading(!ModSyncedDataKeys.RELOADING_RIGHT.getValue(player), ModSyncedDataKeys.RELOADING_RIGHT);
                startReloading();
            }
            if (KeyBinds.KEY_UNLOAD.consumeClick()) {
                unloadAmmo(InteractionHand.MAIN_HAND);
                unloadAmmo(InteractionHand.OFF_HAND);
            }
            if (KeyBinds.KEY_INSPECT.consumeClick()){
                var mainGun = player.getMainHandItem();
                var offGun = player.getOffhandItem();

                if(mainGun.getItem() instanceof GunItem gunItem &&
                        ClientHandler.getInspectionTicks() == 0){
                    ClientHandler.resetInspectionTimer();
                }
            }
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

        if (mainHandItem.getItem() instanceof GunItem
                && !GunModifierHelper.isWeaponFull(mainHandItem)){
            setReloading(!ModSyncedDataKeys.RELOADING_RIGHT.getValue(player), InteractionHand.MAIN_HAND);
        }
        else if (offhandItem.getItem() instanceof GunItem
                && GunModifierHelper.canRenderInOffhand(player)
                && !GunModifierHelper.isWeaponFull(offhandItem)){
            setReloading(!ModSyncedDataKeys.RELOADING_LEFT.getValue(player), InteractionHand.OFF_HAND);
        }
    }

    public void setReloading(boolean reloading, InteractionHand arm) {
        var player = Minecraft.getInstance().player;
        if (player == null) return;

        var dataKey = arm == InteractionHand.MAIN_HAND ?
                ModSyncedDataKeys.RELOADING_RIGHT:
                ModSyncedDataKeys.RELOADING_LEFT;

        var stack = arm == InteractionHand.MAIN_HAND ?
                player.getMainHandItem():
                player.getOffhandItem();

        if (reloading) {
            if (stack.getItem() instanceof GunItem) {
                var tag = stack.getTag();

                if (tag != null && !tag.contains("IgnoreAmmo", Tag.TAG_BYTE)) {
                    var gun = ((GunItem) stack.getItem()).getModifiedGun(stack);
                    reloadTicks = GunModifierHelper.getReloadTime(stack);

                    if (tag.getInt(Tags.AMMO_COUNT) >= GunEnchantmentHelper.getAmmoCapacity(stack))
                        return;
                    if (MinecraftForge.EVENT_BUS.post(new GunReloadEvent.Pre(player, stack)))
                        return;

                    //JET
                    playAnimation(player, stack, gun, arm);

                    dataKey.setValue(player, true);
                    PacketHandler.getPlayChannel().sendToServer(new C2SMessageReload(true, arm));
                    this.reloadingSlot = player.getInventory().selected;
                    reloadTimer = GunModifierHelper.getReloadTime(stack);

                    MinecraftForge.EVENT_BUS.post(new GunReloadEvent.Post(player, stack));
                }
            }
        } else {
            stopReloading(arm);
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

    private static void playAnimation(LocalPlayer player, ItemStack stack, Gun gun, InteractionHand arm) {
        var reloadDuration = 0;
        var general = gun.getGeneral();
        var reloadTime = GunModifierHelper.getReloadTime(stack);

        if(general.getLoadingType().equals(LoadingType.PER_CARTRIDGE)){
//            var ammoCount = general.getMaxAmmo(stack) - Gun.getAmmo(stack);
            var ammoCount =  GunModifierHelper.getMaxAmmo(stack) - Gun.getAmmo(stack);

            for (var i = 0; i < ammoCount; i++)
                reloadDuration += reloadTime;
        }
        else reloadDuration = reloadTime;

        if (Ntgl.playerAnimatorLoaded)
            PlayerAnimationHelper.playAnim(player, gun.getGeneral().getReloadType(), reloadDuration, arm == InteractionHand.OFF_HAND);
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
