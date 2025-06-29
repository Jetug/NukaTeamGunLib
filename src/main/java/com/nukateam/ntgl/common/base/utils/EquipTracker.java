package com.nukateam.ntgl.common.base.utils;

import com.mojang.datafixers.util.Pair;
import com.mrcrayfish.framework.api.sync.SyncedDataKey;
import com.nukateam.ntgl.Ntgl;
import com.nukateam.ntgl.common.foundation.init.ModSyncedDataKeys;
import com.nukateam.ntgl.common.foundation.item.GunItem;
import com.nukateam.ntgl.common.util.util.*;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.Map;
import java.util.WeakHashMap;

import static com.nukateam.ntgl.common.util.util.LivingEntityUtils.getInteractionHand;

@Mod.EventBusSubscriber(modid = Ntgl.MOD_ID)
public class EquipTracker {
    private static final Map<Pair<InteractionHand, Player>, EquipTracker> TRACKER_MAP = new WeakHashMap<>();

    private final InteractionHand arm;
    private final ItemStack stack;
    private final GunItem gunItem;
    private final int slot;

    public int equipTick;

    private EquipTracker(Player player, InteractionHand arm, boolean switchGuns) {
        this.arm = arm;
        this.stack = player.getItemInHand(arm);
        this.gunItem = ((GunItem) stack.getItem());
        this.slot = arm == InteractionHand.MAIN_HAND ? player.getInventory().selected : Inventory.SLOT_OFFHAND;

        var data = new GunData(stack, player);
        this.equipTick = GunModifierHelper.getEquipTime(data);
//        this.equipTick = 20;
    }

    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        try {
            if (event.phase == TickEvent.Phase.START && !event.player.level().isClientSide) {
                var player = event.player;
                handTick(player, InteractionHand.MAIN_HAND);
                handTick(player, InteractionHand.OFF_HAND);
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

    private static void handTick(Player entity, InteractionHand arm) {
        var key = new Pair<>(arm, entity);
        var tracker = TRACKER_MAP.get(key);
        if(tracker != null){
            if(tracker.equipTick > 0){
                tracker.equipTick--;
            }
            else stopEquip(entity, arm);
        }
        else stopEquip(entity, arm);
    }

    private static SyncedDataKey<LivingEntity, Boolean> getDataKey(InteractionHand arm) {
        var dataKey = arm == InteractionHand.MAIN_HAND ?
                ModSyncedDataKeys.EQUIP_RIGHT: ModSyncedDataKeys.EQUIP_LEFT;
        return dataKey;
    }

    public static boolean isEquiping(Player entity, InteractionHand arm){
        var isEquiping = getDataKey(arm).getValue(entity);
        return isEquiping;
//        var key = new Pair<>(arm, entity);
//        return TRACKER_MAP.containsKey(key) && TRACKER_MAP.get(key).equipTick > 0;
    }

    public static void startEquip(Player entity, InteractionHand arm){
        startEquip(entity, arm, false);
    }

    public static void startEquip(Player entity, InteractionHand arm, boolean switchGuns){
        var reloadKey = getDataKey(arm);
        reloadKey.setValue(entity,true);
        addTracker(entity, arm, switchGuns);
    }

    private static void stopEquip(Player entity, InteractionHand arm) {
        var reloadKey = getDataKey(arm);
        reloadKey.setValue(entity, false);
        TRACKER_MAP.remove(new Pair<>(arm, entity));
    }

    private static boolean addTracker(Player entity, InteractionHand arm, boolean switchGuns) {
        var reloadKey = getDataKey(arm);

        var gunItem = entity.getItemInHand(arm).getItem();

        var key = new Pair<>(arm, entity);

        if (!TRACKER_MAP.containsKey(key)) {
            if (!(gunItem instanceof GunItem)) {
                reloadKey.setValue(entity, false);
                return true;
            }
            TRACKER_MAP.put(key, new EquipTracker(entity, arm, switchGuns));
        }
        return false;
    }


}
