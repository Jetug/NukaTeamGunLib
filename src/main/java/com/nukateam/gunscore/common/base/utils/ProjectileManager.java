package com.nukateam.gunscore.common.base.utils;

import com.nukateam.gunscore.common.base.gun.Gun;
import com.nukateam.gunscore.common.data.interfaces.IProjectileFactory;
import com.nukateam.gunscore.common.foundation.entity.ProjectileEntity;
import com.nukateam.gunscore.common.foundation.init.ModEntities;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;

import java.util.HashMap;
import java.util.Map;

/**
 * A class to manage custom projectile factories
 * <p>
 * Author: MrCrayfish
 */
public class ProjectileManager {
    private static ProjectileManager instance = null;

    public static ProjectileManager getInstance() {
        if (instance == null) {
            instance = new ProjectileManager();
        }
        return instance;
    }

    private final IProjectileFactory DEFAULT_FACTORY = (worldIn, entity, weapon, item, modifiedGun) ->
            new ProjectileEntity(ModEntities.PROJECTILE.get(), worldIn, entity, weapon, item, modifiedGun);

    private final Map<ResourceLocation, IProjectileFactory> projectileFactoryMap = new HashMap<>();

    /**
     * Registers a projectile factory for the given item. This allows for control over the entity
     * that is spawned when a weapon, that uses the given ammo, is fired.
     *
     * @param ammo    the ammo item
     * @param factory a custom projectile implementation
     */
    public void registerFactory(Item ammo, IProjectileFactory factory) {
        this.projectileFactoryMap.put(ammo.getRegistryName(), factory);
    }

    /**
     * Gets the projectile factory for the given resource location.
     *
     * @param id the resource id of the projectile factory (the id of the item)
     * @return the custom projectile factory or the default factory if nothing exists for the id
     */
    public IProjectileFactory getFactory(Gun.Projectile projectile) {
        return this.projectileFactoryMap.getOrDefault(projectile.getItem(), DEFAULT_FACTORY);
    }
}
