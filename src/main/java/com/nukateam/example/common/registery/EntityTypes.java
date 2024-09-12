package com.nukateam.example.common.registery;

import com.nukateam.example.common.Raider;
import com.nukateam.ntgl.Ntgl;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

import static net.minecraft.world.entity.EntityType.Builder;

public class EntityTypes {
    public static final DeferredRegister<EntityType<?>> ENTITY_TYPES
            = DeferredRegister.create(ForgeRegistries.ENTITY_TYPES, Ntgl.MOD_ID);

    public static final RegistryObject<EntityType<Raider>> RAIDER =
            registerEntity("raider", Builder
                    .of(Raider::new, MobCategory.CREATURE)
                    .sized(1.0f, 1.9f));

    private static <T extends Entity> RegistryObject<EntityType<T>> registerEntity(String entityName, Builder<T> builder) {
        Ntgl.LOGGER.debug(entityName + "ENTITY REGISTERED");
        return ENTITY_TYPES.register(entityName, () -> builder.build(new ResourceLocation(Ntgl.MOD_ID, entityName).toString()));
    }

    public static void register(IEventBus eventBus) {
        ENTITY_TYPES.register(eventBus);
    }
}
