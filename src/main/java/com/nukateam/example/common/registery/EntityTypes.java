package com.nukateam.example.common.registery;

import com.nukateam.example.common.entities.*;
import com.nukateam.ntgl.Ntgl;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.*;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.*;

import static net.minecraft.world.entity.EntityType.Builder;

public class EntityTypes {
    public static final DeferredRegister<EntityType<?>> ENTITY_TYPES
            = DeferredRegister.create(ForgeRegistries.ENTITY_TYPES, Ntgl.MOD_ID);

    public static final RegistryObject<EntityType<Raider>> RAIDER =
            registerEntity("raider", Builder
                    .of(Raider::new, MobCategory.CREATURE)
                    .sized(1.0f, 1.9f));

    public static final RegistryObject<EntityType<Deathclaw>> DEATHCLAW =
            registerEntity("deathclaw", Builder
                    .of(Deathclaw::new, MobCategory.MONSTER)
                    .sized(1.5f, 3f));

    public static final RegistryObject<EntityType<Brahmin>> BRAHMIN =
            registerEntity("brahmin", Builder
                    .of(Brahmin::new, MobCategory.CREATURE)
                    .sized(1.5f, 1.5f));

    private static <T extends Entity> RegistryObject<EntityType<T>> registerEntity(String entityName, Builder<T> builder) {
        return ENTITY_TYPES.register(entityName, () -> builder.build(new ResourceLocation(Ntgl.MOD_ID, entityName).toString()));
    }

    public static void register(IEventBus eventBus) {
        ENTITY_TYPES.register(eventBus);
    }
}
