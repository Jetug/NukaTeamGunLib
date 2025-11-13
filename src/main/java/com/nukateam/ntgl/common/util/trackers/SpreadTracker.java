package com.nukateam.ntgl.common.util.trackers;

import com.nukateam.ntgl.Config;
import com.nukateam.ntgl.Ntgl;
import com.nukateam.ntgl.common.foundation.init.ModSyncedDataKeys;
import com.nukateam.ntgl.common.foundation.item.interfaces.IWeapon;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import org.apache.commons.lang3.mutable.MutableInt;
import org.apache.commons.lang3.mutable.MutableLong;
import org.apache.commons.lang3.tuple.Pair;

import java.util.HashMap;
import java.util.Map;
import java.util.WeakHashMap;

/**
 * Author: MrCrayfish
 */
@EventBusSubscriber(modid = Ntgl.MOD_ID)
public class SpreadTracker {
    private static final Map<LivingEntity, SpreadTracker> TRACKER_MAP = new WeakHashMap<>();

    private final Map<IWeapon, Pair<MutableLong, MutableInt>> SPREAD_TRACKER_MAP = new HashMap<>();

    public void update(LivingEntity entity, IWeapon item) {
        var entry = SPREAD_TRACKER_MAP.computeIfAbsent(item, gun -> Pair.of(new MutableLong(-1), new MutableInt()));
        var lastFire = entry.getLeft();

        if (lastFire.getValue() != -1) {
            MutableInt spreadCount = entry.getRight();
            long deltaTime = System.currentTimeMillis() - lastFire.getValue();
            if (deltaTime < Config.COMMON.projectileSpread.spreadThreshold.get()) {
                if (spreadCount.getValue() < Config.COMMON.projectileSpread.maxCount.get()) {
                    spreadCount.increment();

                    /* Increases the spread count quicker if the player is not aiming down sight */
                    if (spreadCount.getValue() < Config.COMMON.projectileSpread.maxCount.get() && !ModSyncedDataKeys.AIMING.getValue(entity)) {
                        spreadCount.increment();
                    }
                }
            } else {
                spreadCount.setValue(0);
            }
        }
        lastFire.setValue(System.currentTimeMillis());
    }

    public float getSpread(IWeapon item) {
        var entry = SPREAD_TRACKER_MAP.get(item);
        if (entry != null) {
            return (float) entry.getRight().getValue() / (float) Config.COMMON.projectileSpread.maxCount.get();
        }
        return 0F;
    }

    public static SpreadTracker get(LivingEntity entity) {
        return TRACKER_MAP.computeIfAbsent(entity, player1 -> new SpreadTracker());
    }

    @SubscribeEvent
    public static void onPlayerDisconnect(PlayerEvent.PlayerLoggedOutEvent event) {
        MinecraftServer server = event.getEntity().getServer();
        if (server != null) {
            server.execute(() -> TRACKER_MAP.remove(event.getEntity()));
        }
    }

    @SubscribeEvent
    public static void onPlayerDisconnect(LivingDeathEvent event) {
        var entity = event.getEntity();
        MinecraftServer server = entity.getServer();
        if (server != null) {
            server.execute(() -> TRACKER_MAP.remove(entity));
        }
    }
}
