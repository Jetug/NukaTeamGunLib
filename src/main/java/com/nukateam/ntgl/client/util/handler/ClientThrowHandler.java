package com.nukateam.ntgl.client.util.handler;

import com.nukateam.ntgl.Ntgl;
import com.nukateam.ntgl.client.input.NtglKeyBinds;
import com.nukateam.ntgl.common.data.WeaponData;
import com.nukateam.ntgl.common.data.holders.WeaponMode;
import com.nukateam.ntgl.common.data.holders.WeaponAction;
import com.nukateam.ntgl.common.foundation.item.interfaces.IThrowable;
import com.nukateam.ntgl.common.network.enums.KeyAction;
import com.nukateam.ntgl.common.network.PacketHandler;
import com.nukateam.ntgl.common.network.message.C2SMessageGrenade;
import com.nukateam.ntgl.common.util.util.WeaponModifierHelper;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.HashMap;
import java.util.Map;

import static com.nukateam.ntgl.common.util.util.WeaponModifierHelper.canUseOffhandWeapon;

@Mod.EventBusSubscriber(modid = Ntgl.MOD_ID, value = Dist.CLIENT)
public class ClientThrowHandler {
    private static final Map<InteractionHand, Tracker> TRACKER_MAP = new HashMap<>();

    @SubscribeEvent
    public static void onPostClientTick(TickEvent.ClientTickEvent event){
        var minecraft = Minecraft.getInstance();
        if(event.phase == TickEvent.Phase.END && minecraft.player != null){
            if(!minecraft.options.keyAttack.isDown()) {
                removeTracker(InteractionHand.MAIN_HAND, WeaponMode.PRIMARY);
                removeTracker(InteractionHand.OFF_HAND, WeaponMode.PRIMARY);
            }
            if(!minecraft.options.keyUse.isDown()) {
                removeTracker(InteractionHand.MAIN_HAND, WeaponMode.SECONDARY);
                removeTracker(InteractionHand.OFF_HAND , WeaponMode.SECONDARY);
            }
            if(!NtglKeyBinds.KEY_ADD_ATTACK.isDown()) {
                removeTracker(InteractionHand.MAIN_HAND, WeaponMode.ADDITIONAL);
                removeTracker(InteractionHand.OFF_HAND , WeaponMode.ADDITIONAL);
            }
            if(!NtglKeyBinds.KEY_ALT_ATTACK.isDown()) {
                removeTracker(InteractionHand.MAIN_HAND, WeaponMode.ALTERNATIVE);
                removeTracker(InteractionHand.OFF_HAND , WeaponMode.ALTERNATIVE);
            }
        }
    }

    private static boolean isThrowMode(WeaponData weaponData) {
        return WeaponModifierHelper.getWeaponAction(weaponData) == WeaponAction.THROW;
    }

    public static void handleInput(WeaponData weaponData, InteractionHand hand, KeyMapping key) {
        if (!isThrowMode(weaponData)) {
            return;
        }
        addTracker(weaponData, hand);
    }

    public static void addTracker(WeaponData weaponData, InteractionHand hand) {
        if(isThrowable(weaponData) && !TRACKER_MAP.containsKey(hand)){
            TRACKER_MAP.put(hand, new Tracker(weaponData, hand));
            PacketHandler.getPlayChannel().sendToServer(new C2SMessageGrenade(KeyAction.HOLD, hand, weaponData.weaponMode));
        }
    }

    private static void removeTracker(InteractionHand hand, WeaponMode mode) {
        if(TRACKER_MAP.containsKey(hand) && TRACKER_MAP.get(hand).data.weaponMode == mode) {
            var action = TRACKER_MAP.get(hand).data.weaponMode;
            PacketHandler.getPlayChannel().sendToServer(new C2SMessageGrenade(KeyAction.RELEASE, hand, action));
            TRACKER_MAP.remove(hand);
        }
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onMouseClick(InputEvent.InteractionKeyMappingTriggered event) {
        if (event.isCanceled()) return;

        var mc = Minecraft.getInstance();
        var player = mc.player;

        if (player == null) return;

        if (event.isAttack()) {
            var heldItem = player.getMainHandItem();
            var gunData = new WeaponData(heldItem, player).setWeaponMode(WeaponMode.PRIMARY);

            if (isThrowable(gunData)) {
                event.setCanceled(true);
                event.setSwingHand(false);
            }
        } else if (event.isUseItem()) {
            var offhandItem = player.getOffhandItem();
            var gunData = new WeaponData(offhandItem, player).setWeaponMode(WeaponMode.PRIMARY);

            if (isThrowable(gunData) && canUseOffhandWeapon(player)) {
                event.setCanceled(true);
                event.setSwingHand(false);
            }
        }
    }

    private static boolean isThrowable(WeaponData weaponData) {
        return weaponData.weapon != null && weaponData.weapon.getItem() instanceof IThrowable && WeaponModifierHelper.isThrowable(weaponData);
    }

    private static class Tracker {
        private final WeaponData data;
        private final InteractionHand arm;
        private final ItemStack stack;
        private final LivingEntity entity;

        private Tracker(WeaponData data, InteractionHand arm) {
            this.data = data;
            this.arm = arm;
            this.entity = data.wielder;
            this.stack = entity.getItemInHand(arm);
        }

        private void explode() {
            stop();
        }

        private void stop() {
            TRACKER_MAP.remove(arm);
        }

        private boolean isSameWeapon() {
            return !this.stack.isEmpty() && entity.getItemInHand(arm) == this.stack;
        }
    }
}
