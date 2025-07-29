package com.nukateam.ntgl.client.util.handler;

import com.mojang.datafixers.util.Pair;
import com.nukateam.ntgl.Ntgl;
import com.nukateam.ntgl.common.data.holders.MeleeMode;
import com.nukateam.ntgl.common.data.holders.WeaponMode;
import com.nukateam.ntgl.common.util.trackers.EquipTracker;
import com.nukateam.ntgl.common.event.MeleeAttackEvent;
import com.nukateam.ntgl.common.foundation.init.ModSyncedDataKeys;
import com.nukateam.ntgl.common.foundation.item.WeaponItem;
import com.nukateam.ntgl.common.network.PacketHandler;
import com.nukateam.ntgl.common.network.message.C2SMessageMeleeAttack;
import com.nukateam.ntgl.common.data.GunData;
import com.nukateam.ntgl.common.util.util.GunModifierHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.*;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.HashMap;
import java.util.Map;

import static com.nukateam.ntgl.client.util.handler.ShootingHandler.isInGame;
import static com.nukateam.ntgl.common.util.util.GunModifierHelper.canRenderInOffhand;


@Mod.EventBusSubscriber(modid = Ntgl.MOD_ID, value = Dist.CLIENT)
public class ClientMeleeHandler {
    private static final Map<Pair<LivingEntity, InteractionHand>, ClientMeleeHandler> TRACKER_MAP = new HashMap<>();
    private final InteractionHand arm;
    private int delayTick;
    private int cooldownTick;

    private ClientMeleeHandler(LivingEntity entity, InteractionHand arm) {
        this.arm = arm;
        var stack = entity.getItemInHand(arm);
        var data = new GunData(stack, entity);
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

    public static void addTracker(LivingEntity entity, InteractionHand arm) {
        var doMelee = ModSyncedDataKeys.getDoMelee(arm);
        var gun = entity.getItemInHand(arm);
        var data = new GunData(gun, entity);

        if (gun.getItem() instanceof WeaponItem
                && GunModifierHelper.canMelee(data)
                && !TRACKER_MAP.containsKey(Pair.of(entity, arm))
                && !doMelee.getValue(entity))
        {
            TRACKER_MAP.put(Pair.of(entity, arm), new ClientMeleeHandler(entity, arm));
            PacketHandler.getPlayChannel().sendToServer(new C2SMessageMeleeAttack());
        }
    }
 
    @SubscribeEvent
    public static void onPostClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase == TickEvent.Phase.END && isInGame()) {
            var player = Minecraft.getInstance().player;
            assert player != null;

            var mainHandItem = player.getMainHandItem();
            var offhandItem = player.getOffhandItem();

            if (mainHandItem.getItem() instanceof WeaponItem && isKeyAttackDown()) {
                handleAutoFire(player, mainHandItem, InteractionHand.MAIN_HAND);
            }

            if (offhandItem.getItem() instanceof WeaponItem && canRenderInOffhand(player) && isUseKeyDown()) {
                handleAutoFire(player, offhandItem, InteractionHand.OFF_HAND);
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

    private static void handleAutoFire(LocalPlayer player, ItemStack heldItem, InteractionHand hand) {
        var mc = Minecraft.getInstance();
        var key = hand == InteractionHand.MAIN_HAND ?
                mc.options.keyAttack :
                mc.options.keyUse;

        attack(player, heldItem, hand);

        if(heldItem.getItem() instanceof WeaponItem weaponItem && isMelee(heldItem, player)){
            var mode = weaponItem.getGun().getMelee().getMode();
            if (mode == MeleeMode.SINGLE) {
                key.setDown(false);
            }
        }
    }

    private static void attack(LivingEntity shooter, ItemStack heldItem, InteractionHand hand) {
        if (heldItem.getItem() instanceof WeaponItem
                && isMelee(heldItem, shooter)
                && !EquipTracker.isEquiping(shooter, hand)
                && !shooter.isSpectator()) {

            if (MinecraftForge.EVENT_BUS.post(new MeleeAttackEvent.Pre(shooter, heldItem, hand)))
                return;

            var key = new Pair<>(shooter, hand);

            if(!TRACKER_MAP.containsKey(key)) {
                addTracker(shooter, hand);
            }
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

    private static boolean isMelee(ItemStack heldItem, LivingEntity entity) {
        return GunModifierHelper.getWeaponMode(new GunData(heldItem, entity)) == WeaponMode.MELEE;
    }
}
