package com.nukateam.chassis_core.modules.example.common.registery;

import com.nukateam.chassis_core.ChassisCore;
import com.nukateam.chassis_core.modules.example.common.entities.ExampleChassis;
import com.nukateam.example.common.entities.Brahmin;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.minecraft.core.registries.Registries;
import net.neoforged.neoforge.registries.DeferredHolder;

public class EntityTypes {
    public static final DeferredRegister<EntityType<?>> ENTITY_TYPES
            = DeferredRegister.create(Registries.ENTITY_TYPE, ChassisCore.MOD_ID);

    public static final DeferredHolder<EntityType<?>, EntityType<ExampleChassis>> EXAMPLE_CHASSIS =
            registerEntity("example_chassis", EntityType.Builder
                    .of(ExampleChassis::new, MobCategory.MISC)
                    .sized(1.0f, 2.3f));


    private static <T extends Entity> DeferredHolder<EntityType<?>, EntityType<T>> registerEntity(String entityName, EntityType.Builder<T> builder) {
        return ENTITY_TYPES.register(entityName, () -> builder.build(ResourceLocation.tryBuild(ChassisCore.MOD_ID, entityName).toString()));
    }

    public static void register(IEventBus eventBus) {
        ENTITY_TYPES.register(eventBus);
    }
}
