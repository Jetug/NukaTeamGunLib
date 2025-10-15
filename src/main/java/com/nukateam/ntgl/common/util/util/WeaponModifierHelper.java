package com.nukateam.ntgl.common.util.util;

import com.nukateam.ntgl.common.data.WeaponData;
import com.nukateam.ntgl.common.data.attachment.IAttachment;
import com.nukateam.ntgl.common.data.config.*;
import com.nukateam.ntgl.common.data.config.gun.General;

import com.nukateam.ntgl.common.data.config.gun.WeaponConfig;
import com.nukateam.ntgl.common.data.constants.Tags;
import com.nukateam.ntgl.common.data.holders.*;

import com.nukateam.ntgl.common.foundation.item.interfaces.IAmmo;
import com.nukateam.ntgl.common.foundation.item.interfaces.IWeapon;
import com.nukateam.ntgl.common.util.interfaces.IWeaponModifier;
import com.nukateam.ntgl.modules.enchantment.GunEnchantmentHelper;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import org.apache.commons.lang3.ArrayUtils;

import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;

import static net.minecraftforge.registries.ForgeRegistries.*;

public class WeaponModifierHelper {
    private static final IWeaponModifier[] EMPTY = {};
    public static final ProjectileConfig PROJECTILE = new ProjectileConfig();

    @Deprecated
    public static ProjectileConfig getCurrentAmmo(WeaponData data) {
        return WeaponStateHelper.getProjectileConfig(data);
    }

    public static boolean isAuto(WeaponData itemStack) {
        return WeaponStateHelper.getFireMode(itemStack) == FireMode.AUTO;
    }

    public static boolean isWeaponFull(WeaponData data) {
        var tag = data.weapon.getOrCreateTag();
        return tag.getInt(Tags.AMMO_COUNT) >= GunEnchantmentHelper.getAmmoCapacity(data);
    }

    public static boolean canUseOffhandWeapon(LivingEntity player){
        var mainHandItem = player.getMainHandItem();
        var offhandItem = player.getOffhandItem();

        return WeaponStateHelper.isOneHanded(new WeaponData(mainHandItem, player))
                && WeaponStateHelper.isOneHanded(new WeaponData(offhandItem, player));
    }

    public static boolean isGun(ItemStack data){
        var gunItem = data.getItem();
        return gunItem instanceof IWeapon;
    }

    public static WeaponConfig getConfig(ItemStack stack) {
        var gunItem = (IWeapon) stack.getItem();
        var mod = gunItem.getModifiedConfig(stack);
        return mod;
    }

    public static General getGeneral(WeaponData weaponData) {
        var config = getConfig(weaponData.weapon);
        return config.getGeneral(weaponData.weaponAction);
    }

    public static Melee getMelee(WeaponData weaponData) {
        var config = getConfig(weaponData.weapon);
        return config.getMelee(weaponData.weaponAction);
    }

    public static ThrowableConfig getThrowable(WeaponData weaponData) {
        var config = getConfig(weaponData.weapon);
        return config.getThrowable(weaponData.weaponAction);
    }

    public static Set<AttachmentType> getAttachmentTypes(WeaponData data) {
        var gun = getConfig(data.weapon);
        return gun.getModules().getAttachments().keySet();
    }

    public static boolean isThrowable(WeaponData data) {
        return getGeneral(data).getWeaponMode() == WeaponMode.THROWABLE;
    }

//    public static ArrayList<AttachmentType> getSortedAttachmentTypes(GunData data) {
//        var attachments = getAttachmentTypes(data);
//        var sortedTypes = new ArrayList<>(attachments.keySet());
//        sortedTypes.sort(Comparator.comparing(AttachmentType::toString));
//        return sortedTypes;
//    }

    public static boolean isAlwaysSpread(WeaponData data) {
        var isAlwaysSpread = new AtomicBoolean(getGeneral(data).isAlwaysSpread());
        return isAlwaysSpread.get();
    }

