package com.nukateam.ntgl.common.util.trackers;

import com.mojang.datafixers.util.Pair;
import com.nukateam.ntgl.Ntgl;
import com.nukateam.ntgl.common.foundation.init.ModSyncedDataKeys;
import com.nukateam.ntgl.common.foundation.item.interfaces.IThrowable;
import com.nukateam.ntgl.common.foundation.item.interfaces.IWeapon;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.Map;
import java.util.WeakHashMap;

@Mod.EventBusSubscriber(modid = Ntgl.MOD_ID)
public class EquipTracker {
    private static final Map<Pair<InteractionHand, LivingEntity>, Tracker> TRACKER_MAP = new WeakHashMap<>();

    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        try {
            if (event.phase == TickEvent.Phase.START && !event.player.level().isClientSide) {
                var player = event.player;
                handTick(player, InteractionHand.MAIN_HAND);
                handTick(player, InteractionHand.OFF_HAND);
            }
        }
        catch (Exception e){
            Ntgl.LOGGER.error(e.getMessage(), e);
        }
    }

    @SubscribeEvent
    public static void onPlayerLoggedOut(PlayerEvent.PlayerLoggedOutEvent event) {
        MinecraftServer server = event.getEntity().getServer();
        if (server != null) {
            server.execute(() -> TRACKER_MAP.remove(event.getEntity()));
        }
    }

    private static void handTick(Player entity, InteractionHand arm) {
        var key = new Pair<>(arm, entity);
        var tracker = TRACKER_MAP.get(key);
        if(tracker != null){
            if(tracker.equipTick > 0 && tracker.isSameItem()){
                tracker.equipTick--;
            }
            else stopEquip(entity, arm);
        }
//        else stopEquip(entity, arm);
    }

    public static boolean isEquiping(LivingEntity entity, InteractionHand arm){
        return ModSyncedDataKeys.getEquipKey(arm).getValue(entity);
    }

    public static void startEquip(LivingEntity entity, InteractionHand arm, int equipTime){
        var reloadKey = ModSyncedDataKeys.getEquipKey(arm);
        reloadKey.setValue(entity,true);
        addTracker(entity, arm, equipTime);
    }

    public static void stopEquip(LivingEntity entity, InteractionHand arm) {
        var dataKey = ModSyncedDataKeys.getEquipKey(arm);
        dataKey.setValue(entity, false);
        TRACKER_MAP.remove(new Pair<>(arm, entity));
    }

    private static void addTracker(LivingEntity entity, InteractionHand arm, int equipTime) {
        var reloadKey = ModSyncedDataKeys.getEquipKey(arm);
        var heldItem = entity.getItemInHand(arm).getItem();
        var key = new Pair<>(arm, entity);

        if (!TRACKER_MAP.containsKey(key)) {
            if (!(heldItem instanceof IWeapon) && !(heldItem instanceof IThrowable)) {
                reloadKey.setValue(entity, false);
                return;
            }
            TRACKER_MAP.put(key, new Tracker(entity, arm, equipTime));
        }
    }

    private static class Tracker{
        private final InteractionHand arm;
        private final ItemStack stack;
        private final LivingEntity entity;
        private int equipTick;

        private Tracker(LivingEntity entity, InteractionHand arm, int equipTime) {
            this.arm = arm;
            this.stack = entity.getItemInHand(arm);
            this.entity = entity;
            this.equipTick = equipTime;
        }

        private boolean isSameItem(){
            var heldItem = entity.getItemInHand(arm);
            return !this.stack.isEmpty() && heldItem == this.stack;
        }
    }
}
