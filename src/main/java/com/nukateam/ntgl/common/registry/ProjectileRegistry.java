package com.nukateam.ntgl.common.registry;

import com.nukateam.example.common.registery.ModGuns;
import com.nukateam.ntgl.common.data.holders.ProjectileType;
import com.nukateam.ntgl.common.foundation.entity.throwable.ThrowableGrenadeEntity;
import com.nukateam.ntgl.common.util.managers.ProjectileManager;
import com.nukateam.ntgl.common.foundation.entity.*;
import com.nukateam.ntgl.common.foundation.init.Projectiles;
import com.nukateam.ntgl.common.util.interfaces.IProjectileFactory;
import com.nukateam.ntgl.common.util.interfaces.IThrowableProjectileFactory;
import org.jetbrains.annotations.NotNull;

public class ProjectileRegistry {
    private static final IProjectileFactory DEFAULT = (level, gunData) ->
            new ProjectileEntity(Projectiles.PROJECTILE.get(), level, gunData);

    private static final IProjectileFactory GRENADE = (level, gunData) ->
            new GrenadeEntity(Projectiles.GRENADE.get(), level, gunData);

    private static final IProjectileFactory MISSILE           = (level, gunData) ->
            new MissileEntity(Projectiles.MISSILE.get(), level, gunData);

    private static final IProjectileFactory LASER             = (level, gunData) ->
            new LaserProjectile(Projectiles.LASER_PROJECTILE.get(), level, gunData);

    private static final IProjectileFactory TESLA             = (level, gunData) ->
            new TeslaProjectile(Projectiles.TESLA_PROJECTILE.get(), level, gunData);

    private static final IProjectileFactory FIRE              = (level, gunData) ->
            new FlameProjectile(Projectiles.FLAME_PROJECTILE.get(), level, gunData);

    private static final IProjectileFactory CONTINUOUS_LASER  = (level, gunData) ->
            new ContinuousLaserProjectile(Projectiles.CONTINUOUS_LASER_PROJECTILE.get(), level, gunData);

    private static final IThrowableProjectileFactory THROWABLE_GRENADE = ThrowableGrenadeEntity::new;
    private static final IThrowableProjectileFactory THROWABLE_STUN_GRENADE = StunGrenadeEntity::new;

    public static void registerProjectiles() {
        ProjectileManager.getInstance().registerFactory(ProjectileType.BULLET   , DEFAULT);
        ProjectileManager.getInstance().registerFactory(ProjectileType.GRENADE  , GRENADE);
        ProjectileManager.getInstance().registerFactory(ProjectileType.MISSILE  , MISSILE);
        ProjectileManager.getInstance().registerFactory(ProjectileType.LASER    , LASER);
        ProjectileManager.getInstance().registerFactory(ProjectileType.TESLA    , TESLA);
        ProjectileManager.getInstance().registerFactory(ProjectileType.FIRE    , FIRE);
        ProjectileManager.getInstance().registerFactory(ProjectileType.CONTINUOUS_LASER, CONTINUOUS_LASER);

        ProjectileManager.getInstance().registerFactory(ModGuns.GRENADE.get()   , GRENADE);
        ProjectileManager.getInstance().registerFactory(ModGuns.ROUND10MM.get() , LASER);
        ProjectileManager.getInstance().registerFactory(ModGuns.ROUND5MM.get()  , LASER);
        ProjectileManager.getInstance().registerFactory(ModGuns.ROUND45.get()   , TESLA);
        ProjectileManager.getInstance().registerFactory(ModGuns.ROUND38.get()   , CONTINUOUS_LASER);
        ProjectileManager.getInstance().registerFactory(ModGuns.FUEL.get(), FIRE);

        ProjectileManager.getInstance().registerFactory(ProjectileType.GRENADE      , THROWABLE_GRENADE);
        ProjectileManager.getInstance().registerFactory(ProjectileType.STUN_GRENADE , THROWABLE_STUN_GRENADE);

    }

    private static @NotNull IProjectileFactory registerDefault() {
        return (level, gunData) ->
                new FlameProjectile(Projectiles.FLAME_PROJECTILE.get(), level, gunData);
    }
}