    public static int getMaxAmmo(WeaponData data) {
        var finalMaxAmmo = new AtomicInteger(getGeneral(data).getMaxAmmo());
        var config = WeaponStateHelper.getProjectileConfig(data);

        if (data != null && config != null && data.weapon.getItem() instanceof IWeapon) {
            if (WeaponStateHelper.getProjectileConfig(data).isMagazineMode()) {
                var id = WeaponStateHelper.getCurrentAmmo(data);
                var item = ITEMS.getValue(id.getId());
                finalMaxAmmo.set(item.getMaxDamage(new ItemStack(item)));
            }
        }

        forEachAttachment(data, (modifier -> finalMaxAmmo.set(modifier.modifyMaxAmmo(finalMaxAmmo.get(),  data))));
        return finalMaxAmmo.get();
    }

    public static boolean isAutoReloading(WeaponData data) {
        var autoReloading = new AtomicBoolean(getGeneral(data).isAutoReloading());
        forEachAttachment(data, (modifier -> autoReloading.set(modifier.modifyAutoReloading(autoReloading.get(), data))));
        return autoReloading.get();
    }

    public static ResourceLocation getFireSound(WeaponData data) {
        var fireSound = new AtomicReference<>(getConfig(data.weapon).getSounds().getFire());
        forEachAttachment(data, (modifier -> fireSound.set(modifier.modifyFireSound(fireSound.get(), data))));
        return fireSound.get();
    }

    public static boolean shouldRenderHud(WeaponData data) {
        var renderHud = new AtomicBoolean(getGeneral(data).shouldRenderHud());
        forEachAttachment(data, (modifier -> renderHud.set(modifier.modifyShouldRenderHud(renderHud.get(), data))));
        return renderHud.get();
    }

    public static LoadingType getLoadingType(WeaponData data) {
        var loadingType = new AtomicReference<>(getGeneral(data).getLoadingType());
        forEachAttachment(data, (modifier -> loadingType.set(modifier.modifyLoadingType(loadingType.get(), data))));
        return loadingType.get();
    }

    public static WeaponMode getWeaponMode(WeaponData data) {
        var loadingType = new AtomicReference<>(getGeneral(data).getWeaponMode());
        forEachAttachment(data, (modifier -> loadingType.set(modifier.modifyWeaponMode(loadingType.get(), data))));
        return loadingType.get();
    }

    public static int getProjectileAmount(WeaponData data) {
        var gunProjectileAmount = getGeneral(data).getProjectileAmount();
        var ammoProjectileAmount = WeaponStateHelper.getProjectileConfig(data).getProjectileAmount();

        var finalProjectileAmount = new AtomicInteger(gunProjectileAmount * ammoProjectileAmount);
        forEachAttachment(data, (modifier -> finalProjectileAmount.set(modifier.modifyProjectileAmount(finalProjectileAmount.get(), data))));
        return finalProjectileAmount.get();
    }



    public static int getReloadAmount(WeaponData data) {
        var value = new AtomicInteger(getGeneral(data).getReloadAmount());
        forEachAttachment(data, (modifier -> value.set(modifier.modifyReloadAmount(value.get(), data))));
        return value.get();
    }

    public static int getMultishotAmount(WeaponData data) {
        var value = new AtomicInteger(getGeneral(data).getMultishotAmount());
        forEachAttachment(data, (modifier -> value.set(modifier.modifyMultishotAmount(value.get(), data))));
        return value.get();
    }

    public static Set<FireMode> getFireModes(WeaponData data) {
        var fireMode = getGeneral(data).getFireModes();
        var finalFireMode = new AtomicReference<>(fireMode);
        forEachAttachment(data, (modifier -> finalFireMode.set(modifier.modifyFireModes(finalFireMode.get(), data))));
        return finalFireMode.get();
    }

    public static GripType getGripType(WeaponData data) {
        if(data.weapon == null) return GripType.ONE_HANDED;
        var gripType = getGeneral(data).getGripType();
        var finalGripType = new AtomicReference<>(gripType);
        forEachAttachment(data, (modifier -> finalGripType.set(modifier.modifyGripType(finalGripType.get(), data))));
        return finalGripType.get();
    }

    public static int getFireDelay(WeaponData data) {
        var chargeTime = new AtomicInteger(getGeneral(data).getFireDelay());
        forEachAttachment(data, (modifier -> chargeTime.set(modifier.modifyFireDelay(chargeTime.get(), data))));
        return chargeTime.get();
    }

//    public static Set<AmmoHolder> getFuelTypes(GunData data) {
//        var keys = getGun(data.gun).getFuel().keySet();
//        var fuel = new AtomicReference<>(keys);
//        forEachAttachment(data, (modifier -> fuel.set(modifier.modifyFuel(fuel.get(), data))));
//        return fuel.get();
//    }

