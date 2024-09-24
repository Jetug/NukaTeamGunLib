package com.nukateam.ntgl.common.data.interfaces;

import com.nukateam.ntgl.common.base.GunModifiers;
import com.nukateam.ntgl.common.base.gun.FireMode;
import com.nukateam.ntgl.common.base.gun.GripType;
import com.nukateam.ntgl.common.foundation.item.attachment.impl.Barrel;
import net.minecraft.resources.ResourceLocation;

import java.util.ArrayList;
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
     * @return the new volume
     */
    default float modifyFireSoundVolume(float volume) {
        return volume;
    }

    /**
     * Determines if the fire sound should be the silenced version. If at one attachment on the
     * weapon has this set to true, it will be silenced regardless if other attachments specify false.
     *
     * @return if this fire sound should be silenced version
     */
    default boolean silencedFire() {
        return false;
    }

    /**
     * Modifies the sound radius of the fired sound. It should be noted that if multiple attachments
     * modify the sound radius, the radius given in the arguments may not be exactly the same as the
     * weapon. This is because another attachment has already modified the radius.
     *
     * @param radius the current sound radius
     * @return the new sound radius
     */
    default double modifyFireSoundRadius(double radius) {
        return radius;
    }

    /**
     * Adds additional damage to the weapon. This can be positive or negative number, with negative
     * reducing the damage of the weapon.
     *
     * @return additional damage to add on top of the weapon damage
     */
    default float additionalDamage() {
        return 0.0F;
    }

    /**
     * Modify the damage of the ammo. This is called before critical logic is
     *
     * @param damage the current ammo damage
     * @return a new damage for the ammo
     */
    default float modifyProjectileDamage(float damage) {
        return damage;
    }

    /**
     * Modify the speed of the ammo.
     *
     * @param speed the current ammo speed
     * @return a new speed for the ammo
     */
    default double modifyProjectileSpeed(double speed) {
        return speed;
    }

    /**
     * Modify the spread when firing a ammo. This will affect the accuracy of weapons and only
     * applies to weapons that have spread enabled.
     *
     * @param spread the current weapon spread
     * @return a new spread for the weapon
     */
    default float modifyProjectileSpread(float spread) {
        return spread;
    }

    /**
     * Add additional gravity to the ammo without changing the base gravity.
     *
     * @return additional gravity to add to the ammo
     */
    default double additionalProjectileGravity() {
        return 0;
    }

    /**
     * Change the gravity of the ammo. The higher the value, the quicker the ammo will
     * fall to the ground.
     *
     * @param gravity the current gravity
     * @return a new gravity for the ammo
     */
    default double modifyProjectileGravity(double gravity) {
        return gravity;
    }

    /**
     * Changes the life of the ammo. This is the maximum age before the ammo is removed
     * from the world. The higher the number, the longer it will be in the world (assuming it doesn't
     * collide).
     *
     * @param life the current ammo life
     * @return a new life for the ammo
     */
    default int modifyProjectileLife(int life) {
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
    default float recoilModifier() {
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
    default float kickModifier() {
        return 1.0F;
    }

    /**
     * Changes the visual size of the muzzle flash.
     *
     * @param size the current size
     * @return the new size for the muzzle flash
     */
    @Deprecated(since = "1.3.0", forRemoval = true)
    default double modifyMuzzleFlashSize(double size) {
        return size;
    }

    /**
     * Changes the visual scale of the muzzle flash.
     *
     * @param scale the current scale
     * @return the new scale for the muzzle flash
     */
    default double modifyMuzzleFlashScale(double scale) {
        return scale;
    }

    /**
     * Change the speed it takes to look down the sight of the weapon. The higher the speed, the
     * quicker the player will look down the sight.
     *
     * @param speed the current speed
     * @return the new speed
     */
    default double modifyAimDownSightSpeed(double speed) {
        return speed;
    }

    /**
     * Modifies the fire rate of a weapon. The lower the number, the faster the weapon will fire.
     *
     * @param rate the current fire rate
     * @return the new fire rate
     */
    default int modifyFireRate(int rate) {
        return rate;
    }

    /**
     * Adds chance that critical damage will occur when hitting an entity with a ammo. This
     * can be positive or negative number, with negative reducing the chance of a critical hit.
     *
     * @return additional chance to include when determining critical hit
     */
    default float criticalChance() {
        return 0F;
    }

    default int modifyMaxAmmo(int maxAmmo) {
        return maxAmmo;
    }

    default Set<FireMode> modifyFireModes(Set<FireMode> fireMode) {
        return fireMode;
    }

    default GripType modifyGripType(GripType gripType) {
        return gripType;
    }

    default Set<ResourceLocation> modifyAmmoItems(Set<ResourceLocation> item) {
        return item;
    }

    default int modifyReloadTime(int reloadTime) {
        return reloadTime;
    }
}
