package com.nukateam.ntgl.client.handlers;

import com.nukateam.ntgl.Ntgl;
import com.nukateam.ntgl.client.input.*;
import com.nukateam.ntgl.client.settings.NtglOptions;
import com.nukateam.ntgl.client.util.ClientDebug;
import com.nukateam.ntgl.client.util.TuningMode;
import com.nukateam.ntgl.client.util.handler.*;
import com.nukateam.ntgl.common.data.WeaponData;
import com.nukateam.ntgl.common.data.holders.*;
import com.nukateam.ntgl.common.foundation.item.interfaces.IWeapon;
import com.nukateam.ntgl.common.network.PacketHandler;
import com.nukateam.ntgl.common.network.message.weapon.C2SMessageAttachments;
import com.nukateam.ntgl.common.registry.AmmoHolders;
import com.nukateam.ntgl.common.util.helpers.context.AmmoContext;
import com.nukateam.ntgl.common.util.util.*;
import com.nukateam.ntgl.modules.wheel.ActionWheel;
import com.nukateam.ntgl.modules.wheel.ActionWheelManager;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.ArrayList;

import static com.nukateam.ntgl.client.util.handler.ClientShootingHandler.isInGame;
import static com.nukateam.ntgl.common.util.util.WeaponModifierHelper.canUseOffhandWeapon;

