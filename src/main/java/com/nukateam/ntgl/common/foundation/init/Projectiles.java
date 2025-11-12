package com.nukateam.ntgl.common.foundation.init;

import com.nukateam.ntgl.common.foundation.entity.*;
import com.nukateam.ntgl.Ntgl;
import com.nukateam.ntgl.common.foundation.entity.throwable.ThrowableGrenadeEntity;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.BiFunction;

public class Projectiles {
    public static final DeferredRegister<EntityType<?>> REGISTER = DeferredRegister.create(Registries.ENTITY_TYPE, Ntgl.MOD_ID);

    public static final DeferredHolder<EntityType<?>, EntityType<ProjectileEntity>> PROJECTILE = registerProjectile("projectile", ProjectileEntity::new);
    public static final DeferredHolder<EntityType<?>, EntityType<LaserProjectile>> LASER_PROJECTILE = registerBasic("laser_projectile", LaserProjectile::new);
    public static final DeferredHolder<EntityType<?>, EntityType<ContinuousLaserProjectile>> CONTINUOUS_LASER_PROJECTILE = registerBasic("continuous_laser_projectile", ContinuousLaserProjectile::new);
    public static final DeferredHolder<EntityType<?>, EntityType<TeslaProjectile>> TESLA_PROJECTILE = registerBasic("tesla_projectile", TeslaProjectile::new);
    public static final DeferredHolder<EntityType<?>, EntityType<FlameProjectile>> FLAME_PROJECTILE = registerBasic("flame_projectile", FlameProjectile::new);
    public static final DeferredHolder<EntityType<?>, EntityType<GrenadeEntity>> GRENADE = registerBasic("grenade", GrenadeEntity::new);
    public static final DeferredHolder<EntityType<?>, EntityType<MissileEntity>> MISSILE = registerBasic("missile", MissileEntity::new);
//    public static final RegistryObject<EntityType<ThrowableEntity>> THROWABLE = registerBasic("throwable", ThrowableEntity::new);
    public static final DeferredHolder<EntityType<?>, EntityType<ThrowableGrenadeEntity>> THROWABLE_GRENADE = registerBasic("throwable_grenade", ThrowableGrenadeEntity::new);
    public static final DeferredHolder<EntityType<?>, EntityType<StunGrenadeEntity>> THROWABLE_STUN_GRENADE = registerBasic("throwable_stun_grenade", StunGrenadeEntity::new);

//    public static final RegistryObject<EntityType<FlyingGib>> FLYING_GIBS = register("flying_gibs", FlyingGib::new);

    private static <T extends Entity> DeferredHolder<EntityType<?>, EntityType<T>> registerEntity(String entityName, EntityType.Builder<T> builder) {
        return REGISTER.register(entityName, () -> builder.build(ResourceLocation.tryBuild(Ntgl.MOD_ID, entityName).toString()));
    }

    private static <T extends Entity> DeferredHolder<EntityType<?>, EntityType<T>> registerBasic(String id, BiFunction<EntityType<T>, Level, T> function) {
        return REGISTER.register(id, () -> EntityType.Builder.of(function::apply, MobCategory.MISC)
                .sized(0.25F, 0.25F)
                .setTrackingRange(100)
                .setUpdateInterval(1)
                .noSummon()
                .fireImmune()
                .setShouldReceiveVelocityUpdates(true).build(id));
    }


    private static <T extends Entity> DeferredHolder<EntityType<?>, EntityType<T>> register(String id, BiFunction<EntityType<T>, Level, T> function) {
        return REGISTER.register(id, () -> EntityType.Builder.of(function::apply, MobCategory.MISC)
                .sized(1.25F, 1.25F)
                .build(id));
    }

    /**
     * Entity registration that prevents the entity from being sent and tracked by clients. Projectiles
     * are rendered separately from Minecraft's entity rendering system and their logic is handled
     * exclusively by the server, why send them to the client. Projectiles also have very short time
     * in the world and are spawned many times a tick. There is no reason to send unnecessary packets
     * when it can be avoided to drastically improve the performance of the game.
     *
     * @param id       the id of the projectile
     * @param function the factory to spawn the projectile for the server
     * @param <T>      an entity that is a projectile entity
     * @return A registry object containing the new entity type
     */
    private static <T extends ProjectileEntity> DeferredHolder<EntityType<?>, EntityType<T>> registerProjectile(String id, BiFunction<EntityType<T>, Level, T> function) {
        return REGISTER.register(id, () -> EntityType.Builder.of(function::apply, MobCategory.MISC)
                .sized(0.25F, 0.25F)
                .setTrackingRange(0)
                .noSummon()
                .fireImmune()
                .setShouldReceiveVelocityUpdates(false)
                .setCustomClientFactory((spawnEntity, world) -> null)
                .build(id));
    }

    private static <T extends Entity> RegistryObject<EntityType<T>> registerBasic(String id, BiFunction<EntityType<T>, Level, T> function) {
        return REGISTER.register(id, () -> EntityType.Builder.of(function::apply, MobCategory.MISC)
                .sized(0.25F, 0.25F)
                .setTrackingRange(100)
                .setUpdateInterval(1)
                .noSummon()
                .fireImmune()
                .setShouldReceiveVelocityUpdates(true).build(id));
    }
}
