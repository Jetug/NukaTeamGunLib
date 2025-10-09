package com.nukateam.ntgl.common.foundation.init;

import com.nukateam.ntgl.Ntgl;
import com.nukateam.ntgl.common.foundation.entity.*;
import com.nukateam.ntgl.common.foundation.entity.misc.AshPile;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

/**
 * Author: MrCrayfish
 */
public class ModEntityTypes {
    public static final DeferredRegister<EntityType<?>> ENTITY_TYPES
            = DeferredRegister.create(Registries.ENTITY_TYPE, Ntgl.MOD_ID);

    public static final DeferredHolder<EntityType<?>, EntityType<AshPile>> ASH_PILE =
            registerEntity("ash_pile", EntityType.Builder
                    .<AshPile>of(AshPile::new, MobCategory.MISC)
                    .sized(0.8f, 0.2f));

    public static final DeferredHolder<EntityType<?>, EntityType<FlyingGib>> FLYING_GIBS =
            registerEntity("flying_gibs", EntityType.Builder
                    .<FlyingGib>of(FlyingGib::new, MobCategory.MISC)
                    .sized(0.5f, 0.5f));

    private static <T extends Entity> DeferredHolder<EntityType<?>, EntityType<T>> registerEntity(String entityName, EntityType.Builder<T> builder) {
        return ENTITY_TYPES.register(entityName, () -> builder.build(ResourceLocation.tryBuild(Ntgl.MOD_ID, entityName).toString()));
    }

    public static void register(IEventBus eventBus) {
        ENTITY_TYPES.register(eventBus);
    }
}
