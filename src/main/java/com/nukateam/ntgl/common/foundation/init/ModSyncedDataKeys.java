package com.nukateam.ntgl.common.foundation.init;

import com.mrcrayfish.framework.api.sync.Serializers;
import com.mrcrayfish.framework.api.sync.SyncedClassKey;
import com.mrcrayfish.framework.api.sync.SyncedDataKey;
import com.nukateam.ntgl.Ntgl;
import com.mrcrayfish.framework.api.FrameworkAPI;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;

/**
 * Author: MrCrayfish
 */
public class ModSyncedDataKeys {
    public static final SyncedDataKey<LivingEntity, Boolean> AIMING
            = SyncedDataKey.builder(SyncedClassKey.LIVING_ENTITY, Serializers.BOOLEAN)
            .id(ResourceLocation.tryBuild(Ntgl.MOD_ID, "aiming"))
            .defaultValueSupplier(() -> false)
            .resetOnDeath()
            .build();

    public static final SyncedDataKey<LivingEntity, Boolean> SHOOTING_RIGHT
            = SyncedDataKey.builder(SyncedClassKey.LIVING_ENTITY, Serializers.BOOLEAN)
            .id(ResourceLocation.tryBuild(Ntgl.MOD_ID, "shooting_right"))
            .defaultValueSupplier(() -> false)
            .resetOnDeath()
            .build();

    public static final SyncedDataKey<LivingEntity, Boolean> RELOADING_RIGHT
            = SyncedDataKey.builder(SyncedClassKey.LIVING_ENTITY, Serializers.BOOLEAN)
            .id(ResourceLocation.tryBuild(Ntgl.MOD_ID, "reloading_right"))
            .defaultValueSupplier(() -> false)
            .resetOnDeath()
            .build();

    public static final SyncedDataKey<LivingEntity, Boolean> SHOOTING_LEFT   = registerBooleanKey("shooting_left"  );
    public static final SyncedDataKey<LivingEntity, Boolean> RELOADING_LEFT  = registerBooleanKey("reloading_left" );
    public static final SyncedDataKey<LivingEntity, Boolean> CHARGING_RIGHT  = registerBooleanKey("charging_right" );
    public static final SyncedDataKey<LivingEntity, Boolean> CHARGING_LEFT   = registerBooleanKey("charging_left"  );

    public static final SyncedDataKey<LivingEntity, Boolean> RELOAD_START = registerBooleanKey("reload_start"  );
    public static final SyncedDataKey<LivingEntity, Boolean> RELOAD_END   = registerBooleanKey("reload_end"    );

    public static final SyncedDataKey<LivingEntity, Boolean> EQUIP_RIGHT  = registerBooleanKey("equip_right" );
    public static final SyncedDataKey<LivingEntity, Boolean> EQUIP_LEFT  = registerBooleanKey("equip_left" );

    public static final SyncedDataKey<LivingEntity, Boolean> MELEE_RIGHT  = registerBooleanKey("melee_right" );
    public static final SyncedDataKey<LivingEntity, Boolean> MELEE_LEFT   = registerBooleanKey("melee_left" );

    public static final SyncedDataKey<LivingEntity, Boolean> PREPARE_RIGHT  = registerBooleanKey("prepare_right" );
    public static final SyncedDataKey<LivingEntity, Boolean> PREPARE_LEFT   = registerBooleanKey("prepare_left" );

    public static final SyncedDataKey<LivingEntity, Boolean> THROW_RIGHT  = registerBooleanKey("throw_right" );
    public static final SyncedDataKey<LivingEntity, Boolean> THROW_LEFT   = registerBooleanKey("throw_left" );

    public static SyncedDataKey<LivingEntity, Boolean> getDoMelee(InteractionHand hand){
        return getHandObject(hand, MELEE_RIGHT, MELEE_LEFT);
    }

    public static SyncedDataKey<LivingEntity, Boolean> getReloadKey(InteractionHand hand){
        return getHandObject(hand, RELOADING_RIGHT, RELOADING_LEFT);
    }

    public static SyncedDataKey<LivingEntity, Boolean> getPreparingDataKey(InteractionHand arm) {
        return getHandObject(arm, PREPARE_RIGHT, PREPARE_LEFT);
    }

    public static SyncedDataKey<LivingEntity, Boolean> getThrowingDataKey(InteractionHand arm) {
        return getHandObject(arm, THROW_RIGHT, THROW_LEFT);
    }

    private static <T> T getHandObject(InteractionHand hand, T right, T left) {
        return hand == InteractionHand.MAIN_HAND ? right : left;
    }


    public static void register() {
        FrameworkAPI.registerSyncedDataKey(AIMING);
        FrameworkAPI.registerSyncedDataKey(SHOOTING_RIGHT);
        FrameworkAPI.registerSyncedDataKey(SHOOTING_LEFT);
        FrameworkAPI.registerSyncedDataKey(RELOADING_RIGHT);
        FrameworkAPI.registerSyncedDataKey(RELOADING_LEFT);
        FrameworkAPI.registerSyncedDataKey(CHARGING_RIGHT);
        FrameworkAPI.registerSyncedDataKey(CHARGING_LEFT);
        FrameworkAPI.registerSyncedDataKey(RELOAD_START);
        FrameworkAPI.registerSyncedDataKey(RELOAD_END);
        FrameworkAPI.registerSyncedDataKey(EQUIP_RIGHT);
        FrameworkAPI.registerSyncedDataKey(EQUIP_LEFT );
        FrameworkAPI.registerSyncedDataKey(MELEE_RIGHT);
        FrameworkAPI.registerSyncedDataKey(MELEE_LEFT );
        FrameworkAPI.registerSyncedDataKey(PREPARE_RIGHT);
        FrameworkAPI.registerSyncedDataKey(PREPARE_LEFT );
        FrameworkAPI.registerSyncedDataKey(THROW_RIGHT);
        FrameworkAPI.registerSyncedDataKey(THROW_LEFT );
    }

    private static SyncedDataKey<LivingEntity, Boolean> registerBooleanKey(String name) {
        return SyncedDataKey.builder(SyncedClassKey.LIVING_ENTITY, Serializers.BOOLEAN)
                .id(ResourceLocation.tryBuild(Ntgl.MOD_ID, name))
                .defaultValueSupplier(() -> false)
                .resetOnDeath()
                .build();
    }
}
