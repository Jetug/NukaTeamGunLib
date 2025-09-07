package com.nukateam.ntgl.common.util.trackers;

import com.nukateam.ntgl.Ntgl;
import com.nukateam.ntgl.common.data.holders.ThrowMode;
import com.nukateam.ntgl.common.foundation.item.interfaces.IThrowable;
import com.nukateam.ntgl.common.util.util.ThrowableStateHelper;
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
import com.mojang.datafixers.util.Pair;

import java.util.*;

import static com.nukateam.ntgl.common.foundation.init.ModSyncedDataKeys.*;

@Mod.EventBusSubscriber(modid = Ntgl.MOD_ID)
public class ThrowingTracker {
    private static final Map<Pair<InteractionHand, LivingEntity>, Tracker> TRACKER_MAP = new HashMap<>();

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
                for (var key: TRACKER_MAP.keySet()) {
                    var entity = key.getSecond();
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
            server.execute(() -> {
                TRACKER_MAP.remove(Pair.of(InteractionHand.MAIN_HAND, event.getEntity()));
                TRACKER_MAP.remove(Pair.of(InteractionHand.OFF_HAND, event.getEntity()));
            });
        }
    }

    public static void start(LivingEntity entity, InteractionHand arm){
        addTracker(entity, arm);
    }

    public static void onRelease(LivingEntity entity, InteractionHand arm){
        var tracker = TRACKER_MAP.get(Pair.of(arm, entity));
        tracker.onRelease();
    }

    private static boolean addTracker(LivingEntity entity, InteractionHand arm) {
        var gunItem = entity.getItemInHand(arm).getItem();
        var key = Pair.of(arm, entity);

        if (!TRACKER_MAP.containsKey(key)) {
            if (!(gunItem instanceof IThrowable)) {
                return true;
            }
            TRACKER_MAP.put(key, new Tracker(entity, arm, () -> TRACKER_MAP.remove(Pair.of(arm, entity))));
        }
        return false;
    }

    private static void onEntityTick(LivingEntity entity) {
        handTick(entity, InteractionHand.MAIN_HAND);
        handTick(entity, InteractionHand.OFF_HAND);
    }

    private static void handTick(LivingEntity shooter, InteractionHand arm) {
        var tracker = TRACKER_MAP.get(Pair.of(arm, shooter));

        if(tracker != null) {
            if (!tracker.isSameWeapon()) {
                tracker.stop();
            } else {
                tracker.tick();
            }
        }
    }

    private static class Tracker {
        private final InteractionHand arm;
        private final ItemStack stack;
        private final IThrowable throwable;
        private final int maxPrepare;
        private final int maxThrow;
        private final int maxLife;
        private final LivingEntity entity;
        private Runnable onStop;

        private int prepareTick;
        private int throwTick;
        private int lifeTick;

        private Tracker(LivingEntity entity, InteractionHand arm, Runnable onStop) {
            this.arm = arm;
            this.entity = entity;
            this.onStop = onStop;
            this.stack = entity.getItemInHand(arm);
            this.throwable = (IThrowable) stack.getItem();
            this.maxPrepare = throwable.getConfig().getGeneral().getPrepareTime();
            this.maxThrow = throwable.getConfig().getGeneral().getThrowTime();
            this.maxLife = throwable.getConfig().getProjectile().getLife();

            prepareTick = maxPrepare;
            throwTick = maxThrow;
            lifeTick = maxLife;

            setPreparing(true);
            setThrowing(false);
        }

        public boolean isPreparing() {
            return getPreparingDataKey(arm).getValue(entity);
        }

        public boolean isThrowing() {
            return getThrowingDataKey(arm).getValue(entity);
        }

        public void setThrowing(boolean value) {
            getThrowingDataKey(arm).setValue(entity, value);
        }

        public void setPreparing(boolean value) {
            getPreparingDataKey(arm).setValue(entity, value);
        }

        public void setHolding(boolean value) {
            getHoldingDataKey(arm).setValue(entity, value);
        }

        public void tick(){
            if(!isSameWeapon()) stop();

            prepareTick = Math.max(prepareTick - 1, 0);

            if(prepareTick == 0){
                if(ThrowableStateHelper.getThrowMode(stack) == ThrowMode.UNSAFE) {
                    lifeTick = Math.max(lifeTick - 1, 0);
                }

                setHolding(true);

                if(lifeTick == 0){
                    onExpire();
                }
                else if (isThrowing()){
                    throwTick = Math.max(throwTick - 1, 0);
                }
                if(throwTick == 0){
                    throwItem();
                    stop();
                }
            }
        }

        public void onRelease(){
            if(prepareTick == 0){
                setPreparing(false);
                setThrowing(true);
                setHolding(false);
            }
            else stop();
        }

        private void throwItem(){
            throwable.throwItem(stack, entity, lifeTick);
        }

        private void onExpire() {
            throwable.expire(entity);
            stop();
        }

        private void stop() {
            onStop.run();
            setPreparing(false);
            setThrowing(false);
            setHolding(false);
        }

        private boolean isSameWeapon() {
            var heldItem = entity.getItemInHand(arm);
            return !this.stack.isEmpty() && heldItem == this.stack;
        }
    }
}
