package com.nukateam.ntgl.common.base.utils;

import com.nukateam.ntgl.common.base.gun.Ammo;
import com.nukateam.ntgl.common.base.gun.Gun;
import com.nukateam.ntgl.common.data.interfaces.IProjectileFactory;
import com.nukateam.ntgl.common.foundation.entity.ProjectileEntity;
import com.nukateam.ntgl.common.foundation.init.Projectiles;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.Item;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.HashMap;
import java.util.Map;

/**
 * A class to manage custom projectile factories
 * <p>
 * Author: MrCrayfish
 */
public class ProjectileManager {
    private static ProjectileManager instance = null;
    private final IProjectileFactory DEFAULT_FACTORY = (worldIn, entity, weapon, item, modifiedGun) ->
            new ProjectileEntity(Projectiles.PROJECTILE.get(), worldIn, entity, weapon, item, modifiedGun);
    private final Map<ResourceLocation, IProjectileFactory> projectileFactoryMap = new HashMap();

    public ProjectileManager() {
    }

    public static ProjectileManager getInstance() {
        if (instance == null) {
            instance = new ProjectileManager();
        }

        return instance;
    }

    public void registerFactory(Item ammo, IProjectileFactory factory) {
        this.projectileFactoryMap.put(ForgeRegistries.ITEMS.getKey(ammo), factory);
    }

    public IProjectileFactory getFactory(ResourceLocation id) {
        return this.projectileFactoryMap.getOrDefault(id, this.DEFAULT_FACTORY);
    }
}
