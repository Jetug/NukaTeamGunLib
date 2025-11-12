package com.nukateam.ntgl.client.util.handler;

import com.mojang.datafixers.util.Pair;
import com.nukateam.ntgl.Ntgl;
import com.nukateam.ntgl.common.data.WeaponData;
import com.nukateam.ntgl.common.data.holders.WeaponAction;
import com.nukateam.ntgl.common.data.holders.MeleeMode;
import com.nukateam.ntgl.common.util.trackers.EquipTracker;
import com.nukateam.ntgl.common.foundation.init.ModSyncedDataKeys;
import com.nukateam.ntgl.common.network.PacketHandler;
import com.nukateam.ntgl.common.network.message.C2SMessageMeleeAttack;
import com.nukateam.ntgl.common.util.util.WeaponModifierHelper;
import net.minecraft.client.KeyMapping;
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
    private static final Map<Pair<LivingEntity, InteractionHand>, ClientMeleeTracker> TRACKER_MAP = new HashMap<>();

    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        try {
            if (event.phase == TickEvent.Phase.START) {
                for (var pair: TRACKER_MAP.keySet()) {
                    onEntityTick(pair.getFirst(), pair.getSecond());
                }
            }
        }
        catch (Exception e){
            Ntgl.LOGGER.error(e.getMessage(), e);
        }
    }

    public static ClientMeleeTracker getTracker(LivingEntity shooter, InteractionHand hand) {
        return TRACKER_MAP.get(Pair.of(shooter, hand));
    }

    public static boolean isOnDelay(LivingEntity shooter, InteractionHand hand){
        var key = Pair.of(shooter, hand);
        return TRACKER_MAP.containsKey(key) && TRACKER_MAP.get(key).delayTick > 0;
    }

    public static boolean isOnCooldown(LivingEntity shooter, InteractionHand hand){
        var key = Pair.of(shooter, hand);
        return TRACKER_MAP.containsKey(key) && TRACKER_MAP.get(key).cooldownTick > 0;
    }

    public static void addTracker(WeaponData data, InteractionHand hand) {
        var entity = data.wielder;
        var gun = data.weapon;
        var doMeleeKey = ModSyncedDataKeys.getMeleeKey(hand);
        var doMelee = doMeleeKey.getValue(entity);
        assert gun != null && entity != null;

        if (WeaponModifierHelper.isWeaponItem(gun)
                && WeaponModifierHelper.canMelee(data)
                && !TRACKER_MAP.containsKey(Pair.of(entity, hand))
                && !doMelee)
        {
            TRACKER_MAP.put(Pair.of(entity, hand), new ClientMeleeTracker(data));
            PacketHandler.getPlayChannel().sendToServer(new C2SMessageMeleeAttack(hand, data.weaponMode));
        }
    }

    public static void handleInput(WeaponData data, InteractionHand hand, KeyMapping key) {
        var shooter = data.wielder;

        if(isMelee(data) && !EquipTracker.isEquiping(shooter, hand) && !shooter.isSpectator()){
            attack(data, hand);

            var mode = WeaponModifierHelper.getMeleeMode(data);
            if (mode == MeleeMode.SINGLE) {
                key.setDown(false);
            }
        }
    }

    private static void attack(WeaponData data, InteractionHand hand) {
        var shooter = data.wielder;
        var key = new Pair<>(shooter, hand);

        if(!TRACKER_MAP.containsKey(key)) {
            addTracker(data, hand);
        }
    }

    private static void onEntityTick(LivingEntity shooter, InteractionHand hand) {
        var tracker = TRACKER_MAP.get(Pair.of(shooter, hand));
        if(tracker.delayTick > 0) {
            tracker.delayTick--;
        } else if(tracker.cooldownTick > 0){
            tracker.cooldownTick--;
        } else if(tracker.cooldownTick == 0){
            stopMelee(shooter, hand);
        }
    }


    private static void stopMelee(LivingEntity entity, InteractionHand arm) {
        TRACKER_MAP.remove(Pair.of(entity, arm));
    }

    private static boolean isMelee(WeaponData data) {
        return WeaponModifierHelper.getWeaponAction(data) == WeaponAction.MELEE;
    }

    public static class ClientMeleeTracker{
        private int delayTick;
        private int cooldownTick;
        private final WeaponData data;

        public ClientMeleeTracker(WeaponData data) {
            this.cooldownTick = WeaponModifierHelper.getMeleeCooldown(data);
            this.delayTick = WeaponModifierHelper.getMeleeDelay(data);
            this.data = data;
        }

        public WeaponData getData() {
            return data;
        }

    }
}
