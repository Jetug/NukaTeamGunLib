package com.nukateam.ntgl.common.util.interfaces;

import com.nukateam.ntgl.common.data.WeaponData;
import com.nukateam.ntgl.common.util.managers.ProjectileManager;
import com.nukateam.ntgl.common.foundation.entity.ProjectileEntity;

import net.minecraft.world.level.Level;

/**
 * This class allows weapons to fire custom projectile instead of the default implementation.
 * <p>
 * Use {@link ProjectileManager} to register a factory.
 * <p>
 * Author: Jetug
 */
public interface IProjectileFactory {
    /**
     * Creates a new projectile entity.
     *
     * @param level the world the projectile is going to be spawned into
     * @param weaponData weapon related information
     * @return a projectile entity
     */
    ProjectileEntity create(Level level, WeaponData weaponData);
}