    public static Fuel getFuel(ResourceLocation type, WeaponData data) {
        var value = new AtomicReference<>(getConfig(data.weapon).getFuelConfig(type));
        forEachAttachment(data, (modifier -> value.set(modifier.modifyFuel(value.get(), data))));
        return value.get();
    }

    public static ResourceLocation getAnimation(AnimationType type, WeaponData data) {
        var value = new AtomicReference<>(getConfig(data.weapon).getAnimation(type));
        forEachAttachment(data, (modifier -> value.set(modifier.modifyAnimation(type, value.get(), data))));
        return value.get();
    }

    public static boolean needsFullCharge(WeaponData data) {
        var needsFullCharge = new AtomicBoolean(getGeneral(data).isFullCharge());
        forEachAttachment(data, (modifier -> needsFullCharge.set(modifier.modifyNeedsFullCharge(needsFullCharge.get(), data))));
        return needsFullCharge.get();
    }

    public static boolean isOneTimeCharge(WeaponData data) {
        var oneTimeCharge = new AtomicBoolean(getGeneral(data).isOneTimeCharge());
        forEachAttachment(data, (modifier -> oneTimeCharge.set(modifier.modifyIsOneTimeCharge(oneTimeCharge.get(), data))));
        return oneTimeCharge.get();
    }

    public static boolean canShoot(WeaponData data) {
        var value = getWeaponMode(data);
        return value == WeaponMode.GUN;
    }

    public static boolean canMelee(WeaponData data) {
        var value = getWeaponMode(data);
        return value == WeaponMode.MELEE;
    }

    public static boolean canThrow(WeaponData data) {
        var value = getWeaponMode(data);
        return value == WeaponMode.THROWABLE;
    }

    public static Set<AmmoHolder> getAmmoItems(WeaponData data) {
        var items = getGeneral(data).getAmmo();
        var ammoItem = new AtomicReference<>(items);
        forEachAttachment(data, (modifier -> ammoItem.set(modifier.modifyAmmoItems(ammoItem.get(), data))));
        return ammoItem.get();
    }

    public static Set<AmmoHolder> getAllFuel(WeaponData data) {
        var items = getGeneral(data).getFuel();
        var ammoItem = new AtomicReference<>(items);
        forEachAttachment(data, (modifier -> ammoItem.set(modifier.modifyFuelItems(ammoItem.get(), data))));
        return ammoItem.get();
    }

    public static AmmoHolder getFirstAmmoItem(WeaponData data) {
        var items = getAmmoItems(data);
        return items.iterator().next();
    }

    public static int getReloadStart(WeaponData data) {
        var reloadTime = getGeneral(data).getReloadStart();
        var finalReloadTime = new AtomicInteger(reloadTime);
        forEachAttachment(data, (modifier -> finalReloadTime.set(modifier.modifyReloadStart(finalReloadTime.get(), data))));
        return finalReloadTime.get();
    }

    public static int getReloadTime(WeaponData data) {
        var reloadTime = getGeneral(data).getReloadTime();
        var finalReloadTime = new AtomicInteger(reloadTime);
        forEachAttachment(data, (modifier -> finalReloadTime.set(modifier.modifyReloadTime(finalReloadTime.get(), data))));
        return finalReloadTime.get();
    }

    public static int getReloadEnd(WeaponData data) {
        var reloadTime = getGeneral(data).getReloadEnd();
        var finalReloadTime = new AtomicInteger(reloadTime);
        forEachAttachment(data, (modifier -> finalReloadTime.set(modifier.modifyReloadEnd(finalReloadTime.get(), data))));
        return finalReloadTime.get();
    }

    public static int getEquipTime(WeaponData data) {
        var equipTime = getGeneral(data).getEquipTime();
        var finalEquipTime = new AtomicInteger(equipTime);
        forEachAttachment(data, (modifier -> finalEquipTime.set(modifier.modifyEquipTime(finalEquipTime.get(), data))));
        return finalEquipTime.get();
    }

    public static int getAmmoPerShot(WeaponData data) {
        var value = getGeneral(data).getAmmoPerShot();
        var finalEquipTime = new AtomicInteger(value);
        forEachAttachment(data, (modifier -> finalEquipTime.set(modifier.modifyAmmoPerShot(finalEquipTime.get(), data))));
        return finalEquipTime.get();
    }

