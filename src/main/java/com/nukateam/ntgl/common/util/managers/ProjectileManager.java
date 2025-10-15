package com.nukateam.ntgl.common.util.managers;

import com.nukateam.ntgl.common.data.WeaponData;
import com.nukateam.ntgl.common.data.holders.ProjectileType;
import com.nukateam.ntgl.common.foundation.entity.throwable.ThrowableGrenadeEntity;
import com.nukateam.ntgl.common.util.interfaces.IProjectileFactory;
import com.nukateam.ntgl.common.foundation.entity.ProjectileEntity;
import com.nukateam.ntgl.common.foundation.init.Projectiles;
import com.nukateam.ntgl.common.util.interfaces.IThrowableProjectileFactory;
import com.nukateam.ntgl.common.util.util.WeaponStateHelper;
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
    private final IProjectileFactory DEFAULT_FACTORY = (level,gunData) ->
            new ProjectileEntity(Projectiles.PROJECTILE.get(), level, gunData);
    
    private final IThrowableProjectileFactory DEFAULT_THROWABLE_FACTORY = ThrowableGrenadeEntity::new;

    private final Map<ResourceLocation, IProjectileFactory> projectileForAmmoFactories = new HashMap();
    private final Map<ResourceLocation, IProjectileFactory> projectileForTypeFactories = new HashMap();

    private final Map<ResourceLocation, IThrowableProjectileFactory> throwableProjectileFactories = new HashMap();

    public ProjectileManager() {}

    public static ProjectileManager getInstance() {
        if (instance == null) {
            instance = new ProjectileManager();
        }

        return instance;
    }

    public void registerFactory(ProjectileType ammo, IThrowableProjectileFactory factory) {
        this.throwableProjectileFactories.put(ammo.getId(), factory);
    }

    public void registerFactory(ProjectileType ammo, IProjectileFactory factory) {
        this.projectileForTypeFactories.put(ammo.getId(), factory);
    }

    public void registerFactory(Item ammo, IProjectileFactory factory) {
        this.projectileForAmmoFactories.put(ForgeRegistries.ITEMS.getKey(ammo), factory);
    }

    public IProjectileFactory getFactory(WeaponData data) {
        var item = WeaponStateHelper.getCurrentAmmo(data);
        var projectileType = WeaponStateHelper.getProjectileConfig(data).getProjectileType();
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

    public IThrowableProjectileFactory getFactory(ProjectileType projectileType) {
        var factory = throwableProjectileFactories.get(projectileType.getId());
        if(factory != null){
            return factory;
        }
        return DEFAULT_THROWABLE_FACTORY;
    }
}
