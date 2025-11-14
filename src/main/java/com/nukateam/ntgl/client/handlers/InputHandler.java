package com.nukateam.ntgl.client.handlers;

import com.nukateam.ntgl.Ntgl;
import com.nukateam.ntgl.client.input.NtglKeyBinds;
import com.nukateam.ntgl.client.settings.NtglOptions;
import com.nukateam.ntgl.client.util.ClientDebug;
import com.nukateam.ntgl.client.util.handler.*;
import com.nukateam.ntgl.common.data.WeaponData;
import com.nukateam.ntgl.common.data.holders.WeaponMode;
import com.nukateam.ntgl.common.data.holders.WeaponAction;
import com.nukateam.ntgl.common.foundation.entity.FlyingGib;
import com.nukateam.ntgl.common.foundation.init.ModEntityTypes;
import com.nukateam.ntgl.common.foundation.item.interfaces.IWeapon;
import com.nukateam.ntgl.common.network.PacketHandler;
import com.nukateam.ntgl.common.network.message.weapon.C2SMessageAttachments;
import com.nukateam.ntgl.common.util.util.WeaponModifierHelper;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.world.InteractionHand;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;

import static com.nukateam.ntgl.client.input.NtglKeyBinds.*;
import static com.nukateam.ntgl.client.render.renderers.misc.DeathFxRenderer.addClientEntity;
import static com.nukateam.ntgl.client.util.handler.ClientShootingHandler.isInGame;
import static com.nukateam.ntgl.common.util.util.WeaponModifierHelper.canUseOffhandWeapon;

@EventBusSubscriber(value = Dist.CLIENT)
public class InputHandler {
    @SubscribeEvent
    public static void onClientTick(ClientTickEvent.Post event) {
        if (isInGame()) {
            handleKeys();
            handleDebugKeys();
            handleWeaponKeys();
        }
    }

    public static void handleWeaponKeys() {
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

    public static void handleInput(WeaponData gunData, InteractionHand hand, KeyMapping key) {
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

        var heldItem = player.getItemInHand(hand).getItem();

        if(heldItem instanceof IWeapon) {
            if (NtglKeyBinds.KEY_ATTACHMENTS.consumeClick()) {
                PacketHandler.getPlayChannel().sendToServer(new C2SMessageAttachments());
            }
            if (NtglKeyBinds.KEY_RELOAD.consumeClick()) {
                ClientReloadHandler.get().startReloading();
            }
            if (NtglKeyBinds.KEY_UNLOAD.consumeClick()) {
                ClientReloadHandler.get().unloadAmmo(InteractionHand.MAIN_HAND);
                ClientReloadHandler.get().unloadAmmo(InteractionHand.OFF_HAND);
            }
            if (NtglKeyBinds.KEY_INSPECT.consumeClick()) {
                ClientActions.inspectWeapon(player);
            }
            if (NtglKeyBinds.KEY_FIRE_SELECT.consumeClick()) {
                ClientActions.switchFireMode(hand);
            }
            if (NtglKeyBinds.KEY_AMMO_SELECT.consumeClick()) {
                ClientActions.switchAmmo(hand, player);
            }
            if(NtglKeyBinds.KEY_TIPS.consumeClick()){
                var options = NtglOptions.getInstance();
                options.setShowTips(!options.isShowTips());
                options.saveOptions();
            }
        }
    }

    private static void handleDebugKeys() {
        if (Ntgl.isDebugging()) {
            if (KEY_DEBUG_X_ADD.consumeClick()) {
                ClientDebug.X += 1;
            } else if (KEY_DEBUG_Y_ADD.consumeClick()) {
                ClientDebug.Y += 1;
            } else if (KEY_DEBUG_Z_ADD.consumeClick()) {
                ClientDebug.Z += 1;
            } else if (KEY_DEBUG_X_SUB.consumeClick()) {
                ClientDebug.X -= 1;
            } else if (KEY_DEBUG_Y_SUB.consumeClick()) {
                ClientDebug.Y -= 1;
            } else if (KEY_DEBUG_Z_SUB.consumeClick()) {
                ClientDebug.Z -= 1;
            } else if (KEY_DEBUG_SHOW.consumeClick()) {
                ClientDebug.isHidden = !ClientDebug.isHidden;
            } else if (KEY_DEBUG_ZERO.consumeClick()) {
                var level = Minecraft.getInstance().level;
                var entity = new FlyingGib(ModEntityTypes.FLYING_GIBS.get(), level);

                entity.setPos(Minecraft.getInstance().player.position());
                addClientEntity(entity);

                ClientDebug.X = 0;
                ClientDebug.Y = 0;
                ClientDebug.Z = 0;
            }
        }
    }
}
