package com.nukateam.ntgl.common.foundation.event;

import com.nukateam.ntgl.common.data.WeaponData;
import com.nukateam.ntgl.common.foundation.entity.ProjectileEntity;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.bus.api.Event;

public class GunProjectileSpawnEvent extends Event {
    private final ProjectileEntity projectile;
    private final LivingEntity shooter;
    private final WeaponData weaponData;
    
    private float damageMultiplier = 1.0f;
    private float criticalChanceMultiplier = 1.0f;
    private float criticalDamageMultiplier = 1.0f;

    public GunProjectileSpawnEvent(ProjectileEntity projectile, LivingEntity shooter, WeaponData weaponData) {
        this.projectile = projectile;
        this.shooter = shooter;
        this.weaponData = weaponData;
    }

    public ProjectileEntity getProjectile() {
        return projectile;
    }

    public LivingEntity getShooter() {
        return shooter;
    }

    public WeaponData getWeaponData() {
        return weaponData;
    }

    public float getDamageMultiplier() {
        return damageMultiplier;
    }

    public void setDamageMultiplier(float damageMultiplier) {
        this.damageMultiplier = damageMultiplier;
    }

    public float getCriticalChanceMultiplier() {
        return criticalChanceMultiplier;
    }

    public void setCriticalChanceMultiplier(float criticalChanceMultiplier) {
        this.criticalChanceMultiplier = criticalChanceMultiplier;
    }

    public float getCriticalDamageMultiplier() {
        return criticalDamageMultiplier;
    }

    public void setCriticalDamageMultiplier(float criticalDamageMultiplier) {
        this.criticalDamageMultiplier = criticalDamageMultiplier;
    }
}
