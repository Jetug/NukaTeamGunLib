package com.nukateam.ntgl.common.util.interfaces;

import com.nukateam.ntgl.common.data.GunData;
import com.nukateam.ntgl.common.data.config.gun.Gun;
import com.nukateam.ntgl.common.util.managers.ProjectileManager;
import com.nukateam.ntgl.common.foundation.entity.ProjectileEntity;
import com.nukateam.ntgl.common.foundation.item.WeaponItem;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

/**
 * This class allows weapons to fire custom ammoData instead of the default implementation. The
 * grenade launcher uses this to spawn a grenade entity with custom physics. Use {@link ProjectileManager}
 * to register a factory.
 * <p>
 * Author: MrCrayfish
 */
public interface IProjectileFactory {
    /**
     * Creates a new projectile entity.
     *
     * @param worldIn     the world the projectile is going to be spawned into
     * @param gunData     weapon related information
     * @return a projectile entity
     */
    ProjectileEntity create(Level worldIn, GunData gunData);
}
