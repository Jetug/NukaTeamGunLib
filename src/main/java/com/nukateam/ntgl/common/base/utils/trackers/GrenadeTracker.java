package com.nukateam.ntgl.common.base.utils.trackers;

import com.mrcrayfish.framework.api.sync.SyncedDataKey;
import com.nukateam.ntgl.Ntgl;
import com.nukateam.ntgl.common.foundation.init.ModSyncedDataKeys;
import com.nukateam.ntgl.common.foundation.item.WeaponItem;
import com.nukateam.ntgl.common.foundation.item.interfaces.IThrowable;
import com.nukateam.ntgl.common.util.util.GunData;
import com.nukateam.ntgl.common.util.util.GunModifierHelper;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
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
        var reloadKey = getDataKey(arm);
        reloadKey.setValue(entity, true);
        addTracker(entity, arm);
    }

    private static boolean addTracker(LivingEntity entity, InteractionHand arm) {
        var reloadKey = getDataKey(arm);

        var gunItem = entity.getItemInHand(arm).getItem();

        if (!TRACKER_MAP.containsKey(entity)) {
            if (!(gunItem instanceof WeaponItem)) {
                reloadKey.setValue(entity, false);
                return true;
            }
            TRACKER_MAP.put(entity, new Tracker(entity, arm));
        }
        return false;
    }

    private static void stopMelee(LivingEntity entity, InteractionHand arm) {
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
            var reloadKey = getDataKey(arm);
            reloadKey.setValue(shooter, false);
        }

        if(tracker.meleeTick > 0)
            tracker.meleeTick--;

        if(tracker.meleeTick == tracker.cooldown){
            tracker.tryMeleeAttack(shooter, tracker.stack);
        }

        if(tracker.meleeTick == 0){
            stopMelee(shooter, arm);
        }
    }

    private static SyncedDataKey<LivingEntity, Boolean> getDataKey(InteractionHand arm) {
        return arm == InteractionHand.MAIN_HAND ?
                ModSyncedDataKeys.MELEE_RIGHT : ModSyncedDataKeys.MELEE_LEFT;
    }

    private static class Tracker {
        private final InteractionHand arm;
        private final ItemStack stack;
        private final IThrowable throwable;
        private final int maxPrepare;
        private final int maxThrow;
        private final int maxLife;

        private int throwTick = 0;
        private int timeLeft = 0;
        private int lifeTick = 0;

        private boolean isPreparing = false;
        private boolean isThrowing = false;

        private Tracker(LivingEntity entity, InteractionHand arm) {
            this.arm = arm;
            this.stack = entity.getItemInHand(arm);
            this.throwable = (IThrowable) stack.getItem();
            this.maxPrepare = throwable.getConfig().getGeneral().getPrepareTime();
            this.maxThrow = throwable.getConfig().getGeneral().getThrowTime();
            this.maxLife = throwable.getConfig().getProjectile().getLife();
        }

//        public void tick(){
//            if()
//        }

        private boolean isSameWeapon(LivingEntity entity) {
            return !this.stack.isEmpty() && entity.getItemInHand(arm) == this.stack;
        }
    }
}