    public static int getModifiedProjectileLife(WeaponData data, int life) {
        var finalLife = new AtomicInteger(life);
        forEachAttachment(data, (modifier -> finalLife.set(modifier.modifyProjectileLife(finalLife.get(), data))));
        return finalLife.get();
    }

    public static double getModifiedProjectileGravity(WeaponData data, double gravity) {
        var finalGravity = new AtomicReference<>(gravity);
        forEachAttachment(data, (modifier -> finalGravity.set(modifier.modifyProjectileGravity(finalGravity.get(), data))));
        forEachAttachment(data, (modifier -> finalGravity.updateAndGet(v -> v + modifier.additionalProjectileGravity(data))));
        return finalGravity.get();
    }

    public static float getModifiedSpread(WeaponData data) {
        var gunSpread = getGeneral(data).getSpread();
        var ammoSpread = WeaponStateHelper.getProjectileConfig(data).getSpread();
        var spread = Math.max(gunSpread + ammoSpread, 0);
        var finalSpread = new AtomicReference<>(spread);

        forEachAttachment(data, (modifier -> finalSpread.set(modifier.modifyProjectileSpread(finalSpread.get(), data))));
        return finalSpread.get();
    }

    public static float getModifiedMovementSpeed(WeaponData data) {
        var gunSpread = getGeneral(data).getMovementSpeed();
        var finalValue = new AtomicReference<>(gunSpread);

        forEachAttachment(data, (modifier -> finalValue.set(modifier.modifyMovementSpeed(finalValue.get(), data))));
        return finalValue.get();
    }

    public static double getModifiedProjectileSpeed(WeaponData data, double speed) {
        var finalSpeed = new AtomicReference<>(speed);
        forEachAttachment(data, (modifier -> finalSpeed.set(modifier.modifyProjectileSpeed(finalSpeed.get(), data))));
        return finalSpeed.get();
    }

    public static float getFireSoundVolume(WeaponData data) {
        var volume = new AtomicReference<>(1.0F);
        forEachAttachment(data, (modifier -> volume.set(modifier.modifyFireSoundVolume(volume.get(), data))));
        return Mth.clamp(volume.get(), 0.0F, 16.0F);
    }

    @Deprecated(since = "1.3.0", forRemoval = true)
    public static double getMuzzleFlashSize(WeaponData data, double size) {
        var finalSize = new AtomicReference<>(size);
        forEachAttachment(data, (modifier -> finalSize.set(modifier.modifyMuzzleFlashSize(finalSize.get(),  data))));
        return finalSize.get();
    }

    public static double getMuzzleFlashScale(WeaponData data, double scale) {
        var finalScale = new AtomicReference<>(scale);
        forEachAttachment(data, (modifier -> finalScale.set(modifier.modifyMuzzleFlashScale(scale, data))));
        return finalScale.get();
    }

    public static float getKickReduction(WeaponData data) {
        var kickReduction = new AtomicReference<>(1.0F);
        forEachAttachment(data, (modifier -> kickReduction.updateAndGet(v -> v * Mth.clamp(modifier.kickModifier(data), 0.0F, 1.0F))));
        return 1.0F - kickReduction.get();
    }

    public static float getRecoilModifier(WeaponData data) {
        var recoilReduction = new AtomicReference<>(1.0F);
        forEachAttachment(data, (modifier -> recoilReduction.updateAndGet(
                v -> v * Mth.clamp(modifier.recoilModifier(data), 0.0F, 1.0F))));

        return 1.0F - recoilReduction.get();
    }

    public static boolean isSilencedFire(WeaponData data) {
        var value = new AtomicBoolean(getGeneral(data).isSilenced());
        forEachAttachment(data, (modifier -> value.set(modifier.silencedFire(value.get(), data))));
        return value.get();
    }

    public static double getModifiedFireSoundRadius(WeaponData data, double radius) {
        var minRadius = new AtomicReference<>(radius);
        forEachAttachment(data, (modifier -> {
            var newRadius = modifier.modifyFireSoundRadius(radius, data);
            if (newRadius < minRadius.get()) {
                minRadius.set(newRadius);
            }
        }));
        return Mth.clamp(minRadius.get(), 0.0, Double.MAX_VALUE);
    }

