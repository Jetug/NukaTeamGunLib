package com.nukateam.ntgl.common.util.trackers;

import com.nukateam.ntgl.common.foundation.item.WeaponItem;
import com.nukateam.ntgl.common.data.GunData;
import com.nukateam.ntgl.common.util.util.GunModifierHelper;
import net.minecraft.Util;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import org.apache.commons.lang3.tuple.Pair;
import java.util.Map;
import java.util.WeakHashMap;

/**
 * A simple class to track and control weapon cooldowns
 * <p>
 * Author: MrCrayfish
 */
public class ShootTracker {
    /**
     * A custom implementation of the cooldown tracker in order to provide the best experience for
     * players. On servers, Minecraft's cooldown tracker is sent to the client but the latency creates
     * an awkward experience as the cooldown applies to the item after the packet has traveled to the
     * server then back to the client. To fix this and still apply security, we just handle the
     * cooldown tracker quietly and not send cooldown packet back to client. The cooldown is still
     * applied on the client in {@link WeaponItem#onItemUseFirst(ItemStack, UseOnContext)} and {@link WeaponItem#onUsingTick}.
     */
    private static final Map<Pair<LivingEntity, InteractionHand>, ShootTracker> SHOOT_TRACKER_MAP = new WeakHashMap<>();

    private Pair<Long, Integer> cooldownMap = Pair.of(0L, 0);
    private final InteractionHand hand;

    public ShootTracker(InteractionHand hand) {
        this.hand = hand;
    }

    /**
     * Gets the cooldown tracker for the specified player UUID.
     *
     * @param entity the player instance
     * @return a cooldown tracker get
     */
    public static ShootTracker getShootTracker(LivingEntity entity, InteractionHand hand) {
        return SHOOT_TRACKER_MAP.computeIfAbsent(Pair.of(entity, hand), key -> new ShootTracker(hand));
    }

    /**
     * Puts a cooldown for the specified gun item. This stores the time it was fired and the rate
     * of the weapon to determine when it's allowed to fire again.
     */
    public void putCooldown(ItemStack weapon, LivingEntity shooter) {
        var data = new GunData(weapon, shooter);
        var rate = GunModifierHelper.getRate(data);
        this.cooldownMap = Pair.of(Util.getMillis(), rate * 50);
    }

    /**
     * Checks if the specified item has an active cooldown. If a cooldown is active, it means that
     * the weapon can not be fired until it has finished. This method provides leeway as sometimes a
     * weapon is ready to fire but the cooldown is not completely finished, rather it's in the last
     * 50 milliseconds or 1 game tick.
     *
     * @return if the specified gun item has an active cooldown
     */
    public boolean hasCooldown() {
        if (this.cooldownMap != null) {
            /* Give a 50 millisecond leeway as most of the time the cooldown has finished, just not exactly to the millisecond */
            return Util.getMillis() - this.cooldownMap.getLeft() < this.cooldownMap.getRight() - 50;
        }
        return false;
    }

    /**
     * Gets the remaining milliseconds before the weapon is allowed to shoot again. This doesn't
     * take into account the leeway given in {@link #hasCooldown()}.
     *
     * @return the remaining time in milliseconds
     */
    public long getRemaining() {
        if (this.cooldownMap != null) {
            return this.cooldownMap.getRight() - (Util.getMillis() - this.cooldownMap.getLeft());
        }
        return 0;
    }
}
