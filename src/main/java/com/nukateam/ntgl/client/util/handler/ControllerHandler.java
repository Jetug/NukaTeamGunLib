package com.nukateam.ntgl.client.util.handler;

import com.mrcrayfish.controllable.Controllable;
import com.mrcrayfish.controllable.client.Action;
import com.mrcrayfish.controllable.client.ActionVisibility;
import com.mrcrayfish.controllable.client.binding.ButtonBinding;
import com.mrcrayfish.controllable.client.binding.ButtonBindings;
import com.mrcrayfish.controllable.client.gui.navigation.BasicNavigationPoint;
import com.mrcrayfish.controllable.client.gui.navigation.NavigationPoint;
import com.mrcrayfish.controllable.client.input.Controller;
import com.mrcrayfish.controllable.event.ControllerEvents;
import com.mrcrayfish.controllable.event.Value;
import com.nukateam.ntgl.Config;
import com.nukateam.ntgl.Ntgl;
import com.nukateam.ntgl.client.input.GunButtonBindings;
import com.nukateam.ntgl.client.render.screen.WorkbenchScreen;
import com.nukateam.ntgl.common.data.attachment.impl.Scope;
import com.nukateam.ntgl.common.data.holders.AttackMode;
import com.nukateam.ntgl.common.foundation.init.ModSyncedDataKeys;
import com.nukateam.ntgl.common.foundation.item.WeaponItem;
import com.nukateam.ntgl.common.foundation.item.interfaces.IWeapon;
import com.nukateam.ntgl.common.network.PacketHandler;
import com.nukateam.ntgl.common.network.message.C2SMessageAttachments;
import com.nukateam.ntgl.common.network.message.C2SMessageUnload;
import com.nukateam.ntgl.common.data.GunData;
import com.nukateam.ntgl.common.util.util.GunStateHelper;
import com.nukateam.ntgl.modules.enchantment.GunEnchantmentHelper;
import com.nukateam.ntgl.common.util.util.GunModifierHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.List;
import java.util.Map;

/**
 * Author: MrCrayfish
 */
@Mod.EventBusSubscriber(modid = Ntgl.MOD_ID, value = Dist.CLIENT)
public class ControllerHandler {
    private static int reloadCounter = -1;

    public static void init() {
        MinecraftForge.EVENT_BUS.register(new ControllerHandler());
        ControllerEvents.INPUT.register(ControllerHandler::handleInput);
        ControllerEvents.UPDATE_CAMERA.register(ControllerHandler::handleCamera);
        ControllerEvents.GATHER_ACTIONS.register(ControllerHandler::handleActions);
        ControllerEvents.GATHER_NAVIGATION_POINTS.register(ControllerHandler::handleNavigationPoints);
    }

    private static void handleNavigationPoints(List<NavigationPoint> points) {
        var mc = Minecraft.getInstance();

        if (mc.screen instanceof WorkbenchScreen workbench) {
            int startX = workbench.getGuiLeft();
            int startY = workbench.getGuiTop();

            for (int i = 0; i < workbench.getTabs().size(); i++) {
                int tabX = startX + 28 * i + (28 / 2);
                int tabY = startY - (28 / 2);
                points.add(new BasicNavigationPoint(tabX, tabY));
            }

            for (int i = 0; i < 6; i++) {
                int itemX = startX + 172 + (80 / 2);
                int itemY = startY + i * 19 + 63 + (19 / 2);
                points.add(new BasicNavigationPoint(itemX, itemY));
            }
        }
    }

    private static void handleActions(Map<ButtonBinding, Action> actions, ActionVisibility visibility) {
        var mc = Minecraft.getInstance();
        if (mc.screen != null) return;

        var player = Minecraft.getInstance().player;
        if (player != null) {
            var heldItem = player.getMainHandItem();
            if (heldItem.getItem() instanceof WeaponItem) {
                actions.put(GunButtonBindings.AIM, new Action(Component.translatable("ntgl.action.aim"), Action.Side.RIGHT));
                actions.put(GunButtonBindings.SHOOT, new Action(Component.translatable("ntgl.action.shoot"), Action.Side.RIGHT));

                var tag = heldItem.getTag();
                var data = new GunData(heldItem, player);

                if (tag != null && GunStateHelper.getAmmoCount(data) < GunEnchantmentHelper.getAmmoCapacity(data)) {
                    actions.put(GunButtonBindings.RELOAD, new Action(Component.translatable("ntgl.action.reload"), Action.Side.LEFT));
                }

                Scope scope = GunStateHelper.getScope(heldItem);
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
            if (heldItem.getItem() instanceof WeaponItem && AimingHandler.get().isAiming()) {
                double adsSensitivity = Config.CLIENT.controls.aimDownSightSensitivity.get();
                yawSpeed.set(10.0F * (float) adsSensitivity);
                pitchSpeed.set(7.5F * (float) adsSensitivity);

                var scope = GunStateHelper.getScope(heldItem);
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
                        ClientShootingHandler.get().fire(new GunData(heldItem, player).setWeaponAction(AttackMode.PRIMARY));
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
    public void onRenderTick(TickEvent.RenderTickEvent event) {
        var controller = Controllable.getController();
        var mc = Minecraft.getInstance();
        var player = mc.player;

        if (controller == null) return;
        if (event.phase == TickEvent.Phase.END) return;
        if (player == null) return;

        if (controller.isButtonPressed(GunButtonBindings.SHOOT.getButton()) && Minecraft.getInstance().screen == null) {
            var heldItem = player.getMainHandItem();
            var gunData = new GunData(heldItem, player);

            if (heldItem.getItem() instanceof WeaponItem) {
                if (GunModifierHelper.isAuto(gunData)) {
                    ClientShootingHandler.get().fire(new GunData(heldItem, player).setWeaponAction(AttackMode.PRIMARY));
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