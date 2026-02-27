package com.nukateam.ntgl.common.foundation.init;

import com.nukateam.ntgl.modules.data.DataKey;
import com.nukateam.ntgl.modules.data.DataKeyManager;
import net.minecraft.world.InteractionHand;

/**
 * Author: MrCrayfish
 */
public class ModSyncedDataKeys {
    public static final DataKey AIMING = registerDataKey("aiming");

    public static final DataKey SHOOTING_RIGHT = registerDataKey("shooting_right");
    public static final DataKey SHOOTING_LEFT  = registerDataKey("shooting_left"  );

    public static final DataKey RELOADING_RIGHT = registerDataKey("reloading_right");
    public static final DataKey RELOADING_LEFT  = registerDataKey("reloading_left" );

    public static final DataKey CHARGING_RIGHT  = registerDataKey("charging_right" );
    public static final DataKey CHARGING_LEFT   = registerDataKey("charging_left"  );

    public static final DataKey RELOAD_START = registerDataKey("reload_start"  );
    public static final DataKey RELOAD_END   = registerDataKey("reload_end"    );

    public static final DataKey EQUIP_RIGHT  = registerDataKey("equip_right" );
    public static final DataKey EQUIP_LEFT  = registerDataKey("equip_left" );

    public static final DataKey MELEE_RIGHT  = registerDataKey("melee_right" );
    public static final DataKey MELEE_LEFT   = registerDataKey("melee_left" );

    public static final DataKey PREPARE_RIGHT  = registerDataKey("prepare_right" );
    public static final DataKey PREPARE_LEFT   = registerDataKey("prepare_left" );

    public static final DataKey HOLD_RIGHT  = registerDataKey("hold_right" );
    public static final DataKey HOLD_LEFT   = registerDataKey("hold_left" );

    public static final DataKey THROW_RIGHT  = registerDataKey("throw_right" );
    public static final DataKey THROW_LEFT   = registerDataKey("throw_left" );

    public static DataKey getMeleeKey(InteractionHand hand){
        return getHandObject(hand, MELEE_RIGHT, MELEE_LEFT);
    }

    public static DataKey getEquipKey(InteractionHand hand){
        return getHandObject(hand, EQUIP_RIGHT, EQUIP_LEFT);
    }

    public static DataKey getReloadKey(InteractionHand hand){
        return getHandObject(hand, RELOADING_RIGHT, RELOADING_LEFT);
    }

    public static DataKey getPreparingDataKey(InteractionHand arm) {
        return getHandObject(arm, PREPARE_RIGHT, PREPARE_LEFT);
    }

    public static DataKey getHoldingDataKey(InteractionHand arm) {
        return getHandObject(arm, HOLD_RIGHT, HOLD_LEFT);
    }

    public static DataKey getThrowingDataKey(InteractionHand arm) {
        return getHandObject(arm, THROW_RIGHT, THROW_LEFT);
    }

    private static <T> T getHandObject(InteractionHand hand, T right, T left) {
        return hand == InteractionHand.MAIN_HAND ? right : left;
    }

    public static void register() {
        DataKeyManager.getInstance().registerKey(AIMING);
        DataKeyManager.getInstance().registerKey(SHOOTING_RIGHT);
        DataKeyManager.getInstance().registerKey(SHOOTING_LEFT);
        DataKeyManager.getInstance().registerKey(RELOADING_RIGHT);
        DataKeyManager.getInstance().registerKey(RELOADING_LEFT);
        DataKeyManager.getInstance().registerKey(CHARGING_RIGHT);
        DataKeyManager.getInstance().registerKey(CHARGING_LEFT);
        DataKeyManager.getInstance().registerKey(RELOAD_START);
        DataKeyManager.getInstance().registerKey(RELOAD_END);
        DataKeyManager.getInstance().registerKey(EQUIP_RIGHT);
        DataKeyManager.getInstance().registerKey(EQUIP_LEFT );
        DataKeyManager.getInstance().registerKey(MELEE_RIGHT);
        DataKeyManager.getInstance().registerKey(MELEE_LEFT );
        DataKeyManager.getInstance().registerKey(PREPARE_RIGHT);
        DataKeyManager.getInstance().registerKey(PREPARE_LEFT );
        DataKeyManager.getInstance().registerKey(THROW_RIGHT);
        DataKeyManager.getInstance().registerKey(THROW_LEFT );
        DataKeyManager.getInstance().registerKey(HOLD_RIGHT);
        DataKeyManager.getInstance().registerKey(HOLD_LEFT );
    }

//    private static SyncedDataKey<LivingEntity, Boolean> registerBooleanKey(String name) {
//        return SyncedDataKey.builder(SyncedClassKey.LIVING_ENTITY, Serializers.BOOLEAN)
//                .id(ResourceLocation.tryBuild(Ntgl.MOD_ID, name))
//                .defaultValueSupplier(() -> false)
//                .resetOnDeath()
//                .build();
//    }

    private static DataKey registerDataKey(String name) {
        return new DataKey(false);
    }
}
