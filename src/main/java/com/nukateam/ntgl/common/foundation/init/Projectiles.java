package com.nukateam.ntgl.common.foundation.init;

import com.nukateam.example.common.Raider;
import com.nukateam.ntgl.common.foundation.entity.*;
import com.nukateam.ntgl.Ntgl;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.level.Level;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

import java.util.function.BiFunction;

/**
 * Author: MrCrayfish
 */
public class Projectiles {
    public static final DeferredRegister<EntityType<?>> REGISTER = DeferredRegister.create(ForgeRegistries.ENTITY_TYPES, Ntgl.MOD_ID);

    public static final RegistryObject<EntityType<ProjectileEntity>> PROJECTILE = registerProjectile("ammo", ProjectileEntity::new);
    public static final RegistryObject<EntityType<LaserProjectile>> LASER_PROJECTILE = registerBasic("laser_projectile", LaserProjectile::new);
    public static final RegistryObject<EntityType<ContinuousLaserProjectile>> CONTINUOUS_LASER_PROJECTILE = registerBasic("continuous_laser_projectile", ContinuousLaserProjectile::new);
    public static final RegistryObject<EntityType<TeslaProjectile>> TESLA_PROJECTILE = registerBasic("tesla_projectile", TeslaProjectile::new);
    public static final RegistryObject<EntityType<FlameProjectile>> FLAME_PROJECTILE = registerBasic("flame_projectile", FlameProjectile::new);
    public static final RegistryObject<EntityType<GrenadeEntity>> GRENADE = registerBasic("grenade", GrenadeEntity::new);
    public static final RegistryObject<EntityType<MissileEntity>> MISSILE = registerBasic("missile", MissileEntity::new);
    public static final RegistryObject<EntityType<ThrowableGrenadeEntity>> THROWABLE_GRENADE = registerBasic("throwable_grenade", ThrowableGrenadeEntity::new);
    public static final RegistryObject<EntityType<StunGrenadeEntity>> THROWABLE_STUN_GRENADE = registerBasic("throwable_stun_grenade", StunGrenadeEntity::new);

//    public static final RegistryObject<EntityType<FlyingGibs>> FLYING_GIBS = register("flying_gibs", FlyingGibs::new);

    public static final RegistryObject<EntityType<FlyingGibs>> FLYING_GIBS =
            registerEntity("flying_gibs", EntityType.Builder
                    .<FlyingGibs>of(FlyingGibs::new, MobCategory.MISC)
                    .sized(0.5f, 0.5f));

    private static <T extends Entity> RegistryObject<EntityType<T>> registerEntity(String entityName, EntityType.Builder<T> builder) {
        return REGISTER.register(entityName, () -> builder.build(new ResourceLocation(Ntgl.MOD_ID, entityName).toString()));
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


    private static <T extends Entity> RegistryObject<EntityType<T>> register(String id, BiFunction<EntityType<T>, Level, T> function) {
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
     * @param id       the id of the ammo
     * @param function the factory to spawn the ammo for the server
     * @param <T>      an entity that is a ammo entity
     * @return A registry object containing the new entity type
     */
    private static <T extends ProjectileEntity> RegistryObject<EntityType<T>> registerProjectile(String id, BiFunction<EntityType<T>, Level, T> function) {
        return REGISTER.register(id, () -> EntityType.Builder.of(function::apply, MobCategory.MISC)
                .sized(0.25F, 0.25F)
                .setTrackingRange(0)
                .noSummon()
                .fireImmune()
                .setShouldReceiveVelocityUpdates(false)
                .setCustomClientFactory((spawnEntity, world) -> null)
                .build(id));
    }
}
