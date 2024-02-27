package com.nukateam.gunscore.common.foundation.init;

import com.nukateam.gunscore.GunMod;
import com.mrcrayfish.framework.api.FrameworkAPI;
import com.mrcrayfish.framework.api.data.sync.Serializers;
import com.mrcrayfish.framework.api.data.sync.SyncedClassKey;
import com.mrcrayfish.framework.api.data.sync.SyncedDataKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;

/**
 * Author: MrCrayfish
 */
public class ModSyncedDataKeys {
    public static final SyncedDataKey<LivingEntity, Boolean> AIMING = SyncedDataKey.builder(SyncedClassKey.LIVING_ENTITY, Serializers.BOOLEAN)
            .id(new ResourceLocation(GunMod.MOD_ID, "aiming"))
            .defaultValueSupplier(() -> false)
            .resetOnDeath()
            .build();

    public static final SyncedDataKey<LivingEntity, Boolean> SHOOTING_RIGHT = SyncedDataKey.builder(SyncedClassKey.LIVING_ENTITY, Serializers.BOOLEAN)
            .id(new ResourceLocation(GunMod.MOD_ID, "shooting_right"))
            .defaultValueSupplier(() -> false)
            .resetOnDeath()
            .build();

    public static final SyncedDataKey<LivingEntity, Boolean> SHOOTING_LEFT = SyncedDataKey.builder(SyncedClassKey.LIVING_ENTITY, Serializers.BOOLEAN)
            .id(new ResourceLocation(GunMod.MOD_ID, "shooting_left"))
            .defaultValueSupplier(() -> false)
            .resetOnDeath()
            .build();

    public static final SyncedDataKey<LivingEntity, Boolean> RELOADING_RIGHT = SyncedDataKey.builder(SyncedClassKey.LIVING_ENTITY, Serializers.BOOLEAN)
            .id(new ResourceLocation(GunMod.MOD_ID, "reloading_right"))
            .defaultValueSupplier(() -> false)
            .resetOnDeath()
            .build();

    public static final SyncedDataKey<LivingEntity, Boolean> RELOADING_LEFT = SyncedDataKey.builder(SyncedClassKey.LIVING_ENTITY, Serializers.BOOLEAN)
            .id(new ResourceLocation(GunMod.MOD_ID, "reloading_left"))
            .defaultValueSupplier(() -> false)
            .resetOnDeath()
            .build();

    public static void register() {
        FrameworkAPI.registerSyncedDataKey(AIMING);
        FrameworkAPI.registerSyncedDataKey(SHOOTING_RIGHT);
        FrameworkAPI.registerSyncedDataKey(SHOOTING_LEFT);
        FrameworkAPI.registerSyncedDataKey(RELOADING_RIGHT);
        FrameworkAPI.registerSyncedDataKey(RELOADING_LEFT);
    }
}