@Mod.EventBusSubscriber(value = Dist.CLIENT)
public class InputHandler {
    static {
        KeyPressHandler.addCommand(new KeyCommand(NtglKeyBinds.KEY_RELOAD, () -> reloadUnload(true), InputHandler::closeWheel));
        KeyPressHandler.addCommand(new KeyCommand(NtglKeyBinds.KEY_UNLOAD, () -> reloadUnload(false), InputHandler::closeWheel));
        KeyPressHandler.addCommand(new KeyCommand(NtglKeyBinds.KEY_AMMO_SELECT, InputHandler::selectAmmoKeyPressed, InputHandler::closeWheel));
    }

    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase == TickEvent.Phase.END && isInGame()) {
            handleKeys();
            handleDebugKeys();
            handleWeaponKeys();
        }
    }

    private static boolean isKeyAttackDown() {
        return Minecraft.getInstance().options.keyAttack.isDown();
    }

    private static boolean isUseKeyDown() {
        return Minecraft.getInstance().options.keyUse.isDown();
    }

    private static void handleKeys() {
        var minecraft = Minecraft.getInstance();
        var player = minecraft.player;
        var shiftDown = minecraft.options.keyShift.isDown();
        var hand = shiftDown ? InteractionHand.OFF_HAND : InteractionHand.MAIN_HAND;

        if (player == null || !isInGame())
            return;

        var heldItem = player.getItemInHand(hand);

        if(heldItem.getItem() instanceof IWeapon) {
            if (NtglKeyBinds.KEY_ATTACHMENTS.consumeClick()) {
                PacketHandler.getPlayChannel().sendToServer(new C2SMessageAttachments(hand));
            }
            if (NtglKeyBinds.KEY_DEBUG_SHOW.consumeClick()) {
                ClientReloadHandler.get().startReloading(WeaponMode.ALTERNATIVE);
            }
            if (NtglKeyBinds.KEY_INSPECT.consumeClick()) {
                ClientActions.inspectWeapon(player);
            }
            if (NtglKeyBinds.KEY_FIRE_SELECT.consumeClick()) {
                ClientActions.switchFireMode(hand);
            }
            if(NtglKeyBinds.KEY_TIPS.consumeClick()){
                var options = NtglOptions.getInstance();
                options.setShowTips(!options.isShowTips());
                options.saveOptions();
            }
        }
    }

    private static void handleWeaponKeys() {
        var player = Minecraft.getInstance().player;
        var options = Minecraft.getInstance().options;
        assert player != null;

        var mainHandItem = player.getMainHandItem();
        var offhandItem = player.getOffhandItem();

        if (WeaponModifierHelper.isWeaponItem(mainHandItem)) {
            var data = new WeaponData(mainHandItem, player);

            if(isKeyAttackDown()) {
                data.setWeaponMode(WeaponMode.PRIMARY);
                handleInput(data, InteractionHand.MAIN_HAND, options.keyAttack);
            }
            else if(isUseKeyDown() && !(WeaponModifierHelper.isWeaponItem(offhandItem) && canUseOffhandWeapon(player))) {
                data.setWeaponMode(WeaponMode.SECONDARY);
                handleInput(data, InteractionHand.MAIN_HAND, options.keyUse);
            }
            else if(NtglKeyBinds.KEY_ADD_ATTACK.isDown()) {
                data.setWeaponMode(WeaponMode.ADDITIONAL);
                handleInput(data, InteractionHand.MAIN_HAND, NtglKeyBinds.KEY_ADD_ATTACK);
            }
            else if(NtglKeyBinds.KEY_ALT_ATTACK.isDown()) {
                data.setWeaponMode(WeaponMode.ALTERNATIVE);
                handleInput(data, InteractionHand.MAIN_HAND, NtglKeyBinds.KEY_ALT_ATTACK);
            }
        }

        if (WeaponModifierHelper.isWeaponItem(offhandItem) && canUseOffhandWeapon(player)) {
            var data = new WeaponData(offhandItem, player);

            if(isUseKeyDown()) {
                data.setWeaponMode(WeaponMode.PRIMARY);
                handleInput(data, InteractionHand.OFF_HAND, options.keyUse);
            }
            else if(!WeaponModifierHelper.isWeaponItem(mainHandItem)){
                if (NtglKeyBinds.KEY_ADD_ATTACK.isDown()) {
                    data.setWeaponMode(WeaponMode.ADDITIONAL);
                    handleInput(data, InteractionHand.OFF_HAND, NtglKeyBinds.KEY_ADD_ATTACK);
                } else if (NtglKeyBinds.KEY_ALT_ATTACK.isDown()) {
                    data.setWeaponMode(WeaponMode.ALTERNATIVE);
                    Ntgl.LOGGER.debug("!!! Alt down");
                    handleInput(data, InteractionHand.OFF_HAND, NtglKeyBinds.KEY_ALT_ATTACK);
                }
            }
        }
    }

    private static void handleInput(WeaponData gunData, InteractionHand hand, KeyMapping key) {
        assert Minecraft.getInstance().player != null;
        var player = Minecraft.getInstance().player;
        var weapon = player.getItemInHand(hand);

        if(weapon.getItem() instanceof IWeapon){
            var weaponMode = WeaponModifierHelper.getWeaponAction(gunData);

            if(weaponMode == WeaponAction.SHOT) {
                ClientShootingHandler.get().handleInput(gunData, hand, key);
            }
            else if(weaponMode == WeaponAction.MELEE) {
                ClientMeleeHandler.handleInput(gunData, hand, key);
            }
            if(weaponMode == WeaponAction.THROW) {
                ClientThrowHandler.handleInput(gunData, hand, key);
            }
        }
    }

    private static void handleDebugKeys() {
        if (!Ntgl.isDebugging()) return;

        if (NtglKeyBinds.KEY_DEBUG_TUNING_MODE.consumeClick()) {

            var id = (ClientDebug.tuningMode.ordinal() + 1) % TuningMode.values().length;
            ClientDebug.tuningMode = TuningMode.values()[id];

            var player = Minecraft.getInstance().player;
            if(player != null) {
                String mode = ClientDebug.tuningMode.getName();
                player.displayClientMessage(Component.literal("Tuning Mode: " + mode), true);
            }
            return;
        }
        if (NtglKeyBinds.KEY_DEBUG_X_ADD.consumeClick()) {
            ClientDebug.addX(1);
            return;
        }
        if (NtglKeyBinds.KEY_DEBUG_Y_ADD.consumeClick()) {
            ClientDebug.addY(1);
            return;
        }
        if (NtglKeyBinds.KEY_DEBUG_Z_ADD.consumeClick()) {
            ClientDebug.addZ(1);
            return;
        }
        if (NtglKeyBinds.KEY_DEBUG_X_SUB.consumeClick()) {
            ClientDebug.addX(-1);
            return;
        }
        if (NtglKeyBinds.KEY_DEBUG_Y_SUB.consumeClick()) {
            ClientDebug.addY(-1);
            return;
        }
        if (NtglKeyBinds.KEY_DEBUG_Z_SUB.consumeClick()) {
            ClientDebug.addZ(-1);
            return;
        }
        if (NtglKeyBinds.KEY_DEBUG_RX_ADD.consumeClick()) {
            ClientDebug.addRX(1);
            return;
        }
        if (NtglKeyBinds.KEY_DEBUG_RY_ADD.consumeClick()) {
            ClientDebug.addRY(1);
            return;
        }
        if (NtglKeyBinds.KEY_DEBUG_RZ_ADD.consumeClick()) {
            ClientDebug.addRZ(1);
            return;
        }
        if (NtglKeyBinds.KEY_DEBUG_RX_SUB.consumeClick()) {
            ClientDebug.addRX(-1);
            return;
        }
        if (NtglKeyBinds.KEY_DEBUG_RY_SUB.consumeClick()) {
            ClientDebug.addRY(-1);
            return;
        }
        if (NtglKeyBinds.KEY_DEBUG_RZ_SUB.consumeClick()) {
            ClientDebug.addRZ(-1);
            return;
        }
        if (NtglKeyBinds.KEY_DEBUG_SHOW.consumeClick()) {
            ClientDebug.isHidden = !ClientDebug.isHidden;
            return;
        }

        if (NtglKeyBinds.KEY_DEBUG_ZERO.consumeClick()) {
            ClientDebug.resetCurrent();
        }
    }

    private static void reloadUnload(boolean reload) {
        var minecraft = Minecraft.getInstance();
        var player = minecraft.player;
        var shiftDown = minecraft.options.keyShift.isDown();
        var hand = shiftDown ? InteractionHand.OFF_HAND : InteractionHand.MAIN_HAND;
        assert player != null;
        var heldItem = player.getItemInHand(hand);
        var modes = new ArrayList<>(WeaponModifierHelper.getWeaponModes(new WeaponData(heldItem, player)).keySet());

        modes = new ArrayList<>(modes.stream().filter((mode) -> {
            var weaponData = new WeaponData(heldItem, player).setWeaponMode(mode);
            var maxAmmo = WeaponModifierHelper.getMaxAmmo(weaponData);
            return maxAmmo > 0;
        }).toList());

        modes.add(WeaponMode.PRIMARY);

        var actions = new ArrayList<ActionWheel.WheelAction>();
        for (var mode : modes) {
            var data = new WeaponData(heldItem, player).setWeaponMode(mode);
            var maxAmmo = WeaponModifierHelper.getMaxAmmo(data);

            if (maxAmmo < 1) continue;

            var meta = WeaponModifierHelper.getWeaponModeMeta(data);

            Runnable action = reload ?
                    () -> ClientReloadHandler.get().startReloading(mode) :
                    () -> ClientReloadHandler.get().unloadAmmo(hand, mode);

            actions.add(new ActionWheel.WheelAction()
                    .setIcon(meta.getIcon())
                    .setTitle(meta.getTitle())
                    .setAction(action)
                    .setColor(mode.getColor())
            );
        }

        Runnable defaultAction = reload ?
                () -> ClientReloadHandler.get().startReloading(WeaponMode.PRIMARY) :
                () -> ClientReloadHandler.get().unloadAmmo(hand, WeaponMode.PRIMARY);

        if (actions.size() > 1) {
            ActionWheelManager.getInstance().showWheel(
                    actions,
                    Component.translatable("title.ntgl.ammo_type"),
                    defaultAction);

        } else defaultAction.run();
    }

    private static void selectAmmoKeyPressed() {
        var minecraft = Minecraft.getInstance();
        var player = minecraft.player;
        var shiftDown = minecraft.options.keyShift.isDown();
        var hand = shiftDown ? InteractionHand.OFF_HAND : InteractionHand.MAIN_HAND;
        var heldItem = player.getItemInHand(hand);

        var modes = new ArrayList<>(WeaponModifierHelper.getWeaponModes(new WeaponData(heldItem, player)).keySet());
        modes.add(WeaponMode.PRIMARY);

        var actions = new ArrayList<ActionWheel.WheelAction>();
        for (var mode : modes) {
            var data = new WeaponData(heldItem, player).setWeaponMode(mode);
            var ammoItems = WeaponModifierHelper.getAmmoItems(data);
            for (var ammo : ammoItems) {
                var currentAmmo = WeaponStateHelper.getCurrentAmmo(data);
                if (ammo == AmmoHolders.EMPTY || ammo == currentAmmo) continue;

                var icon = WeaponModifierHelper.getAmmoConfig(ammo.getId(), data).getAmmoType().getIcon();


                if (player.isCreative() || InventoryUtil.findPlayerAmmo(player, ammo) != AmmoContext.NONE) {
                    actions.add(new ActionWheel.WheelAction()
                            .setIcon(icon)
                            .setTitle(Component.translatable(ammo.getDescriptionId()))
                            .setAction(() -> ClientActions.switchAmmo(hand, data, ammo.getId()))
                            .setColor(mode.getColor())
                    );
                }
            }
        }

        ActionWheelManager.getInstance().showWheel(
                actions,
                Component.translatable("title.ntgl.ammo_type"),
                () -> {
                });
    }

    private static void closeWheel() {
        ActionWheelManager.getInstance().hideWheel();
    }
}
