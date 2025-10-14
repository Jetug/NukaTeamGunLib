package com.nukateam.ntgl.client.util.handler;

import com.mojang.datafixers.util.Pair;
import com.nukateam.ntgl.Ntgl;
import com.nukateam.ntgl.client.input.KeyBinds;
import com.nukateam.ntgl.common.data.holders.AttackMode;
import com.nukateam.ntgl.common.data.holders.WeaponMode;
import com.nukateam.ntgl.common.data.holders.MeleeMode;
import com.nukateam.ntgl.common.foundation.item.interfaces.IWeapon;
import com.nukateam.ntgl.common.util.trackers.EquipTracker;
import com.nukateam.ntgl.common.foundation.init.ModSyncedDataKeys;
import com.nukateam.ntgl.common.network.PacketHandler;
import com.nukateam.ntgl.common.network.message.C2SMessageMeleeAttack;
import com.nukateam.ntgl.common.data.GunData;
import com.nukateam.ntgl.common.util.util.GunModifierHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.*;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.HashMap;
import java.util.Map;

import static com.nukateam.ntgl.client.util.handler.ClientShootingHandler.isInGame;
import static com.nukateam.ntgl.common.util.util.GunModifierHelper.canUseOffhandWeapon;


@Mod.EventBusSubscriber(modid = Ntgl.MOD_ID, value = Dist.CLIENT)
public class ClientMeleeHandler {
    private static final Map<Pair<LivingEntity, InteractionHand>, ClientMeleeHandler> TRACKER_MAP = new HashMap<>();
    private int delayTick;
    private int cooldownTick;

    private ClientMeleeHandler(GunData data) {
        this.cooldownTick = GunModifierHelper.getMeleeCooldown(data);
        this.delayTick = GunModifierHelper.getMeleeDelay(data);
    }

    public static boolean isOnDelay(LivingEntity shooter, InteractionHand hand){
        var key = Pair.of(shooter, hand);
        return TRACKER_MAP.containsKey(key) && TRACKER_MAP.get(key).delayTick > 0;
    }

    public static boolean isOnCooldown(LivingEntity shooter, InteractionHand hand){
        var key = Pair.of(shooter, hand);
        return TRACKER_MAP.containsKey(key) && TRACKER_MAP.get(key).cooldownTick > 0;
    }

    public static void addTracker(GunData data, InteractionHand hand) {
        var entity = data.shooter;
        var gun = data.gun;
        var doMelee = ModSyncedDataKeys.getDoMelee(hand);
        assert gun != null && entity != null;

        if (gun.getItem() instanceof IWeapon
                && GunModifierHelper.canMelee(data)
                && !TRACKER_MAP.containsKey(Pair.of(entity, hand))
                && !doMelee.getValue(entity))
        {
            TRACKER_MAP.put(Pair.of(entity, hand), new ClientMeleeHandler(data));
            PacketHandler.getPlayChannel().sendToServer(new C2SMessageMeleeAttack(hand, data.weaponAction));
        }
    }
 
    @SubscribeEvent
    public static void onPostClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase == TickEvent.Phase.END && isInGame()) {
            var player = Minecraft.getInstance().player;
            assert player != null;

            var mainHandItem = player.getMainHandItem();
            var offhandItem = player.getOffhandItem();

            if (mainHandItem.getItem() instanceof IWeapon) {
                var data = new GunData(mainHandItem, player);

                if(isKeyAttackDown()) {
                    data.setWeaponAction(AttackMode.PRIMARY);
                }
                else if(isUseKeyDown()) {
                    data.setWeaponAction(AttackMode.SECONDARY);
                }
                else if(KeyBinds.KEY_ADD_ATTACK.isDown()) {
                    data.setWeaponAction(AttackMode.ADDITIONAL);
                }
                else if(KeyBinds.KEY_ALT_ATTACK.isDown()) {
                    data.setWeaponAction(AttackMode.ALTERNATIVE);
                }
                else return;

                handleAutoFire(data, InteractionHand.MAIN_HAND);
            }

            if (offhandItem.getItem() instanceof IWeapon && canUseOffhandWeapon(player) &&
                    !(mainHandItem.getItem() instanceof IWeapon)) {
                var data = new GunData(offhandItem, player);

                if(isUseKeyDown()) {
                    data.setWeaponAction(AttackMode.PRIMARY);
                }
                else if(KeyBinds.KEY_ADD_ATTACK.isDown()) {
                    data.setWeaponAction(AttackMode.ADDITIONAL);
                }
                else if(KeyBinds.KEY_ALT_ATTACK.isDown()) {
                    data.setWeaponAction(AttackMode.ALTERNATIVE);
                }
                else return;

                handleAutoFire(data, InteractionHand.OFF_HAND);
            }
        }
    }
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

    private static void handleAutoFire(GunData data, InteractionHand hand) {
        var mc = Minecraft.getInstance();
        var key = hand == InteractionHand.MAIN_HAND ?
                mc.options.keyAttack :
                mc.options.keyUse;
        var shooter = data.shooter;

        if(isMelee(data) && !EquipTracker.isEquiping(shooter, hand) && !shooter.isSpectator()){
            attack(data, hand);

            var mode = GunModifierHelper.getMeleeMode(data);
            if (mode == MeleeMode.SINGLE) {
                key.setDown(false);
            }
        }
    }

    private static void attack(GunData data, InteractionHand hand) {
        var shooter = data.shooter;
        var key = new Pair<>(shooter, hand);

        if(!TRACKER_MAP.containsKey(key)) {
            addTracker(data, hand);
        }
    }

    private static boolean isKeyAttackDown() {
        return Minecraft.getInstance().options.keyAttack.isDown();
    }

    private static boolean isUseKeyDown() {
        return Minecraft.getInstance().options.keyUse.isDown();
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

    private static boolean isMelee(GunData data) {
        return GunModifierHelper.getWeaponMode(data) == WeaponMode.MELEE;
    }
}
