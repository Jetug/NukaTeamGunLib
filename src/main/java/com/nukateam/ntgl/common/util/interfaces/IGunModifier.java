package com.nukateam.ntgl.common.util.interfaces;

import com.nukateam.ntgl.common.base.GunModifiers;
import com.nukateam.ntgl.common.base.holders.FireMode;
import com.nukateam.ntgl.common.base.holders.GripType;
import com.nukateam.ntgl.common.base.holders.LoadingType;
import com.nukateam.ntgl.common.base.holders.FuelType;
import com.nukateam.ntgl.common.data.attachment.impl.Barrel;
import com.nukateam.ntgl.common.util.util.GunData;
import net.minecraft.resources.ResourceLocation;

import java.util.Set;

/**
 * An interface that allows control over the behaviour of weapons through attachments.
 * See {@link GunModifiers} for examples of how this can be implemented. Implementations can then
 * be passed to "create" method of attachment objects. See {@link Barrel#create(float, IGunModifier...)}
 * <p>
 * Author: MrCrayfish
 */
public interface IGunModifier {
    /**
     * Modify the volume of the fire sound. This does not change the distance the sound can be heard
     * from, just the volume.
     *
     * @param volume the current fire volume
     * @param data
     * @return the new volume
     */
    default float modifyFireSoundVolume(float volume, GunData data) {
        return volume;
    }

    default ResourceLocation modifyFireSound(ResourceLocation sound, GunData data) {
        return sound;
    }

    /**
     * Determines if the fire sound should be the silenced version. If at one attachment on the
     * weapon has this set to true, it will be silenced regardless if other attachments specify false.
     *
     * @return if this fire sound should be silenced version
     */
    default boolean silencedFire(GunData data) {
        return false;
    }

    /**
     * Modifies the sound radius of the fired sound. It should be noted that if multiple attachments
     * modify the sound radius, the radius given in the arguments may not be exactly the same as the
     * weapon. This is because another attachment has already modified the radius.
     *
     * @param radius the current sound radius
     * @param data
     * @return the new sound radius
     */
    default double modifyFireSoundRadius(double radius, GunData data) {
        return radius;
    }

    /**
     * Adds additional damage to the weapon. This can be positive or negative number, with negative
     * reducing the damage of the weapon.
     *
     * @return additional damage to add on top of the weapon damage
     */
    default float additionalDamage(GunData data) {
        return 0.0F;
    }

    /**
     * Modify the damage of the projectile. This is called before critical logic is
     *
     * @param damage the current projectile damage
     * @param data
     * @return a new damage for the projectile
     */
    default float modifyDamage(float damage, GunData data) {
        return damage;
    }

    /**
     * Modify the speed of the projectile.
     *
     * @param speed the current projectile speed
     * @param data
     * @return a new speed for the projectile
     */
    default double modifyProjectileSpeed(double speed, GunData data) {
        return speed;
    }

    /**
     * Modify the spread when firing a projectile. This will affect the accuracy of weapons and only
     * applies to weapons that have spread enabled.
     *
     * @param spread the current weapon spread
     * @param data
     * @return a new spread for the weapon
     */
    default float modifyProjectileSpread(float spread, GunData data) {
        return spread;
    }

    /**
     * Add additional gravity to the projectile without changing the base gravity.
     *
     * @return additional gravity to add to the projectile
     */
    default double additionalProjectileGravity(GunData data) {
        return 0;
    }

    /**
     * Change the gravity of the projectile. The higher the value, the quicker the projectile will
     * fall to the ground.
     *
     * @param gravity the current gravity
     * @param data
     * @return a new gravity for the projectile
     */
    default double modifyProjectileGravity(double gravity, GunData data) {
        return gravity;
    }

    /**
     * Changes the life of the projectile. This is the maximum age before the projectile is removed
     * from the world. The higher the number, the longer it will be in the world (assuming it doesn't
     * collide).
     *
     * @param life the current projectile life
     * @param data
     * @return a new life for the projectile
     */
    default int modifyProjectileLife(int life, GunData data) {
        return life;
    }

    /**
     * Changes the amount of recoil given when firing a weapon. This value is multiplied with the recoil
     * value of the weapon. Anything greater than one will result in the weapon having more recoil, while
     * less than one but more than zero will result in less kick. Changing this does have an affect
     * of the accuracy of a weapon if recoil is enabled in the server config.
     *
     * @return a value to multiply the weapon's kick
     */
    default float recoilModifier(GunData data) {
        return 1.0F;
    }

