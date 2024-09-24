package com.nukateam.ntgl.client.data.handler;

import com.mrcrayfish.controllable.client.input.Controller;
import com.mrcrayfish.controllable.event.ControllerEvents;
import com.nukateam.ntgl.Config;
import com.nukateam.ntgl.client.input.GunButtonBindings;
import com.nukateam.ntgl.client.screen.WorkbenchScreen;
import com.nukateam.ntgl.common.base.gun.Ammo;
import com.nukateam.ntgl.common.base.gun.Gun;
import com.nukateam.ntgl.common.data.util.GunEnchantmentHelper;
import com.nukateam.ntgl.common.data.util.GunModifierHelper;
import com.nukateam.ntgl.common.foundation.init.ModSyncedDataKeys;
import com.nukateam.ntgl.common.foundation.item.GunItem;
import com.nukateam.ntgl.common.foundation.item.attachment.impl.Scope;
import com.nukateam.ntgl.common.network.PacketHandler;
import com.nukateam.ntgl.common.network.message.C2SMessageAttachments;
import com.nukateam.ntgl.common.network.message.C2SMessageUnload;
import com.mrcrayfish.controllable.Controllable;
import com.mrcrayfish.controllable.client.Action;
import com.mrcrayfish.controllable.client.gui.navigation.BasicNavigationPoint;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

/**
 * Author: MrCrayfish
 */
public class ControllerHandler {
    private static int reloadCounter = -1;

    public static void init() {
        MinecraftForge.EVENT_BUS.register(new ControllerHandler());
        ControllerEvents.INPUT.register((controller, newButton, originalButton, state) -> {
            var player = Minecraft.getInstance().player;
            var world = Minecraft.getInstance().level;
            var shouldCancel = false;

            if (player != null && world != null && Minecraft.getInstance().screen == null) {
                var heldItem = player.getMainHandItem();
                if (originalButton == GunButtonBindings.SHOOT.getButton()) {
                    if (heldItem.getItem() instanceof GunItem) {
                        shouldCancel = true;
                        if (state) {
                            ShootingHandler.get().fire(player, heldItem);
                        }
                    }
                } else if (originalButton == GunButtonBindings.AIM.getButton()) {
                    if (heldItem.getItem() instanceof GunItem) {
                        shouldCancel = true;
                    }
                } else if (originalButton == GunButtonBindings.STEADY_AIM.getButton()) {
                    if (heldItem.getItem() instanceof GunItem) {
                        shouldCancel = true;
                    }
                } else if (originalButton == GunButtonBindings.RELOAD.getButton()) {
                    if (heldItem.getItem() instanceof GunItem) {
                        shouldCancel = true;
                        if (state) {
                            ControllerHandler.reloadCounter = 0;
                        }
                    }
                } else if (originalButton == GunButtonBindings.OPEN_ATTACHMENTS.getButton()) {
                    if (heldItem.getItem() instanceof GunItem && Minecraft.getInstance().screen == null) {
                        shouldCancel = true;
                        if (state) {
                            PacketHandler.getPlayChannel().sendToServer(new C2SMessageAttachments());
                        }
                    }
                }
            }
            return shouldCancel;
        });

        ControllerEvents.UPDATE_CAMERA.register((yawSpeed, pitchSpeed) -> {
            var player = Minecraft.getInstance().player;
            if (player != null) {
                var heldItem = player.getMainHandItem();
                if (heldItem.getItem() instanceof GunItem && AimingHandler.get().isAiming()) {
                    double adsSensitivity = Config.CLIENT.controls.aimDownSightSensitivity.get();
                    yawSpeed.set(10.0F * (float) adsSensitivity);
                    pitchSpeed.set(7.5F * (float) adsSensitivity);

                    var scope = Gun.getScope(heldItem);
                    var controller = Controllable.getController();
                    if (scope != null && scope.isStable() && controller != null && controller.isButtonPressed(GunButtonBindings.STEADY_AIM.getButton())) {
                        yawSpeed.set(yawSpeed.get() / 2.0F);
                        pitchSpeed.set(pitchSpeed.get() / 2.0F);
                    }
                }
            }
            return false;
        });
        ControllerEvents.GATHER_ACTIONS.register((actions, visibility) -> {
            var mc = Minecraft.getInstance();
            if (mc.screen != null) return;

            var player = Minecraft.getInstance().player;
            if (player != null) {
                var heldItem = player.getMainHandItem();
                if (heldItem.getItem() instanceof GunItem gunItem) {
                    actions.put(GunButtonBindings.AIM, new Action(Component.translatable("ntgl.action.aim"), Action.Side.RIGHT));
                    actions.put(GunButtonBindings.SHOOT, new Action(Component.translatable("ntgl.action.shoot"), Action.Side.RIGHT));

                    var modifiedGun = gunItem.getModifiedGun(heldItem);
                    var tag = heldItem.getTag();

                    if (tag != null && Gun.getAmmo(heldItem) < GunEnchantmentHelper.getAmmoCapacity(heldItem)) {
                        actions.put(GunButtonBindings.RELOAD, new Action(Component.translatable("ntgl.action.reload"), Action.Side.LEFT));
                    }

                    Scope scope = Gun.getScope(heldItem);
                    if (scope != null && scope.isStable() && AimingHandler.get().isAiming()) {
                        actions.put(GunButtonBindings.STEADY_AIM, new Action(Component.translatable("ntgl.action.steady_aim"), Action.Side.RIGHT));
                    }
                }
            }
        });
        ControllerEvents.GATHER_NAVIGATION_POINTS.register(points -> {
            Minecraft mc = Minecraft.getInstance();
            if (mc.screen instanceof WorkbenchScreen) {
                WorkbenchScreen workbench = (WorkbenchScreen) mc.screen;
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
        });
    }

    @SubscribeEvent
    public void onRender(TickEvent.RenderTickEvent event) {
        Controller controller = Controllable.getController();
        if (controller == null)
            return;

        if (event.phase == TickEvent.Phase.END)
            return;

        Minecraft mc = Minecraft.getInstance();
        Player player = mc.player;
        if (player == null)
            return;

        if (controller.isButtonPressed(GunButtonBindings.SHOOT.getButton()) && Minecraft.getInstance().screen == null) {
            var heldItem = player.getMainHandItem();
            if (heldItem.getItem() instanceof GunItem) {
                if (GunModifierHelper.isAuto(heldItem)) {
                    ShootingHandler.get().fire(player, heldItem);
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

    public static boolean isAiming() {
        Controller controller = Controllable.getController();
        return controller != null && controller.isButtonPressed(GunButtonBindings.AIM.getButton());
    }

    public static boolean isShooting() {
        Controller controller = Controllable.getController();
        return controller != null && controller.isButtonPressed(GunButtonBindings.SHOOT.getButton());
    }
}