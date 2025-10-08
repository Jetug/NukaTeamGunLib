package com.nukateam.ntgl.client.util.handler;

import com.nukateam.ntgl.Ntgl;
import com.nukateam.ntgl.common.data.GunData;
import com.nukateam.ntgl.common.data.holders.WeaponMode;
import com.nukateam.ntgl.common.foundation.item.interfaces.IThrowable;
import com.nukateam.ntgl.common.network.KeyAction;
import com.nukateam.ntgl.common.network.PacketHandler;
import com.nukateam.ntgl.common.network.message.C2SMessageGrenade;
import com.nukateam.ntgl.common.util.util.GunModifierHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.HashMap;
import java.util.Map;

import static com.nukateam.ntgl.common.util.util.GunModifierHelper.canRenderInOffhand;

@Mod.EventBusSubscriber(modid = Ntgl.MOD_ID, value = Dist.CLIENT)
public class ClientThrowableHandler {
    private static final Map<InteractionHand, Tracker> TRACKER_MAP = new HashMap<>();

    @SubscribeEvent
    public static void tick(TickEvent.ClientTickEvent event){
        var minecraft = Minecraft.getInstance();
        if(event.phase == TickEvent.Phase.END && minecraft.player != null){
            if(minecraft.options.keyAttack.isDown()){
                addTracker(minecraft.player, InteractionHand.MAIN_HAND);
            }
            else {
                removeTracker(InteractionHand.MAIN_HAND);
            }

            if(minecraft.options.keyUse.isDown()){
                addTracker(minecraft.player, InteractionHand.OFF_HAND);
            }
            else {
                removeTracker(InteractionHand.OFF_HAND);
            }
        }
    }

    private static void addTracker(Player player, InteractionHand hand) {
        var heldItem = player.getItemInHand(hand);
        if(isThrowable(heldItem, player) && !TRACKER_MAP.containsKey(hand)){
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

            if (isThrowable(heldItem, player)) {
                event.setCanceled(true);
                event.setSwingHand(false);
            }
        } else if (event.isUseItem()) {
            var offhandItem = player.getOffhandItem();

            if (isThrowable(offhandItem, player) && canRenderInOffhand(player)) {
                event.setCanceled(true);
                event.setSwingHand(false);
            }
        }
    }

    private static boolean isThrowable(ItemStack heldItem, Player player) {
        return heldItem.getItem() instanceof IThrowable && GunModifierHelper.isThrowable(new GunData(heldItem, player));
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