    /**
     * Changes the amount of kick given when firing a weapon. This value is multiplied with the kick
     * value of the weapon. Anything greater than one will result in the weapon having more kick, while
     * less than one but more than zero will result in less kick. Changing this does not make the
     * weapon more or less accurate, it's simply visual.
     *
     * @return a value to multiply the weapon's kick
     */
    default float kickModifier(GunData data) {
        return 1.0F;
    }

    /**
     * Changes the visual size of the muzzle flash.
     *
     * @param size the current size
     * @param data
     * @return the new size for the muzzle flash
     */
    default double modifyMuzzleFlashSize(double size, GunData data) {
        return size;
    }

    /**
     * Changes the visual scale of the muzzle flash.
     *
     * @param scale the current scale
     * @param data
     * @return the new scale for the muzzle flash
     */
    default double modifyMuzzleFlashScale(double scale, GunData data) {
        return scale;
    }

    /**
     * Change the speed it takes to look down the sight of the weapon. The higher the speed, the
     * quicker the player will look down the sight.
     *
     * @param speed the current speed
     * @param data
     * @return the new speed
     */
    default double modifyAimDownSightSpeed(double speed, GunData data) {
        return speed;
    }

    /**
     * Modifies the fire rate of a weapon. The lower the number, the faster the weapon will fire.
     *
     * @param rate the current fire rate
     * @param data
     * @return the new fire rate
     */
    default int modifyFireRate(int rate, GunData data) {
        return rate;
    }

    /**
     * Adds chance that critical damage will occur when hitting an entity with a projectile. This
     * can be positive or negative number, with negative reducing the chance of a critical hit.
     *
     * @return additional chance to include when determining critical hit
     */
    default float criticalChance(GunData data) {
        return 0F;
    }

    default int modifyMaxAmmo(int maxAmmo, GunData data) {
        return maxAmmo;
    }

    default int modifyProjectileAmount(int amount, GunData data) {
        return amount;
    }

    default Set<FireMode> modifyFireModes(Set<FireMode> fireMode, GunData data) {
        return fireMode;
    }

    default GripType modifyGripType(GripType gripType, GunData data) {
        return gripType;
    }

    default int modifyFireDelay(int chargeTime, GunData data) {
        return chargeTime;
    }

    default boolean modifyNeedsFullCharge(boolean needsFullCharge, GunData data) {
        return needsFullCharge;
    }

    default boolean modifyIsOneTimeCharge(boolean oneTimeCharge, GunData data) {
        return oneTimeCharge;
    }

    default boolean modifyCanMelee(boolean value, GunData data) {
        return value;
    }

    default Set<ResourceLocation> modifyAmmoItems(Set<ResourceLocation> item, GunData data) {
        return item;
    }

    default int modifyReloadStart(int reloadTime, GunData data) {
        return reloadTime;
    }

    default int modifyReloadTime(int reloadTime, GunData data) {
        return reloadTime;
    }

    default int modifyReloadEnd(int reloadTime, GunData data) {
        return reloadTime;
    }

    default int modifyEquipTime(int equipTime, GunData data) {
        return equipTime;
    }

    default int modifyAmmoPerShot(int ammoPerShot, GunData data) {
        return ammoPerShot;
    }

    default boolean modifyAutoReloading(boolean autoReload, GunData data) {
        return autoReload;
    }

    default boolean modifyShouldRenderHud(boolean isRenderHud, GunData data) {
        return isRenderHud;
    }

    default LoadingType modifyLoadingType(LoadingType loadingType, GunData data) {
        return loadingType;
    }

    default Set<FuelType> modifyFuel(Set<FuelType> secondaryAmmo, GunData data) {
        return secondaryAmmo;
    }

    default int modifyMaxFuel(int max, FuelType type, GunData data) {
        return max;
    }

    default int modifyMeleeCooldown(int time, GunData data) {
        return time;
    }

    default int modifyMeleeDelay(int time, GunData data) {
        return time;
    }

    default float modifyMeleeDamage (float value, GunData data) {
        return value;
    }

    default float modifyMeleeDistance (float value, GunData data) {
        return value;
    }

    default float modifyMeleeAngle(float value, GunData data) {
        return value;
    }

    default float modifyMeleeKnockback(float value, GunData data) {
        return value;
    }

    default int modifyMeleeMaxTargets(int value, GunData data) {
        return value;
    }
}
