package com.nukateam.ntgl.client.util.handler;

import com.mojang.datafixers.util.Pair;
import com.nukateam.ntgl.Ntgl;
import com.nukateam.ntgl.common.foundation.init.ModSyncedDataKeys;
import com.nukateam.ntgl.common.foundation.item.GunItem;
import com.nukateam.ntgl.common.network.PacketHandler;
import com.nukateam.ntgl.common.network.message.C2SMessageMeleeAttack;
import com.nukateam.ntgl.common.util.util.GunData;
import com.nukateam.ntgl.common.util.util.GunModifierHelper;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.*;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.HashMap;
import java.util.Map;


@Mod.EventBusSubscriber(modid = Ntgl.MOD_ID, value = Dist.CLIENT)
public class ClientMeleeHandler {
    private static final Map<Pair<LivingEntity, InteractionHand>, ClientMeleeHandler> TRACKER_MAP = new HashMap<>();
    private final InteractionHand arm;
    private int meleeTick;
    private int cooldownTick;

    private ClientMeleeHandler(LivingEntity entity, InteractionHand arm) {
        this.arm = arm;
        var stack = entity.getItemInHand(arm);
        var data = new GunData(stack, entity);
        this.cooldownTick = GunModifierHelper.getMeleeCooldown(data);
        this.meleeTick = GunModifierHelper.getMeleeDelay(data);
    }

    public static void addTracker(LivingEntity entity, InteractionHand arm) {
        var doMelee = ModSyncedDataKeys.getDoMelee(arm);
        var gun = entity.getItemInHand(arm);
        var data = new GunData(gun, entity);

        if (gun.getItem() instanceof GunItem
                && GunModifierHelper.canMelee(data)
                && !TRACKER_MAP.containsKey(Pair.of(entity, arm))
                && !doMelee.getValue(entity))
        {
            TRACKER_MAP.put(Pair.of(entity, arm), new ClientMeleeHandler(entity, arm));
            PacketHandler.getPlayChannel().sendToServer(new C2SMessageMeleeAttack());
        }
    }

    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        try {
            if (event.phase == TickEvent.Phase.START) {
                for (var pair: TRACKER_MAP.keySet()) {
                    onEntityTick(pair.getFirst());
                }
            }
        }
        catch (Exception e){
            Ntgl.LOGGER.error(e.getMessage(), e);
        }
    }

    private static void onEntityTick(LivingEntity entity) {
//        if (ModSyncedDataKeys.MELEE_RIGHT.getValue(entity)) {
            handTick(entity, InteractionHand.MAIN_HAND);
//        }
//        if (ModSyncedDataKeys.MELEE_LEFT.getValue(entity)) {
            handTick(entity, InteractionHand.OFF_HAND);
//        }
    }

    private static void handTick(LivingEntity shooter, InteractionHand arm) {
        var tracker = TRACKER_MAP.get(Pair.of(shooter, arm));
        if(tracker.meleeTick > 0) {
            tracker.meleeTick--;
        } else if(tracker.cooldownTick > 0){
            tracker.cooldownTick--;
        } else if(tracker.cooldownTick == 0){
            stopMelee(shooter, arm);
        }
    }

    private static void stopMelee(LivingEntity entity, InteractionHand arm) {
        TRACKER_MAP.remove(Pair.of(entity, arm));
    }

    public static boolean isOnDelay(LivingEntity shooter, InteractionHand hand){
        var key = Pair.of(shooter, hand);
        return TRACKER_MAP.containsKey(key) && TRACKER_MAP.get(key).meleeTick > 0;
    }

    public static boolean isOnCooldown(LivingEntity shooter, InteractionHand hand){
        var key = Pair.of(shooter, hand);
        return TRACKER_MAP.containsKey(key) && TRACKER_MAP.get(key).cooldownTick > 0;
    }
}
