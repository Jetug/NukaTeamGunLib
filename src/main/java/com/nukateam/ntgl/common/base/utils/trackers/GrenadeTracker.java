package com.nukateam.ntgl.common.base.utils.trackers;

import com.mrcrayfish.framework.api.sync.SyncedDataKey;
import com.nukateam.ntgl.Ntgl;
import com.nukateam.ntgl.common.foundation.init.ModSyncedDataKeys;
import com.nukateam.ntgl.common.foundation.item.interfaces.IThrowable;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.fml.common.Mod;

import java.util.*;

@Mod.EventBusSubscriber(modid = Ntgl.MOD_ID)
public class GrenadeTracker {
    private static final Map<LivingEntity, Tracker> TRACKER_MAP = new HashMap<>();

    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        try {
            if (event.phase == TickEvent.Phase.START && !event.player.level().isClientSide) {
                var player = event.player;
                onEntityTick(player);
            }
        }
        catch (Exception e){
            Ntgl.LOGGER.error(e.getMessage(), e);
        }
    }

    @SubscribeEvent
    public static void onServerTick(TickEvent.ServerTickEvent event) {
        try {
            if (event.phase == TickEvent.Phase.START && event.side == LogicalSide.SERVER) {
                for (var entity: TRACKER_MAP.keySet()) {
                    if(entity instanceof Player) continue;
                    onEntityTick(entity);
                }
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

    public static void start(LivingEntity entity, InteractionHand arm){
        var dataKey = getDataKey(arm);
        dataKey.setValue(entity, true);
        addTracker(entity, arm);
    }

    private static boolean addTracker(LivingEntity entity, InteractionHand arm) {
        var dataKey = getDataKey(arm);

        var gunItem = entity.getItemInHand(arm).getItem();

        if (!TRACKER_MAP.containsKey(entity)) {
            if (!(gunItem instanceof IThrowable)) {
                dataKey.setValue(entity, false);
                return true;
            }
            TRACKER_MAP.put(entity, new Tracker(entity, arm));
        }
        return false;
    }

    private static void stop(LivingEntity entity, InteractionHand arm) {
        var dataKey = getDataKey(arm);
        TRACKER_MAP.remove(entity);
        dataKey.setValue(entity, false);
    }

    private static void onEntityTick(LivingEntity entity) {
        if (ModSyncedDataKeys.MELEE_RIGHT.getValue(entity)) {
            handTick(entity, InteractionHand.MAIN_HAND);
        }
        else if (ModSyncedDataKeys.MELEE_LEFT.getValue(entity)) {
            handTick(entity, InteractionHand.OFF_HAND);
        }
        else if (TRACKER_MAP.containsKey(entity)) {
            TRACKER_MAP.remove(entity);
        }
    }

    private static void handTick(LivingEntity shooter, InteractionHand arm) {
        var tracker = TRACKER_MAP.get(shooter);
        var isSameWeapon = !tracker.isSameWeapon(shooter);

        if (isSameWeapon) {
            TRACKER_MAP.remove(shooter);
            var dataKey = getDataKey(arm);
            dataKey.setValue(shooter, false);
        }

    }

    private static SyncedDataKey<LivingEntity, Boolean> getDataKey(InteractionHand arm) {
        return arm == InteractionHand.MAIN_HAND ?
                ModSyncedDataKeys.PREPARE_RIGHT : ModSyncedDataKeys.PREPARE_LEFT;
    }

    private static class Tracker {
        private final InteractionHand arm;
        private final ItemStack stack;
        private final IThrowable throwable;
        private final int maxPrepare;
        private final int maxThrow;
        private final int maxLife;
        private final LivingEntity entity;

        private int prepareTick = 0;
        private int throwTick = 0;
        private int lifeTick = 0;

        private boolean isPreparing = true;
        private boolean isThrowing = false;

        private Tracker(LivingEntity entity, InteractionHand arm) {
            this.arm = arm;
            this.entity = entity;
            this.stack = entity.getItemInHand(arm);
            this.throwable = (IThrowable) stack.getItem();
            this.maxPrepare = throwable.getConfig().getGeneral().getPrepareTime();
            this.maxThrow = throwable.getConfig().getGeneral().getThrowTime();
            this.maxLife = throwable.getConfig().getProjectile().getLife();

            prepareTick = maxPrepare;
            throwTick = maxThrow;
            lifeTick = maxLife;
        }

        public void tick(){
            if(!isSameWeapon()) stop();

            prepareTick = Math.max(prepareTick - 1, 0);

            if(prepareTick == 0){
                isPreparing = false;
                lifeTick = Math.max(lifeTick - 1, 0);

                if(lifeTick == 0){
                    explode();
                }

                if (isThrowing){
                    throwTick = Math.max(throwTick - 1, 0);
                }
            }
//            throwTick = Math.max(prepareTick - 1, 0);
        }

        public boolean isPreparing() {
            return isPreparing;
        }

        public boolean isThrowing() {
            return isThrowing;
        }

        public void onRelease(){
            if(prepareTick == 0){
                isThrowing = true;
                throwItem();
            }
        }

        private void throwItem(){
            throwable.throwItem(stack, entity, lifeTick);
            stop();
        }

        private void explode() {
            throwable.explode(entity);
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
