package com.nukateam.ntgl.client.util.handler;

import com.mrcrayfish.controllable.Controllable;
import com.mrcrayfish.controllable.client.Action;
import com.mrcrayfish.controllable.client.binding.ButtonBinding;
import com.mrcrayfish.controllable.client.binding.ButtonBindings;
import com.mrcrayfish.controllable.client.gui.navigation.BasicNavigationPoint;
import com.mrcrayfish.controllable.client.gui.navigation.NavigationPoint;
import com.mrcrayfish.controllable.client.input.Controller;
import com.mrcrayfish.controllable.client.settings.ActionVisibility;
import com.mrcrayfish.controllable.event.ControllerEvents;
import com.mrcrayfish.controllable.event.Value;
import com.nukateam.ntgl.Config;
import com.nukateam.ntgl.Ntgl;
import com.nukateam.ntgl.client.input.GunButtonBindings;
import com.nukateam.ntgl.common.data.WeaponData;
import com.nukateam.ntgl.common.data.attachment.impl.Scope;
import com.nukateam.ntgl.common.data.holders.WeaponMode;
import com.nukateam.ntgl.common.foundation.components.NtglComponents;
import com.nukateam.ntgl.common.foundation.init.ModSyncedDataKeys;

import com.nukateam.ntgl.common.foundation.item.interfaces.IWeapon;
import com.nukateam.ntgl.common.network.PacketHandler;
import com.nukateam.ntgl.common.network.message.weapon.C2SMessageAttachments;
import com.nukateam.ntgl.common.network.message.weapon.C2SMessageUnload;
import com.nukateam.ntgl.common.util.util.WeaponStateHelper;
import com.nukateam.ntgl.common.util.util.WeaponModifierHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;

import java.util.List;
import java.util.Map;

/**
 * Author: MrCrayfish
 */
@EventBusSubscriber(modid = Ntgl.MOD_ID, value = Dist.CLIENT)
public class ControllerHandler {
    private static int reloadCounter = -1;

    public static void init() {
        NeoForge.EVENT_BUS.register(new ControllerHandler());
        ControllerEvents.INPUT.register(ControllerHandler::handleInput);
        ControllerEvents.UPDATE_CAMERA.register(ControllerHandler::handleCamera);
        ControllerEvents.GATHER_ACTIONS.register(ControllerHandler::handleActions);
    }

    private static void handleActions(Map<ButtonBinding, Action> actions, ActionVisibility visibility) {
        var mc = Minecraft.getInstance();
        if (mc.screen != null) return;

        var player = Minecraft.getInstance().player;
        if (player != null) {
            var heldItem = player.getMainHandItem();
            if (heldItem.getItem() instanceof IWeapon) {
                actions.put(GunButtonBindings.AIM, new Action(Component.translatable("ntgl.action.aim"), Action.Side.RIGHT));
                actions.put(GunButtonBindings.SHOOT, new Action(Component.translatable("ntgl.action.shoot"), Action.Side.RIGHT));

                var tag = NtglComponents.getWeaponTag(heldItem);
                var data = new WeaponData(heldItem, player);

                if (tag != null && WeaponStateHelper.getAmmoCount(data) < WeaponModifierHelper.getMaxAmmo(data)) {
                    actions.put(GunButtonBindings.RELOAD, new Action(Component.translatable("ntgl.action.reload"), Action.Side.LEFT));
                }
                var scope = WeaponStateHelper.getScope(data);
                if (scope != null && scope.isStable() && AimingHandler.get().isAiming()) {
                    actions.put(GunButtonBindings.STEADY_AIM, new Action(Component.translatable("ntgl.action.steady_aim"), Action.Side.RIGHT));
                }
            }
        }
    }

    private static boolean handleCamera(Value<Float> yawSpeed, Value<Float> pitchSpeed) {
        var player = Minecraft.getInstance().player;
        if (player != null) {
            var heldItem = player.getMainHandItem();
            if (heldItem.getItem() instanceof IWeapon && AimingHandler.get().isAiming()) {
                double adsSensitivity = Config.CLIENT.controls.aimDownSightSensitivity.get();
                yawSpeed.set(10.0F * (float) adsSensitivity);
                pitchSpeed.set(7.5F * (float) adsSensitivity);
                var data = new WeaponData(heldItem, player);

                var scope = WeaponStateHelper.getScope(data);
                var controller = Controllable.getController();
                if (scope != null && scope.isStable() && controller != null && controller.isButtonPressed(GunButtonBindings.STEADY_AIM.getButton())) {
                    yawSpeed.set(yawSpeed.get() / 2.0F);
                    pitchSpeed.set(pitchSpeed.get() / 2.0F);
                }
            }
        }
        return false;
    }

