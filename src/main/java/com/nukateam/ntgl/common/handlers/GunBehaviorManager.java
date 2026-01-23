package com.nukateam.ntgl.common.handlers;

import com.nukateam.ntgl.Ntgl;
import com.nukateam.ntgl.common.foundation.goals.GunAttackGoal;
import com.nukateam.ntgl.common.foundation.item.interfaces.IWeapon;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Mod.EventBusSubscriber(modid = Ntgl.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class GunBehaviorManager {
    private static final Map<UUID, GunAttackGoal> activeGoals = new HashMap<>();
    private static final int CHECK_INTERVAL = 20;

    @SubscribeEvent
    public static void onLivingTick(LivingEvent.LivingTickEvent event) {
        if (!(event.getEntity() instanceof PathfinderMob mob)) return;
        if (mob.level().isClientSide()) return;

        if (mob.tickCount % CHECK_INTERVAL != 0) return;

        var mobId = mob.getUUID();
        var hasWeapon = mob.getMainHandItem().getItem() instanceof IWeapon ||
                mob.getOffhandItem().getItem() instanceof IWeapon;

        if (hasWeapon) {
            if (!activeGoals.containsKey(mobId)) {
                addGunGoalToMob(mob);
            }
        } else {
            if (activeGoals.containsKey(mobId)) {
                removeGunGoalFromMob(mob);
            }
        }
    }

    private static void addGunGoalToMob(PathfinderMob mob) {
        var gunGoal = new GunAttackGoal(mob, 1.0D, 1.0F, 30.0F);

        mob.goalSelector.addGoal(2, gunGoal);
        activeGoals.put(mob.getUUID(), gunGoal);

        Ntgl.LOGGER.debug("Added gun behavior to {}", mob);
    }

    private static void removeGunGoalFromMob(PathfinderMob mob) {
        var goal = activeGoals.remove(mob.getUUID());
        if (goal != null) {
            mob.goalSelector.removeGoal(goal);
            Ntgl.LOGGER.debug("Removed gun behavior from {}", mob);
        }
    }

    @SubscribeEvent
    public static void onEntityDeath(LivingDeathEvent event) {
        if (event.getEntity() instanceof PathfinderMob mob) {
            activeGoals.remove(mob.getUUID());
        }
    }
}