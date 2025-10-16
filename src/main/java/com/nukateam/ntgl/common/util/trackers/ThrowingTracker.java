package com.nukateam.ntgl.common.util.trackers;

import com.nukateam.ntgl.Ntgl;
import com.nukateam.ntgl.common.data.WeaponData;
import com.nukateam.ntgl.common.data.holders.ThrowMode;
import com.nukateam.ntgl.common.foundation.item.interfaces.IThrowable;
import com.nukateam.ntgl.common.util.util.ThrowableStateHelper;
import com.nukateam.ntgl.common.util.util.WeaponModifierHelper;
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

    public static void start(WeaponData weaponData, InteractionHand arm){
        assert weaponData.wielder != null && weaponData.weapon != null;

        var entity = weaponData.wielder;
        var key = Pair.of(arm, entity);

        if (!TRACKER_MAP.containsKey(key)) {
            if (!(weaponData.weapon.getItem() instanceof IThrowable)) {
                return;
            }
            TRACKER_MAP.put(key, new Tracker(weaponData, arm, () -> TRACKER_MAP.remove(Pair.of(arm, entity))));
        }
        else {
            TRACKER_MAP.get(key).released = false;
        }
    }

    public static void onRelease(WeaponData weaponData, InteractionHand arm){
        var tracker = TRACKER_MAP.get(Pair.of(arm, weaponData.wielder));
        tracker.onRelease();
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
        private final WeaponData weaponData;
        private final InteractionHand arm;
        private final ItemStack stack;
        private final IThrowable throwable;
        private final int maxPrepare;
        private final int maxThrow;
        private final int maxLife;
        private final LivingEntity entity;
        private Runnable onStop;
        private boolean released = false;
        private int prepareTick;
        private int throwTick;
        private int lifeTick;

        private Tracker(WeaponData weaponData, InteractionHand arm, Runnable onStop) {
            this.weaponData = weaponData;
            this.arm = arm;
            this.entity = weaponData.wielder;
            this.onStop = onStop;
            this.stack = entity.getItemInHand(arm);
            this.throwable = (IThrowable) stack.getItem();
            this.maxPrepare = WeaponModifierHelper.getPrepareTime(weaponData);
            this.maxThrow = WeaponModifierHelper.getThrowTime(weaponData);
            this.maxLife = WeaponModifierHelper.getThrowable(weaponData).getProjectile().getLife();

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
                if(ThrowableStateHelper.getThrowMode(weaponData) == ThrowMode.UNSAFE) {
                    lifeTick = Math.max(lifeTick - 1, 0);
                }

                if(!isThrowing()){
                    if(released){
                        setPreparing(false);
                        setThrowing(true);
                        setHolding(false);
                    }
                    else {
                        setHolding(true);
                    }
                }
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
            this.released = true;
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