    private static boolean handleInput(Controller controller, Value<Integer> newButton, int originalButton, boolean state) {
        var player = Minecraft.getInstance().player;
        var world = Minecraft.getInstance().level;
        var shouldCancel = false;
        var shiftDown = ButtonBindings.SNEAK.isButtonDown();
        var hand = shiftDown ? InteractionHand.OFF_HAND : InteractionHand.MAIN_HAND;

        if (player != null && world != null && Minecraft.getInstance().screen == null) {
            var heldItem = player.getMainHandItem();

            if (heldItem.getItem() instanceof IWeapon) {
                if (isEquals(originalButton, GunButtonBindings.SHOOT)) {
                    shouldCancel = true;
                    if (state) {
                        ClientShootingHandler.get().fire(new WeaponData(heldItem, player).setWeaponMode(WeaponMode.PRIMARY));
                    }
                } else if (isEquals(originalButton, GunButtonBindings.AIM)) {
                    shouldCancel = true;
                } else if (isEquals(originalButton, GunButtonBindings.STEADY_AIM)) {
                    shouldCancel = true;
                } else if (isEquals(originalButton, GunButtonBindings.RELOAD)) {
                    shouldCancel = true;
                    if (state) {
                        ControllerHandler.reloadCounter = 0;
                    }
                } else if (isEquals(originalButton, GunButtonBindings.OPEN_ATTACHMENTS)) {
                    shouldCancel = true;
                    if (state) {
                        PacketHandler.getPlayChannel().sendToServer(new C2SMessageAttachments());
                    }
                } else if (isEquals(originalButton, GunButtonBindings.INSPECT)) {
                    shouldCancel = true;
                    ClientActions.inspectWeapon(player);
                } else if (isEquals(originalButton, GunButtonBindings.SELECT_FIRE)) {
                    shouldCancel = true;
                    ClientActions.switchFireMode(hand);
                } else if (isEquals(originalButton, GunButtonBindings.SELECT_AMMO)) {
                    shouldCancel = true;
                    ClientActions.switchFireMode(hand);
                }
            }
        }
        return shouldCancel;
    }

    @SubscribeEvent
    public static void onRenderTick(ClientTickEvent.Pre event) {
        var controller = Controllable.getController();
        var mc = Minecraft.getInstance();
        var player = mc.player;

        if (controller == null) return;
        if (player == null) return;

        if (controller.isButtonPressed(GunButtonBindings.SHOOT.getButton()) && Minecraft.getInstance().screen == null) {
            var heldItem = player.getMainHandItem();
            var gunData = new WeaponData(heldItem, player);

            if (heldItem.getItem() instanceof IWeapon) {
                if (WeaponModifierHelper.isAuto(gunData)) {
                    ClientShootingHandler.get().fire(new WeaponData(heldItem, player).setWeaponMode(WeaponMode.PRIMARY));
                }
            }
        }

        if (mc.screen == null && reloadCounter != -1) {
            if (controller.isButtonPressed(GunButtonBindings.RELOAD.getButton())) {
                reloadCounter++;
            }
        }

        if (reloadCounter > 40) {
            ClientReloadHandler.get().setReloading(false, InteractionHand.MAIN_HAND);
            PacketHandler.getPlayChannel().sendToServer(new C2SMessageUnload());
            reloadCounter = -1;
        } else if (reloadCounter > 0 && !controller.isButtonPressed(GunButtonBindings.RELOAD.getButton())) {
            ClientReloadHandler.get().setReloading(!ModSyncedDataKeys.RELOADING_RIGHT.getValue(player), InteractionHand.MAIN_HAND);
            reloadCounter = -1;
        }
    }

    private static boolean isEquals(int originalButton, ButtonBinding aim) {
        return originalButton == aim.getButton();
    }

    public static boolean isAiming() {
        Controller controller = Controllable.getController();
        return controller != null && controller.isButtonPressed(GunButtonBindings.AIM.getButton());
    }

    public static boolean isShooting() {
        Controller controller = Controllable.getController();
        return controller != null && controller.isButtonPressed(GunButtonBindings.SHOOT.getButton());
    }
}