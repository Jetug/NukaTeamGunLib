package com.nukateam.ntgl.client.handlers;

import com.nukateam.ntgl.Ntgl;
import com.nukateam.ntgl.client.input.*;
import com.nukateam.ntgl.client.settings.NtglOptions;
import com.nukateam.ntgl.client.util.ClientDebug;
import com.nukateam.ntgl.client.util.PAWeaponOffsets;
import com.nukateam.ntgl.client.util.handler.*;
import com.nukateam.ntgl.common.data.WeaponData;
import com.nukateam.ntgl.common.data.holders.*;
import com.nukateam.ntgl.common.foundation.entity.FlyingGib;
import com.nukateam.ntgl.common.foundation.init.ModEntityTypes;
import com.nukateam.ntgl.common.foundation.item.interfaces.IWeapon;
import com.nukateam.ntgl.common.network.PacketHandler;
import com.nukateam.ntgl.common.network.message.C2SMessageAttachments;
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

import static com.nukateam.ntgl.client.render.renderers.misc.DeathFxRenderer.addClientEntity;
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
            var player = Minecraft.getInstance().player;
            if (player != null && !player.getMainHandItem().isEmpty()) {
                var item = player.getMainHandItem().getItem();
                var id = net.minecraftforge.registries.ForgeRegistries.ITEMS.getKey(item);
                if (ClientDebug.currentlyTuningItem != id) {
                    ClientDebug.currentlyTuningItem = id;
                    var offset = PAWeaponOffsets.get(id);
                    var global = PAWeaponOffsets.getGlobal();
                    if (global != null) {
                        ClientDebug.gX = global.x; ClientDebug.gY = global.y; ClientDebug.gZ = global.z;
                        ClientDebug.gRX = global.rx; ClientDebug.gRY = global.ry; ClientDebug.gRZ = global.rz;
                        ClientDebug.grArmX = global.rArmX; ClientDebug.grArmY = global.rArmY; ClientDebug.grArmZ = global.rArmZ;
                        ClientDebug.grArmRX = global.rArmRX; ClientDebug.grArmRY = global.rArmRY; ClientDebug.grArmRZ = global.rArmRZ;
                        ClientDebug.glArmX = global.lArmX; ClientDebug.glArmY = global.lArmY; ClientDebug.glArmZ = global.lArmZ;
                        ClientDebug.glArmRX = global.lArmRX; ClientDebug.glArmRY = global.lArmRY; ClientDebug.glArmRZ = global.lArmRZ;
                        ClientDebug.hX = global.hX; ClientDebug.hY = global.hY; ClientDebug.hZ = global.hZ;
                        ClientDebug.hRX = global.hRX; ClientDebug.hRY = global.hRY; ClientDebug.hRZ = global.hRZ;
                        ClientDebug.emX = global.emX; ClientDebug.emY = global.emY; ClientDebug.emZ = global.emZ;
                        ClientDebug.hudX = global.hudX; ClientDebug.hudY = global.hudY; ClientDebug.hudZ = global.hudZ;
                        ClientDebug.gpX = global.gpX; ClientDebug.gpY = global.gpY; ClientDebug.gpZ = global.gpZ;
                        ClientDebug.mfX = global.mfX; ClientDebug.mfY = global.mfY; ClientDebug.mfZ = global.mfZ;
                    } else {
                        ClientDebug.gX = 0; ClientDebug.gY = 0; ClientDebug.gZ = 0; ClientDebug.gRX = 0; ClientDebug.gRY = 0; ClientDebug.gRZ = 0;
                        ClientDebug.grArmX = 0; ClientDebug.grArmY = 0; ClientDebug.grArmZ = 0; ClientDebug.grArmRX = 0; ClientDebug.grArmRY = 0; ClientDebug.grArmRZ = 0;
                        ClientDebug.glArmX = 0; ClientDebug.glArmY = 0; ClientDebug.glArmZ = 0; ClientDebug.glArmRX = 0; ClientDebug.glArmRY = 0; ClientDebug.glArmRZ = 0;
                        ClientDebug.hX = 0; ClientDebug.hY = 0; ClientDebug.hZ = 0; ClientDebug.hRX = 0; ClientDebug.hRY = 0; ClientDebug.hRZ = 0;
                        ClientDebug.emX = 0; ClientDebug.emY = 0; ClientDebug.emZ = 0;
                        ClientDebug.hudX = 0; ClientDebug.hudY = 0; ClientDebug.hudZ = 0;
                        ClientDebug.gpX = 0; ClientDebug.gpY = 0; ClientDebug.gpZ = 0;
                        ClientDebug.mfX = 0; ClientDebug.mfY = 0; ClientDebug.mfZ = 0;
                    }
                    if (offset != null) {
                        ClientDebug.X = offset.x; ClientDebug.Y = offset.y; ClientDebug.Z = offset.z;
                        ClientDebug.RX = offset.rx; ClientDebug.RY = offset.ry; ClientDebug.RZ = offset.rz;
                        ClientDebug.rArmX = offset.rArmX; ClientDebug.rArmY = offset.rArmY; ClientDebug.rArmZ = offset.rArmZ;
                        ClientDebug.rArmRX = offset.rArmRX; ClientDebug.rArmRY = offset.rArmRY; ClientDebug.rArmRZ = offset.rArmRZ;
                        ClientDebug.lArmX = offset.lArmX; ClientDebug.lArmY = offset.lArmY; ClientDebug.lArmZ = offset.lArmZ;
                        ClientDebug.lArmRX = offset.lArmRX; ClientDebug.lArmRY = offset.lArmRY; ClientDebug.lArmRZ = offset.lArmRZ;
                    } else {
                        ClientDebug.X = 0; ClientDebug.Y = 0; ClientDebug.Z = 0; ClientDebug.RX = 0; ClientDebug.RY = 0; ClientDebug.RZ = 0;
                        ClientDebug.rArmX = 0; ClientDebug.rArmY = 0; ClientDebug.rArmZ = 0; ClientDebug.rArmRX = 0; ClientDebug.rArmRY = 0; ClientDebug.rArmRZ = 0;
                        ClientDebug.lArmX = 0; ClientDebug.lArmY = 0; ClientDebug.lArmZ = 0; ClientDebug.lArmRX = 0; ClientDebug.lArmRY = 0; ClientDebug.lArmRZ = 0;
                    }
                }
            }

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
            ClientDebug.tuningMode = (ClientDebug.tuningMode + 1) % 11;
            var player = Minecraft.getInstance().player;
            if(player != null) {
                String mode = "";
                switch(ClientDebug.tuningMode) {
                    case 0: mode = "Unique Weapon"; break;
                    case 1: mode = "Unique Right Arm"; break;
                    case 2: mode = "Unique Left Arm"; break;
                    case 3: mode = "Global Weapon"; break;
                    case 4: mode = "Global Right Arm"; break;
                    case 5: mode = "Global Left Arm"; break;
                    case 6: mode = "Passenger Head"; break;
                    case 7: mode = "Emissive Layer"; break;
                    case 8: mode = "Gun HUD"; break;
                    case 9: mode = "Grenade Pose"; break;
                    case 10: mode = "Muzzle Flash"; break;
                }
                player.displayClientMessage(Component.literal("Tuning Mode: " + mode), true);
            }
            return;
        }
        if (NtglKeyBinds.KEY_DEBUG_X_ADD.consumeClick()) {
            if(ClientDebug.tuningMode == 0) ClientDebug.X += 1; else if(ClientDebug.tuningMode == 1) ClientDebug.rArmX += 1; else if(ClientDebug.tuningMode == 2) ClientDebug.lArmX += 1; else if(ClientDebug.tuningMode == 3) ClientDebug.gX += 1; else if(ClientDebug.tuningMode == 4) ClientDebug.grArmX += 1; else if(ClientDebug.tuningMode == 5) ClientDebug.glArmX += 1; else if(ClientDebug.tuningMode == 6) ClientDebug.hX += 1; else if(ClientDebug.tuningMode == 7) ClientDebug.emX += 1; else if(ClientDebug.tuningMode == 8) ClientDebug.hudX += 1; else if(ClientDebug.tuningMode == 9) ClientDebug.gpX += 1; else ClientDebug.mfX += 1;
            syncDebugToOffsets(); return;
        }
        if (NtglKeyBinds.KEY_DEBUG_Y_ADD.consumeClick()) {
            if(ClientDebug.tuningMode == 0) ClientDebug.Y += 1; else if(ClientDebug.tuningMode == 1) ClientDebug.rArmY += 1; else if(ClientDebug.tuningMode == 2) ClientDebug.lArmY += 1; else if(ClientDebug.tuningMode == 3) ClientDebug.gY += 1; else if(ClientDebug.tuningMode == 4) ClientDebug.grArmY += 1; else if(ClientDebug.tuningMode == 5) ClientDebug.glArmY += 1; else if(ClientDebug.tuningMode == 6) ClientDebug.hY += 1; else if(ClientDebug.tuningMode == 7) ClientDebug.emY += 1; else if(ClientDebug.tuningMode == 8) ClientDebug.hudY += 1; else if(ClientDebug.tuningMode == 9) ClientDebug.gpY += 1; else ClientDebug.mfY += 1;
            syncDebugToOffsets(); return;
        }
        if (NtglKeyBinds.KEY_DEBUG_Z_ADD.consumeClick()) {
            if(ClientDebug.tuningMode == 0) ClientDebug.Z += 1; else if(ClientDebug.tuningMode == 1) ClientDebug.rArmZ += 1; else if(ClientDebug.tuningMode == 2) ClientDebug.lArmZ += 1; else if(ClientDebug.tuningMode == 3) ClientDebug.gZ += 1; else if(ClientDebug.tuningMode == 4) ClientDebug.grArmZ += 1; else if(ClientDebug.tuningMode == 5) ClientDebug.glArmZ += 1; else if(ClientDebug.tuningMode == 6) ClientDebug.hZ += 1; else if(ClientDebug.tuningMode == 7) ClientDebug.emZ += 1; else if(ClientDebug.tuningMode == 8) ClientDebug.hudZ += 1; else if(ClientDebug.tuningMode == 9) ClientDebug.gpZ += 1; else ClientDebug.mfZ += 1;
            syncDebugToOffsets(); return;
        }
        if (NtglKeyBinds.KEY_DEBUG_X_SUB.consumeClick()) {
            if(ClientDebug.tuningMode == 0) ClientDebug.X -= 1; else if(ClientDebug.tuningMode == 1) ClientDebug.rArmX -= 1; else if(ClientDebug.tuningMode == 2) ClientDebug.lArmX -= 1; else if(ClientDebug.tuningMode == 3) ClientDebug.gX -= 1; else if(ClientDebug.tuningMode == 4) ClientDebug.grArmX -= 1; else if(ClientDebug.tuningMode == 5) ClientDebug.glArmX -= 1; else if(ClientDebug.tuningMode == 6) ClientDebug.hX -= 1; else if(ClientDebug.tuningMode == 7) ClientDebug.emX -= 1; else if(ClientDebug.tuningMode == 8) ClientDebug.hudX -= 1; else if(ClientDebug.tuningMode == 9) ClientDebug.gpX -= 1; else ClientDebug.mfX -= 1;
            syncDebugToOffsets(); return;
        }
        if (NtglKeyBinds.KEY_DEBUG_Y_SUB.consumeClick()) {
            if(ClientDebug.tuningMode == 0) ClientDebug.Y -= 1; else if(ClientDebug.tuningMode == 1) ClientDebug.rArmY -= 1; else if(ClientDebug.tuningMode == 2) ClientDebug.lArmY -= 1; else if(ClientDebug.tuningMode == 3) ClientDebug.gY -= 1; else if(ClientDebug.tuningMode == 4) ClientDebug.grArmY -= 1; else if(ClientDebug.tuningMode == 5) ClientDebug.glArmY -= 1; else if(ClientDebug.tuningMode == 6) ClientDebug.hY -= 1; else if(ClientDebug.tuningMode == 7) ClientDebug.emY -= 1; else if(ClientDebug.tuningMode == 8) ClientDebug.hudY -= 1; else if(ClientDebug.tuningMode == 9) ClientDebug.gpY -= 1; else ClientDebug.mfY -= 1;
            syncDebugToOffsets(); return;
        }
        if (NtglKeyBinds.KEY_DEBUG_Z_SUB.consumeClick()) {
            if(ClientDebug.tuningMode == 0) ClientDebug.Z -= 1; else if(ClientDebug.tuningMode == 1) ClientDebug.rArmZ -= 1; else if(ClientDebug.tuningMode == 2) ClientDebug.lArmZ -= 1; else if(ClientDebug.tuningMode == 3) ClientDebug.gZ -= 1; else if(ClientDebug.tuningMode == 4) ClientDebug.grArmZ -= 1; else if(ClientDebug.tuningMode == 5) ClientDebug.glArmZ -= 1; else if(ClientDebug.tuningMode == 6) ClientDebug.hZ -= 1; else if(ClientDebug.tuningMode == 7) ClientDebug.emZ -= 1; else if(ClientDebug.tuningMode == 8) ClientDebug.hudZ -= 1; else if(ClientDebug.tuningMode == 9) ClientDebug.gpZ -= 1; else ClientDebug.mfZ -= 1;
            syncDebugToOffsets(); return;
        }
        if (NtglKeyBinds.KEY_DEBUG_RX_ADD.consumeClick()) {
            if(ClientDebug.tuningMode == 0) ClientDebug.RX += 1; else if(ClientDebug.tuningMode == 1) ClientDebug.rArmRX += 1; else if(ClientDebug.tuningMode == 2) ClientDebug.lArmRX += 1; else if(ClientDebug.tuningMode == 3) ClientDebug.gRX += 1; else if(ClientDebug.tuningMode == 4) ClientDebug.grArmRX += 1; else if(ClientDebug.tuningMode == 5) ClientDebug.glArmRX += 1; else if(ClientDebug.tuningMode == 6) ClientDebug.hRX += 1;
            syncDebugToOffsets(); return;
        }
        if (NtglKeyBinds.KEY_DEBUG_RY_ADD.consumeClick()) {
            if(ClientDebug.tuningMode == 0) ClientDebug.RY += 1; else if(ClientDebug.tuningMode == 1) ClientDebug.rArmRY += 1; else if(ClientDebug.tuningMode == 2) ClientDebug.lArmRY += 1; else if(ClientDebug.tuningMode == 3) ClientDebug.gRY += 1; else if(ClientDebug.tuningMode == 4) ClientDebug.grArmRY += 1; else if(ClientDebug.tuningMode == 5) ClientDebug.glArmRY += 1; else if(ClientDebug.tuningMode == 6) ClientDebug.hRY += 1;
            syncDebugToOffsets(); return;
        }
        if (NtglKeyBinds.KEY_DEBUG_RZ_ADD.consumeClick()) {
            if(ClientDebug.tuningMode == 0) ClientDebug.RZ += 1; else if(ClientDebug.tuningMode == 1) ClientDebug.rArmRZ += 1; else if(ClientDebug.tuningMode == 2) ClientDebug.lArmRZ += 1; else if(ClientDebug.tuningMode == 3) ClientDebug.gRZ += 1; else if(ClientDebug.tuningMode == 4) ClientDebug.grArmRZ += 1; else if(ClientDebug.tuningMode == 5) ClientDebug.glArmRZ += 1; else if(ClientDebug.tuningMode == 6) ClientDebug.hRZ += 1;
            syncDebugToOffsets(); return;
        }
        if (NtglKeyBinds.KEY_DEBUG_RX_SUB.consumeClick()) {
            if(ClientDebug.tuningMode == 0) ClientDebug.RX -= 1; else if(ClientDebug.tuningMode == 1) ClientDebug.rArmRX -= 1; else if(ClientDebug.tuningMode == 2) ClientDebug.lArmRX -= 1; else if(ClientDebug.tuningMode == 3) ClientDebug.gRX -= 1; else if(ClientDebug.tuningMode == 4) ClientDebug.grArmRX -= 1; else if(ClientDebug.tuningMode == 5) ClientDebug.glArmRX -= 1; else if(ClientDebug.tuningMode == 6) ClientDebug.hRX -= 1;
            syncDebugToOffsets(); return;
        }
        if (NtglKeyBinds.KEY_DEBUG_RY_SUB.consumeClick()) {
            if(ClientDebug.tuningMode == 0) ClientDebug.RY -= 1; else if(ClientDebug.tuningMode == 1) ClientDebug.rArmRY -= 1; else if(ClientDebug.tuningMode == 2) ClientDebug.lArmRY -= 1; else if(ClientDebug.tuningMode == 3) ClientDebug.gRY -= 1; else if(ClientDebug.tuningMode == 4) ClientDebug.grArmRY -= 1; else if(ClientDebug.tuningMode == 5) ClientDebug.glArmRY -= 1; else if(ClientDebug.tuningMode == 6) ClientDebug.hRY -= 1;
            syncDebugToOffsets(); return;
        }
        if (NtglKeyBinds.KEY_DEBUG_RZ_SUB.consumeClick()) {
            if(ClientDebug.tuningMode == 0) ClientDebug.RZ -= 1; else if(ClientDebug.tuningMode == 1) ClientDebug.rArmRZ -= 1; else if(ClientDebug.tuningMode == 2) ClientDebug.lArmRZ -= 1; else if(ClientDebug.tuningMode == 3) ClientDebug.gRZ -= 1; else if(ClientDebug.tuningMode == 4) ClientDebug.grArmRZ -= 1; else if(ClientDebug.tuningMode == 5) ClientDebug.glArmRZ -= 1; else if(ClientDebug.tuningMode == 6) ClientDebug.hRZ -= 1;
            syncDebugToOffsets(); return;
        }
        if (NtglKeyBinds.KEY_DEBUG_SHOW.consumeClick()) {
            ClientDebug.isHidden = !ClientDebug.isHidden;
            return;
        }

        if (NtglKeyBinds.KEY_DEBUG_SAVE.consumeClick()) {
            var player = Minecraft.getInstance().player;
            if (player != null && !player.getMainHandItem().isEmpty()) {
                var globalId = new net.minecraft.resources.ResourceLocation("nukacraft", "global_offsets");
                var id = net.minecraftforge.registries.ForgeRegistries.ITEMS.getKey(player.getMainHandItem().getItem());
                PAWeaponOffsets.save(globalId, ClientDebug.gX, ClientDebug.gY, ClientDebug.gZ, ClientDebug.gRX, ClientDebug.gRY, ClientDebug.gRZ,
                        ClientDebug.grArmX, ClientDebug.grArmY, ClientDebug.grArmZ, ClientDebug.grArmRX, ClientDebug.grArmRY, ClientDebug.grArmRZ,
                        ClientDebug.glArmX, ClientDebug.glArmY, ClientDebug.glArmZ, ClientDebug.glArmRX, ClientDebug.glArmRY, ClientDebug.glArmRZ,
                        ClientDebug.hX, ClientDebug.hY, ClientDebug.hZ, ClientDebug.hRX, ClientDebug.hRY, ClientDebug.hRZ,
                        ClientDebug.emX, ClientDebug.emY, ClientDebug.emZ,
                        ClientDebug.hudX, ClientDebug.hudY, ClientDebug.hudZ,
                        ClientDebug.gpX, ClientDebug.gpY, ClientDebug.gpZ,
                        ClientDebug.mfX, ClientDebug.mfY, ClientDebug.mfZ);
                PAWeaponOffsets.save(id, ClientDebug.X, ClientDebug.Y, ClientDebug.Z, ClientDebug.RX, ClientDebug.RY, ClientDebug.RZ,
                        ClientDebug.rArmX, ClientDebug.rArmY, ClientDebug.rArmZ, ClientDebug.rArmRX, ClientDebug.rArmRY, ClientDebug.rArmRZ,
                        ClientDebug.lArmX, ClientDebug.lArmY, ClientDebug.lArmZ, ClientDebug.lArmRX, ClientDebug.lArmRY, ClientDebug.lArmRZ,
                        0, 0, 0, 0, 0, 0,
                        0, 0, 0,
                        0, 0, 0,
                        0, 0, 0,
                        0, 0, 0);
                player.displayClientMessage(Component.literal("Saved both Global base offsets and " + id + " unique tweaks!"), false);
            }
            return;
        }
        if (NtglKeyBinds.KEY_DEBUG_ZERO.consumeClick()) {
            if(ClientDebug.tuningMode == 0) { ClientDebug.X=0; ClientDebug.Y=0; ClientDebug.Z=0; ClientDebug.RX=0; ClientDebug.RY=0; ClientDebug.RZ=0; }
            else if(ClientDebug.tuningMode == 1) { ClientDebug.rArmX=0; ClientDebug.rArmY=0; ClientDebug.rArmZ=0; ClientDebug.rArmRX=0; ClientDebug.rArmRY=0; ClientDebug.rArmRZ=0; }
            else if(ClientDebug.tuningMode == 2) { ClientDebug.lArmX=0; ClientDebug.lArmY=0; ClientDebug.lArmZ=0; ClientDebug.lArmRX=0; ClientDebug.lArmRY=0; ClientDebug.lArmRZ=0; }
            else if(ClientDebug.tuningMode == 3) { ClientDebug.gX=0; ClientDebug.gY=0; ClientDebug.gZ=0; ClientDebug.gRX=0; ClientDebug.gRY=0; ClientDebug.gRZ=0; }
            else if(ClientDebug.tuningMode == 4) { ClientDebug.grArmX=0; ClientDebug.grArmY=0; ClientDebug.grArmZ=0; ClientDebug.grArmRX=0; ClientDebug.grArmRY=0; ClientDebug.grArmRZ=0; }
            else if(ClientDebug.tuningMode == 5) { ClientDebug.glArmX=0; ClientDebug.glArmY=0; ClientDebug.glArmZ=0; ClientDebug.glArmRX=0; ClientDebug.glArmRY=0; ClientDebug.glArmRZ=0; }
            else if(ClientDebug.tuningMode == 6) { ClientDebug.hX=0; ClientDebug.hY=0; ClientDebug.hZ=0; ClientDebug.hRX=0; ClientDebug.hRY=0; ClientDebug.hRZ=0; }
            else if(ClientDebug.tuningMode == 7) { ClientDebug.emX=0; ClientDebug.emY=0; ClientDebug.emZ=0; }
            else if(ClientDebug.tuningMode == 8) { ClientDebug.hudX=0; ClientDebug.hudY=0; ClientDebug.hudZ=0; }
            else if(ClientDebug.tuningMode == 9) { ClientDebug.gpX=0; ClientDebug.gpY=0; ClientDebug.gpZ=0; }
            else { ClientDebug.mfX=0; ClientDebug.mfY=0; ClientDebug.mfZ=0; }
            syncDebugToOffsets(); return;
        }
    }

    private static void syncDebugToOffsets() {
        var player = Minecraft.getInstance().player;
        if (player == null || player.getMainHandItem().isEmpty()) return;
        var globalId = new net.minecraft.resources.ResourceLocation("nukacraft", "global_offsets");
        var id = net.minecraftforge.registries.ForgeRegistries.ITEMS.getKey(player.getMainHandItem().getItem());
        PAWeaponOffsets.updateInMemory(globalId, ClientDebug.gX, ClientDebug.gY, ClientDebug.gZ, ClientDebug.gRX, ClientDebug.gRY, ClientDebug.gRZ,
                ClientDebug.grArmX, ClientDebug.grArmY, ClientDebug.grArmZ, ClientDebug.grArmRX, ClientDebug.grArmRY, ClientDebug.grArmRZ,
                ClientDebug.glArmX, ClientDebug.glArmY, ClientDebug.glArmZ, ClientDebug.glArmRX, ClientDebug.glArmRY, ClientDebug.glArmRZ,
                ClientDebug.hX, ClientDebug.hY, ClientDebug.hZ, ClientDebug.hRX, ClientDebug.hRY, ClientDebug.hRZ,
                ClientDebug.emX, ClientDebug.emY, ClientDebug.emZ,
                ClientDebug.hudX, ClientDebug.hudY, ClientDebug.hudZ,
                ClientDebug.gpX, ClientDebug.gpY, ClientDebug.gpZ,
                ClientDebug.mfX, ClientDebug.mfY, ClientDebug.mfZ);
        PAWeaponOffsets.updateInMemory(id, ClientDebug.X, ClientDebug.Y, ClientDebug.Z, ClientDebug.RX, ClientDebug.RY, ClientDebug.RZ,
                ClientDebug.rArmX, ClientDebug.rArmY, ClientDebug.rArmZ, ClientDebug.rArmRX, ClientDebug.rArmRY, ClientDebug.rArmRZ,
                ClientDebug.lArmX, ClientDebug.lArmY, ClientDebug.lArmZ, ClientDebug.lArmRX, ClientDebug.lArmRY, ClientDebug.lArmRZ,
                0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0);
    }

    private static void reloadUnload(boolean reload) {
        var minecraft = Minecraft.getInstance();
        var player = minecraft.player;
        var shiftDown = minecraft.options.keyShift.isDown();
        var hand = shiftDown ? InteractionHand.OFF_HAND : InteractionHand.MAIN_HAND;
        assert player != null;
        var heldItem = player.getItemInHand(hand);
        var modes = new ArrayList<>(WeaponModifierHelper.getWeaponModes(new WeaponData(heldItem, player)).keySet());
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
