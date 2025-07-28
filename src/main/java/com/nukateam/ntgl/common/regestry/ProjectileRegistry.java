package com.nukateam.ntgl.common.regestry;

import com.nukateam.example.common.registery.ModGuns;
import com.nukateam.ntgl.common.base.holders.ProjectileType;
import com.nukateam.ntgl.common.base.utils.ProjectileManager;
import com.nukateam.ntgl.common.foundation.entity.*;
import com.nukateam.ntgl.common.foundation.init.Projectiles;
import com.nukateam.ntgl.common.util.interfaces.IProjectileFactory;
import org.jetbrains.annotations.NotNull;

public class ProjectileRegistry {
    private static final IProjectileFactory DEFAULT = (level, entity, weapon, item, modifiedGun) ->
            new ProjectileEntity(Projectiles.PROJECTILE.get(), level, entity, weapon, item, modifiedGun);

    private static final IProjectileFactory GRENADE = (level, entity, weapon, item, modifiedGun) ->
            new GrenadeEntity(Projectiles.GRENADE.get(), level, entity, weapon, item, modifiedGun);

    private static final IProjectileFactory MISSILE           = (level, entity, weapon, item, modifiedGun) ->
            new MissileEntity(Projectiles.MISSILE.get(), level, entity, weapon, item, modifiedGun);

    private static final IProjectileFactory LASER             = (level, entity, weapon, item, modifiedGun) ->
            new LaserProjectile(Projectiles.LASER_PROJECTILE.get(), level, entity, weapon, item, modifiedGun);

    private static final IProjectileFactory TESLA             = (level, entity, weapon, item, modifiedGun) ->
            new TeslaProjectile(Projectiles.TESLA_PROJECTILE.get(), level, entity, weapon, item, modifiedGun);

    private static final IProjectileFactory FIRE              = (level, entity, weapon, item, modifiedGun) ->
            new FlameProjectile(Projectiles.FLAME_PROJECTILE.get(), level, entity, weapon, item, modifiedGun);

    private static final IProjectileFactory CONTINUOUS_LASER  = (level, entity, weapon, item, modifiedGun) ->
            new ContinuousLaserProjectile(Projectiles.CONTINUOUS_LASER_PROJECTILE.get(), level, entity, weapon, item, modifiedGun);

    public static void registerProjectiles() {
        ProjectileManager.getInstance().registerFactory(ProjectileType.BULLET, DEFAULT);
        ProjectileManager.getInstance().registerFactory(ProjectileType.GRENADE  , GRENADE);
        ProjectileManager.getInstance().registerFactory(ProjectileType.MISSILE  , MISSILE);
        ProjectileManager.getInstance().registerFactory(ProjectileType.LASER    , LASER);
        ProjectileManager.getInstance().registerFactory(ProjectileType.TESLA    , TESLA);
        ProjectileManager.getInstance().registerFactory(ProjectileType.CONTINUOUS_LASER, CONTINUOUS_LASER);

        ProjectileManager.getInstance().registerFactory(ModGuns.GRENADE.get()   , GRENADE);
        ProjectileManager.getInstance().registerFactory(ModGuns.ROUND10MM.get() , LASER);
        ProjectileManager.getInstance().registerFactory(ModGuns.ROUND5MM.get() , LASER);
        ProjectileManager.getInstance().registerFactory(ModGuns.ROUND45.get()   , TESLA);
        ProjectileManager.getInstance().registerFactory(ModGuns.ROUND38.get()   , CONTINUOUS_LASER);
        ProjectileManager.getInstance().registerFactory(ModGuns.FUEL.get(), FIRE);
    }

    private static @NotNull IProjectileFactory registerDefault() {
        return (level, entity, weapon, item, modifiedGun) ->
                new FlameProjectile(Projectiles.FLAME_PROJECTILE.get(), level, entity, weapon, item, modifiedGun);
    }
}
