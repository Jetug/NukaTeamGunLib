package com.nukateam.ntgl.common.util.interfaces;

import com.nukateam.example.common.registery.WeaponModifiers;
import com.nukateam.ntgl.common.data.WeaponData;
import com.nukateam.ntgl.common.data.attachment.impl.Barrel;
import com.nukateam.ntgl.common.data.config.weapon.*;
import com.nukateam.ntgl.common.data.holders.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.Vec3;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * An interface that allows control over the behaviour of weapons through attachments.
 * See {@link WeaponModifiers} for examples of how this can be implemented. Implementations can then
 * be passed to "create" method of attachment objects. See {@link Barrel#create(float, IWeaponModifier...)}
 * <p>
 * Author: Jetug
 */
public interface IWeaponModifier {
    /**
     * Modify the volume of the fire sound. This does not change the distance the sound can be heard
     * from, just the volume.
     *
     * @param volume the current fire volume
     * @param data
     * @return the new volume
     */
    default float modifyFireSoundVolume(float volume, WeaponData data) {
        return volume;
    }

    default ResourceLocation modifyFireSound(ResourceLocation sound, WeaponData data) {
        return sound;
    }

    @Nullable
    default ResourceLocation modifySound(String name, @Nullable ResourceLocation sound, WeaponData data) {
        return sound;
    }

    /**
     * Determines if the fire sound should be the silenced version.
     * @return if this fire sound should be silenced version
     */
    default boolean silencedFire(boolean silenced, WeaponData data) {
        return silenced;
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
    default double modifyFireSoundRadius(double radius, WeaponData data) {
        return radius;
    }

    /**
     * Modify the damage of the projectile. This is called before critical logic is
     *
     * @param damage the current projectile damage
     * @param data
     * @return new damage for the projectile
     */
     default float modifyProjectileDamage(float damage, ResourceLocation ammo, WeaponData data) {
        return damage;
     }

    /**
     * Modify the spread when firing a projectile. This will affect the accuracy of weapons and only
     * applies to weapons that have spread enabled.
     *
     * @param spread the current weapon spread
     * @param data
     * @return a new spread for the weapon
     */
    default float modifyProjectileSpread(float spread, WeaponData data) {
        return spread;
    }

    /**
     * Add additional gravity to the projectile without changing the base gravity.
     *
     * @return additional gravity to add to the projectile
     */
    default double additionalProjectileGravity(WeaponData data) {
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
    default double modifyProjectileGravity(double gravity, WeaponData data) {
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
    default int modifyProjectileLife(int life, WeaponData data) {
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
    default float recoilModifier(WeaponData data) {
        return 1.0F;
    }

    default float modifyRecoil(float value, WeaponData data) {
        return value;
    }

    /**
     * Changes the amount of kick given when firing a weapon. This value is multiplied with the kick
     * value of the weapon. Anything greater than one will result in the weapon having more kick, while
     * less than one but more than zero will result in less kick. Changing this does not make the
     * weapon more or less accurate, it's simply visual.
     *
     * @return a value to multiply the weapon's kick
     */
    default float kickModifier(WeaponData data) {
        return 1.0F;
    }

    /**
     * Changes the visual size of the muzzle flash.
     *
     * @param size the current size
     * @param data
     * @return the new size for the muzzle flash
     */
    default double modifyMuzzleFlashSize(double size, WeaponData data) {
        return size;
    }

    /**
     * Changes the visual scale of the muzzle flash.
     *
     * @param scale the current scale
     * @param data
     * @return the new scale for the muzzle flash
     */
    default double modifyMuzzleFlashScale(double scale, WeaponData data) {
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
    default double modifyAimDownSightSpeed(double speed, WeaponData data) {
        return speed;
    }

    /**
     * Modifies the fire rate of a weapon. The lower the number, the faster the weapon will fire.
     *
     * @param rate the current fire rate
     * @param data
     * @return the new fire rate
     */
    default int modifyFireRate(int rate, WeaponData data) {
        return rate;
    }

    /**
     * Adds chance that critical damage will occur when hitting an entity with a projectile. This
     * can be positive or negative number, with negative reducing the chance of a critical hit.
     *
     * @return additional chance to include when determining critical hit
     */
    default float criticalChance(WeaponData data) {
        return 0F;
    }

    default int modifyMaxAmmo(int maxAmmo, WeaponData data) {
        return maxAmmo;
    }

    default int modifyProjectileAmount(int amount, WeaponData data) {
        return amount;
    }

    default int modifyMultishotAmount(int amount, WeaponData data) {
        return amount;
    }

    default int modifyReloadAmount(int amount, WeaponData data) {
        return amount;
    }

    default Set<FireMode> modifyFireModes(Set<FireMode> fireMode, WeaponData data) {
        return fireMode;
    }

    default GripType modifyGripType(GripType gripType, WeaponData data) {
        return gripType;
    }

    default boolean modifyOneHanded(boolean value, WeaponData data) {
        return value;
    }

    default int modifyFireDelay(int value, WeaponData data) {
        return value;
    }

    default boolean modifyNeedsFullCharge(boolean value, WeaponData data) {
        return value;
    }

    default boolean modifyIsOneTimeCharge(boolean value, WeaponData data) {
        return value;
    }

    default Set<AmmoHolder> modifyAmmoItems(Set<AmmoHolder> item, WeaponData data) {
        return item;
    }

    default Set<AmmoHolder> modifyFuelItems(Set<AmmoHolder> item, WeaponData data) {
        return item;
    }

    default int modifyReloadStart(int value, WeaponData data) {
        return value;
    }

    default int modifyReloadTime(int value, WeaponData data) {
        return value;
    }

    default int modifyReloadEnd(int value, WeaponData data) {
        return value;
    }

    default int modifyEquipTime(int equipTime, WeaponData data) {
        return equipTime;
    }

    default int modifyAmmoPerShot(int value, WeaponData data) {
        return value;
    }

    default boolean modifyAutoReloading(boolean value, WeaponData data) {
        return value;
    }

    default boolean modifyShouldRenderHud(boolean value, WeaponData data) {
        return value;
    }

    default ArrayList<AttributeModifier> modifyAttributeModifiers(ArrayList<AttributeModifier> value, WeaponData data) {
        return value;
    }

    default WeaponAction modifyWeaponAction(WeaponAction value, WeaponData data) {
        return value;
    }

    default WeaponModeMeta modifyWeaponModeMeta(WeaponModeMeta value, WeaponData data) {
        return value;
    }

    default HashMap<WeaponMode, WeaponSettings> modifyWeaponModes(HashMap<WeaponMode, WeaponSettings> value, WeaponData data) {
        return value;
    }

    default LoadingType modifyLoadingType(LoadingType loadingType, WeaponData data) {
        return loadingType;
    }

    default MeleeMode modifyMeleeMode(MeleeMode value, WeaponData data) {
        return value;
    }

    default int modifyMeleeCooldown(int value, WeaponData data) {
        return value;
    }

    default int modifyMeleeDelay(int value, WeaponData data) {
        return value;
    }

    default float modifyMeleeDamage (float value, WeaponData data) {
        return value;
    }

    default float modifyMeleeDistance (float value, WeaponData data) {
        return value;
    }

    default float modifyMeleeAngle(float value, WeaponData data) {
        return value;
    }

    default float modifyMeleeKnockback(float value, WeaponData data) {
        return value;
    }

    default int modifyMeleeMaxTargets(int value, WeaponData data) {
        return value;
    }

    default float modifyFov(float value, WeaponData data) {
        return value;
    }

    default Vec3 modifySightOffset(Vec3 value, WeaponData data) {
        return value;
    }

    default ProjectileConfig modifyProjectile(ProjectileConfig value, WeaponData data) {
        return value;
    }

    default Fuel modifyFuel(Fuel value, WeaponData data) {
        return value;
    }

    default ResourceLocation modifyAnimation(AnimationType tupe, ResourceLocation animation, WeaponData data) {
        return animation;
    }

    default AmmoConfig modifyAmmo(AmmoConfig value, WeaponData data) {
        return value;
    }

    default AmmoConfig modifyFuelAmmo(AmmoConfig value, WeaponData data) {
        return value;
    }

    default int modifyMaxFuel(ResourceLocation ammo, int max, WeaponData data) {
        return max;
    }

    default boolean modifyIsFuelMandatory(ResourceLocation ammo, boolean value, WeaponData data) {
        return value;
    }

    default int modifyFuelAmountPerUse(ResourceLocation ammo, int value, WeaponData data) {
        return value;
    }

    default int modifyPrepareTime(int item, WeaponData data) {
        return item;
    }

    default int modifyThrowTime(int item, WeaponData data) {
        return item;
    }

    default LinkedHashSet<ThrowMode> modifyThrowModes(LinkedHashSet<ThrowMode> item, WeaponData data) {
        return item;
    }

//    default boolean modifyDamageReduceOverDistance(boolean reduceOverDistance, GunData data) {
//        return reduceOverDistance;
//    }
//
//    default float modifyExplosionRadius(float radius, GunData data) {
//        return radius;
//    }
}