    public static float getAdditionalDamage(WeaponData data) {
        var additionalDamage = new AtomicReference<>(0.0F);
        forEachAttachment(data, (modifier -> additionalDamage.updateAndGet(
                v -> v + modifier.additionalDamage(data))));
        return additionalDamage.get();
    }

    public static float getModifiedDamage(WeaponData data) {
        var damage = getGeneral(data).getDamage();
        damage *= getAmmoDamageMultiplier(data);
        var finalDamage = new AtomicReference<>(damage);
        forEachAttachment(data, (modifier -> finalDamage.set(modifier.modifyDamage(finalDamage.get(), data))));
        forEachAttachment(data, (modifier -> finalDamage.updateAndGet(v -> v + modifier.additionalDamage(data))));

        return finalDamage.get();
    }

    public static float getAmmoDamageMultiplier(WeaponData data){
        return WeaponStateHelper.getProjectileConfig(data).getDamage();
    }

    public static double getModifiedAimDownSightSpeed(WeaponData data) {
        double speed = GunEnchantmentHelper.getAimDownSightSpeed(data.weapon);
        var buffSpeed = new AtomicReference<>(speed);

        forEachAttachment(data, (modifier ->
                buffSpeed.set(modifier.modifyAimDownSightSpeed(buffSpeed.get(), data))));

        return Mth.clamp(buffSpeed.get(), 0.01, Double.MAX_VALUE);
    }

    public static int getRate(WeaponData data) {
        var rate = getGeneral(data).getRate();
        rate = GunEnchantmentHelper.getRate(data.weapon, rate);
        var buffRate = new AtomicInteger(rate);
        forEachAttachment(data, (modifier -> buffRate.set(modifier.modifyFireRate(buffRate.get(), data))));
        return Mth.clamp(buffRate.get(), 0, Integer.MAX_VALUE);
    }

    public static float getCriticalChance(WeaponData data) {
        var chance = new AtomicReference<>(0F);
        forEachAttachment(data, (modifier ->
                chance.updateAndGet(v -> v + modifier.criticalChance(data))));
        chance.updateAndGet(v -> v + GunEnchantmentHelper.getPuncturingChance(data.weapon));

        return Mth.clamp(chance.get(), 0F, 1F);
    }

    public static MeleeMode getMeleeMode(WeaponData data) {
        var value = new AtomicReference<>(getMelee(data).getMode());
        forEachAttachment(data, (modifier -> value.set(modifier.modifyMeleeMode(value.get(), data))));
        return value.get();
    }

    public static int getMeleeCooldown(WeaponData data) {
        var time = getMelee(data).getCooldown();
        var finalTime = new AtomicInteger(time);
        forEachAttachment(data, (modifier -> finalTime.set(modifier.modifyMeleeCooldown(finalTime.get(), data))));
        return finalTime.get();
    }

    public static int getMeleeDelay(WeaponData data) {
        var time = getMelee(data).getDelay();
        var finalTime = new AtomicInteger(time);
        forEachAttachment(data, (modifier -> finalTime.set(modifier.modifyMeleeDelay(finalTime.get(), data))));
        return finalTime.get();
    }

    public static int getMeleeMaxTargets(WeaponData data) {
        var time = getMelee(data).getMaxTargets();
        var finalTime = new AtomicInteger(time);
        forEachAttachment(data, (modifier -> finalTime.set(modifier.modifyMeleeMaxTargets(finalTime.get(), data))));
        return finalTime.get();
    }

    public static float getMeleeDamage(WeaponData data) {
        var value = getMelee(data).getDamage();
        var finalValue = new AtomicReference<Float>(value);
        forEachAttachment(data, (modifier -> finalValue.set(modifier.modifyMeleeDamage(finalValue.get(), data))));
        return finalValue.get();
    }

    public static float getMeleeDistance(WeaponData data) {
        var value = getMelee(data).getDistance();
        var finalValue = new AtomicReference<Float>(value);
        forEachAttachment(data, (modifier -> finalValue.set(modifier.modifyMeleeDistance(finalValue.get(), data))));
        return finalValue.get();
    }

    public static float getMeleeAngle(WeaponData data) {
        var value = getMelee(data).getAngle();
        var finalValue = new AtomicReference<Float>(value);
        forEachAttachment(data, (modifier -> finalValue.set(modifier.modifyMeleeAngle(finalValue.get(), data))));
        return finalValue.get();
    }

