
package com.nukateam.ntgl.common.base.utils;

import com.mrcrayfish.framework.api.sync.SyncedDataKey;
import com.nukateam.ntgl.Ntgl;
import com.nukateam.ntgl.common.base.gun.Ammo;
import com.nukateam.ntgl.common.base.gun.Gun;
import com.nukateam.ntgl.common.foundation.init.ModSyncedDataKeys;
import com.nukateam.ntgl.common.foundation.item.GunItem;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import static com.nukateam.ntgl.common.data.util.LivingEntityUtils.getInteractionHand;

@Mod.EventBusSubscriber(modid = Ntgl.MOD_ID)
public class EntityReloadTracker {
    private final HumanoidArm arm;
    private final ItemStack stack;
    private final GunItem gunItem;
    private final Gun gun;
    private int reloadTick;

    private static final Map<LivingEntity, EntityReloadTracker> RELOAD_TRACKER_MAP = new HashMap<>();
    private static final ArrayList<LivingEntity> FOR_REMOVE = new ArrayList<>();

    private EntityReloadTracker(LivingEntity entity, HumanoidArm arm) {
        this.arm = arm;
        this.stack = entity.getItemInHand(getInteractionHand(arm));
        this.gunItem = ((GunItem) stack.getItem());
        this.gun = gunItem.getModifiedGun(stack);
        this.reloadTick = gun.getGeneral().getReloadTime();
    }

    public static boolean isReloading(LivingEntity entity){
        return RELOAD_TRACKER_MAP.containsKey(entity);
    }

    public static void addTracker(LivingEntity entity, HumanoidArm arm){
        setReloading(entity, arm, true);
        RELOAD_TRACKER_MAP.put(entity, new EntityReloadTracker(entity, arm));
    }

    @SubscribeEvent
    public static void onServerTick(TickEvent.ServerTickEvent event) {
        try {
            if (event.phase == TickEvent.Phase.START) {
                for (var entry: RELOAD_TRACKER_MAP.entrySet()) {
                    var tracker = entry.getValue();
                    var entity = entry.getKey();
                    onTick(entity, tracker);
                }
                removeTrackers();
            }
        }
        catch (Exception e){
            Ntgl.LOGGER.error(e.getMessage(), e);
        }
    }

    private static void removeTrackers() {
        for (var removed: FOR_REMOVE) {
            RELOAD_TRACKER_MAP.remove(removed);
        }
        FOR_REMOVE.clear();
    }


    private static void onTick(LivingEntity entity, EntityReloadTracker tracker){
        if (tracker.reloadTick > 0) {
            tracker.reloadTick = Math.max(tracker.reloadTick - 1, 0);
        }
        else{
            Gun.fillAmmo(tracker.stack);
            setReloading(entity, tracker.arm, false);
//            RELOAD_TRACKER_MAP.remove(entity);
            FOR_REMOVE.add(entity);
        }
    }

    private static SyncedDataKey<LivingEntity, Boolean> getReloadSataKey(HumanoidArm arm){
        return arm == HumanoidArm.RIGHT ?
                ModSyncedDataKeys.RELOADING_RIGHT :
                ModSyncedDataKeys.RELOADING_LEFT;
    }

    private static void setReloading(LivingEntity entity, HumanoidArm arm, boolean value) {
        getReloadSataKey(arm).setValue(entity, value);
    }
}
