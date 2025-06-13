package com.nukateam.ntgl.common.base.utils;

import com.nukateam.ntgl.common.base.holders.ProjectileType;
import com.nukateam.ntgl.common.util.interfaces.IProjectileFactory;
import com.nukateam.ntgl.common.foundation.entity.ProjectileEntity;
import com.nukateam.ntgl.common.foundation.init.Projectiles;
import com.nukateam.ntgl.common.util.util.GunData;
import com.nukateam.ntgl.common.util.util.GunStateHelper;
import net.minecraft.resources.ResourceLocation;
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

    private final Map<ResourceLocation, IProjectileFactory> projectileForAmmoFactories = new HashMap();
    private final Map<ResourceLocation, IProjectileFactory> projectileForTypeFactories = new HashMap();

    public ProjectileManager() {}

    public static ProjectileManager getInstance() {
        if (instance == null) {
            instance = new ProjectileManager();
        }

        return instance;
    }

    public void registerFactory(ProjectileType ammo, IProjectileFactory factory) {
        this.projectileForTypeFactories.put(ammo.getId(), factory);
    }

    public void registerFactory(Item ammo, IProjectileFactory factory) {
        this.projectileForAmmoFactories.put(ForgeRegistries.ITEMS.getKey(ammo), factory);
    }

    public IProjectileFactory getFactory(GunData data) {
        var item = GunStateHelper.getAmmoId(data);
        var projectileType = GunStateHelper.getAmmoConfig(data).getProjectile();
        var factory = projectileForAmmoFactories.get(item);

        if(projectileForAmmoFactories.containsKey(item)){
            return projectileForAmmoFactories.get(item);
        }
        factory = projectileForTypeFactories.get(projectileType.getId());
        if(factory != null){
            return factory;
        }
        return DEFAULT_FACTORY;
    }
}
