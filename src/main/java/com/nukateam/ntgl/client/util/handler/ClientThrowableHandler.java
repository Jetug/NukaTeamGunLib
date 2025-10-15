package com.nukateam.ntgl.client.util.handler;

import com.nukateam.ntgl.Ntgl;
import com.nukateam.ntgl.common.data.WeaponData;
import com.nukateam.ntgl.common.data.holders.AttackMode;
import com.nukateam.ntgl.common.foundation.item.interfaces.IThrowable;
import com.nukateam.ntgl.common.network.KeyAction;
import com.nukateam.ntgl.common.network.PacketHandler;
import com.nukateam.ntgl.common.network.message.C2SMessageGrenade;
import com.nukateam.ntgl.common.util.util.GunModifierHelper;
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

import static com.nukateam.ntgl.common.util.util.GunModifierHelper.canUseOffhandWeapon;

@Mod.EventBusSubscriber(modid = Ntgl.MOD_ID, value = Dist.CLIENT)
public class ClientThrowableHandler {
    private static final Map<InteractionHand, Tracker> TRACKER_MAP = new HashMap<>();

    @SubscribeEvent
    public static void tick(TickEvent.ClientTickEvent event){
        var minecraft = Minecraft.getInstance();
        if(event.phase == TickEvent.Phase.END && minecraft.player != null){
            if(minecraft.options.keyAttack.isDown()){
                var gunData = new WeaponData(minecraft.player.getMainHandItem(), minecraft.player)
                        .setWeaponAction(AttackMode.PRIMARY);
                addTracker(gunData, InteractionHand.MAIN_HAND);
            }
            else {
                removeTracker(InteractionHand.MAIN_HAND);
            }

            if(minecraft.options.keyUse.isDown()){
                var gunData = new WeaponData(minecraft.player.getOffhandItem(), minecraft.player)
                        .setWeaponAction(AttackMode.PRIMARY);
                addTracker(gunData, InteractionHand.OFF_HAND);
            }
            else {
                removeTracker(InteractionHand.OFF_HAND);
            }
        }
    }

    public static void addTracker(WeaponData weaponData, InteractionHand hand) {
        var player = weaponData.wielder;
        if(isThrowable(weaponData) && !TRACKER_MAP.containsKey(hand)){
            TRACKER_MAP.put(hand, new Tracker(player, hand));
            PacketHandler.getPlayChannel().sendToServer(new C2SMessageGrenade(KeyAction.HOLD, hand));
        }
    }

    private static void removeTracker(InteractionHand hand) {
        if(TRACKER_MAP.containsKey(hand)) {
            PacketHandler.getPlayChannel().sendToServer(new C2SMessageGrenade(KeyAction.RELEASE, hand));
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
            var gunData = new WeaponData(heldItem, player).setWeaponAction(AttackMode.PRIMARY);

            if (isThrowable(gunData)) {
                event.setCanceled(true);
                event.setSwingHand(false);
            }
        } else if (event.isUseItem()) {
            var offhandItem = player.getOffhandItem();
            var gunData = new WeaponData(offhandItem, player).setWeaponAction(AttackMode.PRIMARY);

            if (isThrowable(gunData) && canUseOffhandWeapon(player)) {
                event.setCanceled(true);
                event.setSwingHand(false);
            }
        }
    }

    private static boolean isThrowable(WeaponData weaponData) {
        return weaponData.weapon != null && weaponData.weapon.getItem() instanceof IThrowable && GunModifierHelper.isThrowable(weaponData);
    }

    private static class Tracker {
        private final InteractionHand arm;
        private final ItemStack stack;
        private final LivingEntity entity;

        private Tracker(LivingEntity entity, InteractionHand arm) {
            this.arm = arm;
            this.entity = entity;
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