    public static float getMeleeKnockback(WeaponData data) {
        var value = getMelee(data).getKnockback();
        var finalValue = new AtomicReference<Float>(value);
        forEachAttachment(data, (modifier -> finalValue.set(modifier.modifyMeleeKnockback(finalValue.get(), data))));
        return finalValue.get();
    }

    public static ProjectileConfig getProjectileConfig(ResourceLocation ammoId, WeaponData data) {
        var gun = getConfig(data.weapon);
        ProjectileConfig config = null;
        var item = WeaponStateHelper.getCurrentAmmoWithoutCheck(data);

        if(gun.hasAmmo(ammoId)) {
            config = gun.getProjectileConfig(ammoId);
        }
        else if(item.canReturnAmmo() && ITEMS.getValue(item.getId()) instanceof IAmmo ammoItem) {
            config = ammoItem.getAmmo();
        }
        if(config == null) {
            config = new ProjectileConfig();
        }

        var finalValue = new AtomicReference<>(config);
        forEachAttachment(data, (modifier -> finalValue.set(modifier.modifyProjectile(finalValue.get(), data))));
        return finalValue.get();
    }

    public static AmmoConfig getAmmoConfig(ResourceLocation ammoId, WeaponData data) {
        var finalValue = new AtomicReference<>(getConfig(data.weapon).getAmmoConfig(ammoId));
        forEachAttachment(data, (modifier -> finalValue.set(modifier.modifyAmmo(finalValue.get(), data))));
        return finalValue.get();
    }

    public static AmmoConfig getFuelAmmoConfig(ResourceLocation ammoId, WeaponData data) {
        var finalValue = new AtomicReference<>(getConfig(data.weapon).getFuelAmmoConfig(ammoId));
        forEachAttachment(data, (modifier -> finalValue.set(modifier.modifyFuelAmmo(finalValue.get(), data))));
        return finalValue.get();
    }

    public static Integer getMaxFuel(ResourceLocation type, WeaponData data) {
        var fuel = getFuel(type, data);
        int max = fuel != null ? fuel.getMax() : 0;
        var result = new AtomicReference<>(max);

        forEachAttachment(data, (modifier -> result.set(modifier.modifyMaxFuel(type, result.get(), data))));
        return result.get();
    }

    public static boolean isFuelMandatory(ResourceLocation ammoId, WeaponData data) {
        var finalValue = new AtomicReference<>(getConfig(data.weapon).getFuelConfig(ammoId).isMandatory());
        forEachAttachment(data, (modifier -> finalValue.set(modifier.modifyIsFuelMandatory(ammoId, finalValue.get(), data))));
        return finalValue.get();
    }

    public static int getFuelAmountPerUse(ResourceLocation ammoId, WeaponData data) {
        var finalValue = new AtomicReference<>(getConfig(data.weapon).getFuelConfig(ammoId).getAmountPerUse());
        forEachAttachment(data, (modifier -> finalValue.set(modifier.modifyFuelAmountPerUse(ammoId, finalValue.get(), data))));
        return finalValue.get();
    }

    private static void forEachAttachment(WeaponData data, Consumer<IWeaponModifier> consumer){
        var gun = data.weapon;
        var config = getConfig(gun);
        var attachments = config.getModules().getAttachments();

        for (var attachmentType : attachments.keySet()) {
            var attachmentItem = WeaponStateHelper.getAttachmentItem(attachmentType, gun);
            data.attachment = attachmentItem;
            var modifiers = getAttachmentModifiers(attachmentItem);

            for (var modifier : modifiers) {
                consumer.accept(modifier);
            }
        }

        var gunItem = (IWeapon) gun.getItem();

        for (var modifier : gunItem.getModifiers()) {
            consumer.accept(modifier);
        }
    }

    private static IWeaponModifier[] getAttachmentModifiers(ItemStack attachmentItem) {
        if (!attachmentItem.isEmpty() && attachmentItem.getItem() instanceof IAttachment<?> attachment) {
            var modifiers = attachment.getProperties().getModifiers();
            var configModifiers = attachment.getAttachmentConfig().getModifiers();
            return ArrayUtils.add(modifiers, configModifiers);
        }
        return EMPTY;
    }
}
