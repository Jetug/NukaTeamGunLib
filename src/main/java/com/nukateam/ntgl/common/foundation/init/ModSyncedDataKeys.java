package com.nukateam.ntgl.common.foundation.init;

import com.mrcrayfish.framework.api.sync.Serializers;
import com.mrcrayfish.framework.api.sync.SyncedClassKey;
import com.mrcrayfish.framework.api.sync.SyncedDataKey;
import com.nukateam.ntgl.Ntgl;
import com.mrcrayfish.framework.api.FrameworkAPI;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;

/**
 * Author: MrCrayfish
 */
public class ModSyncedDataKeys {

    public static final SyncedDataKey<LivingEntity, Boolean> AIMING = SyncedDataKey.builder(SyncedClassKey.LIVING_ENTITY, Serializers.BOOLEAN)
            .id(new ResourceLocation(Ntgl.MOD_ID, "aiming"))
            .defaultValueSupplier(() -> false)
            .resetOnDeath()
            .build();

    public static final SyncedDataKey<LivingEntity, Boolean> SHOOTING_RIGHT = SyncedDataKey.builder(SyncedClassKey.LIVING_ENTITY, Serializers.BOOLEAN)
            .id(new ResourceLocation(Ntgl.MOD_ID, "shooting_right"))
            .defaultValueSupplier(() -> false)
            .resetOnDeath()
            .build();

    public static final SyncedDataKey<LivingEntity, Boolean> RELOADING_RIGHT = SyncedDataKey.builder(SyncedClassKey.LIVING_ENTITY, Serializers.BOOLEAN)
            .id(new ResourceLocation(Ntgl.MOD_ID, "reloading_right"))
            .defaultValueSupplier(() -> false)
            .resetOnDeath()
            .build();

//    public static final SyncedDataKey<LivingEntity, Boolean> AIMING          = registerBooleanKey("aiming"         );
//    public static final SyncedDataKey<LivingEntity, Boolean> SHOOTING_RIGHT  = registerBooleanKey("shooting_right" );
    public static final SyncedDataKey<LivingEntity, Boolean> SHOOTING_LEFT   = registerBooleanKey("shooting_left"  );
//    public static final SyncedDataKey<LivingEntity, Boolean> RELOADING_RIGHT = registerBooleanKey("reloading_right");
    public static final SyncedDataKey<LivingEntity, Boolean> RELOADING_LEFT  = registerBooleanKey("reloading_left" );
    public static final SyncedDataKey<LivingEntity, Boolean> CHARGING_RIGHT  = registerBooleanKey("charging_right" );
    public static final SyncedDataKey<LivingEntity, Boolean> CHARGING_LEFT   = registerBooleanKey("charging_left"  );

    public static SyncedDataKey<LivingEntity, Boolean> getReloadKey(InteractionHand hand){
        return switch (hand){
            case MAIN_HAND -> RELOADING_RIGHT;
            case OFF_HAND -> RELOADING_LEFT;
        };
    }

    public static void register() {
        FrameworkAPI.registerSyncedDataKey(AIMING);
        FrameworkAPI.registerSyncedDataKey(SHOOTING_RIGHT);
        FrameworkAPI.registerSyncedDataKey(SHOOTING_LEFT);
        FrameworkAPI.registerSyncedDataKey(RELOADING_RIGHT);
        FrameworkAPI.registerSyncedDataKey(RELOADING_LEFT);
        FrameworkAPI.registerSyncedDataKey(CHARGING_RIGHT);
        FrameworkAPI.registerSyncedDataKey(CHARGING_LEFT);
    }

    private static SyncedDataKey<LivingEntity, Boolean> registerBooleanKey(String name) {
        return SyncedDataKey.builder(SyncedClassKey.LIVING_ENTITY, Serializers.BOOLEAN)
                .id(new ResourceLocation(Ntgl.MOD_ID, name))
                .defaultValueSupplier(() -> false)
                .resetOnDeath()
                .build();
    }
}
