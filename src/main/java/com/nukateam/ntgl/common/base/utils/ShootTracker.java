package com.nukateam.ntgl.common.base.utils;

import com.nukateam.ntgl.common.data.util.GunModifierHelper;
import com.nukateam.ntgl.common.foundation.item.GunItem;
import com.google.common.collect.Maps;
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
     * applied on the client in {@link GunItem#onItemUseFirst(ItemStack, UseOnContext)} and {@link GunItem#onUsingTick}.
     */
    private static final Map<Pair<LivingEntity, InteractionHand>, ShootTracker> SHOOT_TRACKER_MAP = new WeakHashMap<>();

    private final Map<InteractionHand, Pair<Long, Integer>> cooldownMap = Maps.newHashMap();

    /**
     * Gets the cooldown tracker for the specified player UUID.
     *
     * @param entity the player instance
     * @return a cooldown tracker get
     */
    public static ShootTracker getShootTracker(LivingEntity entity, InteractionHand hand) {
        return SHOOT_TRACKER_MAP.computeIfAbsent(Pair.of(entity, hand), key -> new ShootTracker());
    }

    /**
     * Puts a cooldown for the specified gun item. This stores the time it was fired and the rate
     * of the weapon to determine when it's allowed to fire again.
     *
     * @param hand the hand gun get of the specified gun
     */
    public void putCooldown(ItemStack weapon, InteractionHand hand) {
//        var modifiedGun = item.getModifiedGun(weapon);
//        int rate = GunEnchantmentHelper.getRate(weapon, modifiedGun);
        var rate = GunModifierHelper.getRate(weapon);
        this.cooldownMap.put(hand, Pair.of(Util.getMillis(), rate * 50));
    }

    /**
     * Checks if the specified item has an active cooldown. If a cooldown is active, it means that
     * the weapon can not be fired until it has finished. This method provides leeway as sometimes a
     * weapon is ready to fire but the cooldown is not completely finished, rather it's in the last
     * 50 milliseconds or 1 game tick.
     *
     * @return if the specified gun item has an active cooldown
     */
    public boolean hasCooldown(InteractionHand hand) {
        var pair = this.cooldownMap.get(hand);
        if (pair != null) {
            /* Give a 50 millisecond leeway as most of the time the cooldown has finished, just not exactly to the millisecond */
            return Util.getMillis() - pair.getLeft() < pair.getRight() - 50;
        }
        return false;
    }

    /**
     * Gets the remaining milliseconds before the weapon is allowed to shoot again. This doesn't
     * take into account the leeway given in {@link #hasCooldown(InteractionHand)}.
     *
     * @return the remaining time in milliseconds
     */
    public long getRemaining(InteractionHand hand) {
        Pair<Long, Integer> pair = this.cooldownMap.get(hand);
        if (pair != null) {
            return pair.getRight() - (Util.getMillis() - pair.getLeft());
        }
        return 0;
    }
}
