package com.nukateam.ntgl.common.util.util;

import com.nukateam.ntgl.common.data.GunData;
import com.nukateam.ntgl.common.data.attachment.IAttachment;
import com.nukateam.ntgl.common.data.config.*;
import com.nukateam.ntgl.common.data.config.gun.General;

import com.nukateam.ntgl.common.data.config.gun.Gun;
import com.nukateam.ntgl.common.data.constants.Tags;
import com.nukateam.ntgl.common.data.holders.*;
import com.nukateam.ntgl.common.foundation.item.WeaponItem;
import com.nukateam.ntgl.common.foundation.item.interfaces.IAmmo;
import com.nukateam.ntgl.common.foundation.item.interfaces.IWeapon;
import com.nukateam.ntgl.common.util.interfaces.IGunModifier;
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

/**
 * Author: MrCrayfish
 */
public class GunModifierHelper {
    private static final IGunModifier[] EMPTY = {};
    public static final ProjectileConfig PROJECTILE = new ProjectileConfig();

    @Deprecated
    public static ProjectileConfig getCurrentAmmo(GunData data) {
        return GunStateHelper.getProjectileConfig(data);
    }

    public static boolean isAuto(GunData itemStack) {
        return GunStateHelper.getFireMode(itemStack) == FireMode.AUTO;
    }

    public static boolean isWeaponFull(GunData data) {
        var tag = data.gun.getOrCreateTag();
        var gun = ((WeaponItem)data.gun.getItem()).getModifiedConfig(data.gun);
        return tag.getInt(Tags.AMMO_COUNT) >= GunEnchantmentHelper.getAmmoCapacity(data);
    }

    public static boolean canRenderInOffhand(LivingEntity player){
        var mainHandItem = player.getMainHandItem();
        var offhandItem = player.getOffhandItem();

        return GunStateHelper.isOneHanded(new GunData(mainHandItem, player))
                && GunStateHelper.isOneHanded(new GunData(offhandItem, player));
    }

    public static boolean isGun(ItemStack data){
        var gunItem = data.getItem();
        return gunItem instanceof WeaponItem;
    }

    public static Gun getGun(ItemStack stack) {
        var gunItem = (IWeapon) stack.getItem();
        return gunItem.getModifiedConfig(stack);
    }

    public static General getGeneral(GunData gunData) {
        var config = getGun(gunData.gun);
        return config.getGeneral(gunData.weaponAction);
    }

    public static Melee getMelee(GunData gunData) {
        var config = getGun(gunData.gun);
        return config.getMelee(gunData.weaponAction);
    }

    public static ThrowableConfig getThrowable(GunData gunData) {
        var config = getGun(gunData.gun);
        return config.getThrowable(gunData.weaponAction);
    }

    public static Set<AttachmentType> getAttachmentTypes(GunData data) {
        var gun = getGun(data.gun);
        return gun.getModules().getAttachments().keySet();
    }

    public static boolean isThrowable(GunData data) {
        return GunModifierHelper.getGun(data.gun).getGeneral().getWeaponMode() == WeaponMode.THROWABLE;
    }

//    public static ArrayList<AttachmentType> getSortedAttachmentTypes(GunData data) {
//        var attachments = getAttachmentTypes(data);
//        var sortedTypes = new ArrayList<>(attachments.keySet());
//        sortedTypes.sort(Comparator.comparing(AttachmentType::toString));
//        return sortedTypes;
//    }

    public static boolean isAlwaysSpread(GunData data) {
        var isAlwaysSpread = new AtomicBoolean(getGeneral(data).isAlwaysSpread());
        return isAlwaysSpread.get();
    }

    public static int getMaxAmmo(GunData data) {
        var finalMaxAmmo = new AtomicInteger(getGeneral(data).getMaxAmmo());
        var config = GunStateHelper.getProjectileConfig(data);

        if (data != null && config != null && data.gun.getItem() instanceof WeaponItem) {
            if (GunStateHelper.getProjectileConfig(data).isMagazineMode()) {
                var id = GunStateHelper.getCurrentAmmo(data);
                var item = ITEMS.getValue(id.getId());
                finalMaxAmmo.set(item.getMaxDamage(new ItemStack(item)));
            }
        }

        forEachAttachment(data, (modifier -> finalMaxAmmo.set(modifier.modifyMaxAmmo(finalMaxAmmo.get(),  data))));
        return finalMaxAmmo.get();
    }

    public static boolean isAutoReloading(GunData data) {
        var autoReloading = new AtomicBoolean(getGeneral(data).isAutoReloading());
        forEachAttachment(data, (modifier -> autoReloading.set(modifier.modifyAutoReloading(autoReloading.get(), data))));
        return autoReloading.get();
    }

    public static ResourceLocation getFireSound(GunData data) {
        var fireSound = new AtomicReference<>(getGun(data.gun).getSounds().getFire());
        forEachAttachment(data, (modifier -> fireSound.set(modifier.modifyFireSound(fireSound.get(), data))));
        return fireSound.get();
    }

    public static boolean shouldRenderHud(GunData data) {
        var renderHud = new AtomicBoolean(getGeneral(data).shouldRenderHud());
        forEachAttachment(data, (modifier -> renderHud.set(modifier.modifyShouldRenderHud(renderHud.get(), data))));
        return renderHud.get();
    }

    public static LoadingType getLoadingType(GunData data) {
        var loadingType = new AtomicReference<>(getGeneral(data).getLoadingType());
        forEachAttachment(data, (modifier -> loadingType.set(modifier.modifyLoadingType(loadingType.get(), data))));
        return loadingType.get();
    }

    public static WeaponMode getWeaponMode(GunData data) {
        var loadingType = new AtomicReference<>(getGeneral(data).getWeaponMode());
        forEachAttachment(data, (modifier -> loadingType.set(modifier.modifyWeaponMode(loadingType.get(), data))));
        return loadingType.get();
    }

    public static int getProjectileAmount(GunData data) {
        var gunProjectileAmount = getGeneral(data).getProjectileAmount();
        var ammoProjectileAmount = GunStateHelper.getProjectileConfig(data).getProjectileAmount();

        var finalProjectileAmount = new AtomicInteger(gunProjectileAmount * ammoProjectileAmount);
        forEachAttachment(data, (modifier -> finalProjectileAmount.set(modifier.modifyProjectileAmount(finalProjectileAmount.get(), data))));
        return finalProjectileAmount.get();
    }



    public static int getReloadAmount(GunData data) {
        var value = new AtomicInteger(getGeneral(data).getReloadAmount());
        forEachAttachment(data, (modifier -> value.set(modifier.modifyReloadAmount(value.get(), data))));
        return value.get();
    }

    public static int getMultishotAmount(GunData data) {
        var value = new AtomicInteger(getGeneral(data).getMultishotAmount());
        forEachAttachment(data, (modifier -> value.set(modifier.modifyMultishotAmount(value.get(), data))));
        return value.get();
    }

    public static Set<FireMode> getFireModes(GunData data) {
        var fireMode = getGeneral(data).getFireModes();
        var finalFireMode = new AtomicReference<>(fireMode);
        forEachAttachment(data, (modifier -> finalFireMode.set(modifier.modifyFireModes(finalFireMode.get(), data))));
        return finalFireMode.get();
    }

    public static GripType getGripType(GunData data) {
        if(data.gun == null) return GripType.ONE_HANDED;
        var gripType = getGeneral(data).getGripType();
        var finalGripType = new AtomicReference<>(gripType);
        forEachAttachment(data, (modifier -> finalGripType.set(modifier.modifyGripType(finalGripType.get(), data))));
        return finalGripType.get();
    }

    public static int getFireDelay(GunData data) {
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

    public static Fuel getFuel(ResourceLocation type, GunData data) {
        var value = new AtomicReference<>(getGun(data.gun).getFuelConfig(type));
        forEachAttachment(data, (modifier -> value.set(modifier.modifyFuel(value.get(), data))));
        return value.get();
    }

    public static ResourceLocation getAnimation(AnimationType type, GunData data) {
        var value = new AtomicReference<>(getGun(data.gun).getAnimation(type));
        forEachAttachment(data, (modifier -> value.set(modifier.modifyAnimation(type, value.get(), data))));
        return value.get();
    }

    public static boolean needsFullCharge(GunData data) {
        var needsFullCharge = new AtomicBoolean(getGeneral(data).isFullCharge());
        forEachAttachment(data, (modifier -> needsFullCharge.set(modifier.modifyNeedsFullCharge(needsFullCharge.get(), data))));
        return needsFullCharge.get();
    }

    public static boolean isOneTimeCharge(GunData data) {
        var oneTimeCharge = new AtomicBoolean(getGeneral(data).isOneTimeCharge());
        forEachAttachment(data, (modifier -> oneTimeCharge.set(modifier.modifyIsOneTimeCharge(oneTimeCharge.get(), data))));
        return oneTimeCharge.get();
    }

    public static boolean canMelee(GunData data) {
        var value = new AtomicBoolean(getGeneral(data).canMelee());
        forEachAttachment(data, (modifier -> value.set(modifier.modifyCanMelee(value.get(), data))));
        return value.get();
    }

    public static Set<AmmoHolder> getAmmoItems(GunData data) {
        var items = getGeneral(data).getAmmo();
        var ammoItem = new AtomicReference<>(items);
        forEachAttachment(data, (modifier -> ammoItem.set(modifier.modifyAmmoItems(ammoItem.get(), data))));
        return ammoItem.get();
    }

    public static Set<AmmoHolder> getAllFuel(GunData data) {
        var items = getGeneral(data).getFuel();
        var ammoItem = new AtomicReference<>(items);
        forEachAttachment(data, (modifier -> ammoItem.set(modifier.modifyFuelItems(ammoItem.get(), data))));
        return ammoItem.get();
    }

    public static AmmoHolder getFirstAmmoItem(GunData data) {
        var items = getAmmoItems(data);
        return items.iterator().next();
    }

    public static int getReloadStart(GunData data) {
        var reloadTime = getGeneral(data).getReloadStart();
        var finalReloadTime = new AtomicInteger(reloadTime);
        forEachAttachment(data, (modifier -> finalReloadTime.set(modifier.modifyReloadStart(finalReloadTime.get(), data))));
        return finalReloadTime.get();
    }

    public static int getReloadTime(GunData data) {
        var reloadTime = getGeneral(data).getReloadTime();
        var finalReloadTime = new AtomicInteger(reloadTime);
        forEachAttachment(data, (modifier -> finalReloadTime.set(modifier.modifyReloadTime(finalReloadTime.get(), data))));
        return finalReloadTime.get();
    }

    public static int getReloadEnd(GunData data) {
        var reloadTime = getGeneral(data).getReloadEnd();
        var finalReloadTime = new AtomicInteger(reloadTime);
        forEachAttachment(data, (modifier -> finalReloadTime.set(modifier.modifyReloadEnd(finalReloadTime.get(), data))));
        return finalReloadTime.get();
    }

    public static int getEquipTime(GunData data) {
        var equipTime = getGeneral(data).getEquipTime();
        var finalEquipTime = new AtomicInteger(equipTime);
        forEachAttachment(data, (modifier -> finalEquipTime.set(modifier.modifyEquipTime(finalEquipTime.get(), data))));
        return finalEquipTime.get();
    }

    public static int getAmmoPerShot(GunData data) {
        var value = getGeneral(data).getAmmoPerShot();
        var finalEquipTime = new AtomicInteger(value);
        forEachAttachment(data, (modifier -> finalEquipTime.set(modifier.modifyAmmoPerShot(finalEquipTime.get(), data))));
        return finalEquipTime.get();
    }

    public static int getModifiedProjectileLife(GunData data, int life) {
        var finalLife = new AtomicInteger(life);
        forEachAttachment(data, (modifier -> finalLife.set(modifier.modifyProjectileLife(finalLife.get(), data))));
        return finalLife.get();
    }

    public static double getModifiedProjectileGravity(GunData data, double gravity) {
        var finalGravity = new AtomicReference<>(gravity);
        forEachAttachment(data, (modifier -> finalGravity.set(modifier.modifyProjectileGravity(finalGravity.get(), data))));
        forEachAttachment(data, (modifier -> finalGravity.updateAndGet(v -> v + modifier.additionalProjectileGravity(data))));
        return finalGravity.get();
    }

    public static float getModifiedSpread(GunData data) {
        var gunSpread = getGeneral(data).getSpread();
        var ammoSpread = GunStateHelper.getProjectileConfig(data).getSpread();
        var spread = Math.max(gunSpread + ammoSpread, 0);
        var finalSpread = new AtomicReference<>(spread);

        forEachAttachment(data, (modifier -> finalSpread.set(modifier.modifyProjectileSpread(finalSpread.get(), data))));
        return finalSpread.get();
    }

    public static float getModifiedMovementSpeed(GunData data) {
        var gunSpread = getGeneral(data).getMovementSpeed();
        var finalValue = new AtomicReference<>(gunSpread);

        forEachAttachment(data, (modifier -> finalValue.set(modifier.modifyMovementSpeed(finalValue.get(), data))));
        return finalValue.get();
    }

    public static double getModifiedProjectileSpeed(GunData data, double speed) {
        var finalSpeed = new AtomicReference<>(speed);
        forEachAttachment(data, (modifier -> finalSpeed.set(modifier.modifyProjectileSpeed(finalSpeed.get(), data))));
        return finalSpeed.get();
    }

    public static float getFireSoundVolume(GunData data) {
        var volume = new AtomicReference<>(1.0F);
        forEachAttachment(data, (modifier -> volume.set(modifier.modifyFireSoundVolume(volume.get(), data))));
        return Mth.clamp(volume.get(), 0.0F, 16.0F);
    }

    @Deprecated(since = "1.3.0", forRemoval = true)
    public static double getMuzzleFlashSize(GunData data, double size) {
        var finalSize = new AtomicReference<>(size);
        forEachAttachment(data, (modifier -> finalSize.set(modifier.modifyMuzzleFlashSize(finalSize.get(),  data))));
        return finalSize.get();
    }

    public static double getMuzzleFlashScale(GunData data, double scale) {
        var finalScale = new AtomicReference<>(scale);
        forEachAttachment(data, (modifier -> finalScale.set(modifier.modifyMuzzleFlashScale(scale, data))));
        return finalScale.get();
    }

    public static float getKickReduction(GunData data) {
        var kickReduction = new AtomicReference<>(1.0F);
        forEachAttachment(data, (modifier -> kickReduction.updateAndGet(v -> v * Mth.clamp(modifier.kickModifier(data), 0.0F, 1.0F))));
        return 1.0F - kickReduction.get();
    }

    public static float getRecoilModifier(GunData data) {
        var recoilReduction = new AtomicReference<>(1.0F);
        forEachAttachment(data, (modifier -> recoilReduction.updateAndGet(
                v -> v * Mth.clamp(modifier.recoilModifier(data), 0.0F, 1.0F))));

        return 1.0F - recoilReduction.get();
    }

    public static boolean isSilencedFire(GunData data) {
        var value = new AtomicBoolean(getGeneral(data).isSilenced());
        forEachAttachment(data, (modifier -> value.set(modifier.silencedFire(value.get(), data))));
        return value.get();
    }

    public static double getModifiedFireSoundRadius(GunData data, double radius) {
        var minRadius = new AtomicReference<>(radius);
        forEachAttachment(data, (modifier -> {
            var newRadius = modifier.modifyFireSoundRadius(radius, data);
            if (newRadius < minRadius.get()) {
                minRadius.set(newRadius);
            }
        }));
        return Mth.clamp(minRadius.get(), 0.0, Double.MAX_VALUE);
    }

    public static float getAdditionalDamage(GunData data) {
        var additionalDamage = new AtomicReference<>(0.0F);
        forEachAttachment(data, (modifier -> additionalDamage.updateAndGet(
                v -> v + modifier.additionalDamage(data))));
        return additionalDamage.get();
    }

    public static float getModifiedDamage(GunData data) {
        var damage = getGeneral(data).getDamage();
        damage *= getAmmoDamageMultiplier(data);
        var finalDamage = new AtomicReference<>(damage);
        forEachAttachment(data, (modifier -> finalDamage.set(modifier.modifyDamage(finalDamage.get(), data))));
        forEachAttachment(data, (modifier -> finalDamage.updateAndGet(v -> v + modifier.additionalDamage(data))));

        return finalDamage.get();
    }

    public static float getAmmoDamageMultiplier(GunData data){
        return GunStateHelper.getProjectileConfig(data).getDamage();
    }

    public static double getModifiedAimDownSightSpeed(GunData data) {
        double speed = GunEnchantmentHelper.getAimDownSightSpeed(data.gun);
        var buffSpeed = new AtomicReference<>(speed);

        forEachAttachment(data, (modifier ->
                buffSpeed.set(modifier.modifyAimDownSightSpeed(buffSpeed.get(), data))));

        return Mth.clamp(buffSpeed.get(), 0.01, Double.MAX_VALUE);
    }

    public static int getRate(GunData data) {
        var rate = getGeneral(data).getRate();
        rate = GunEnchantmentHelper.getRate(data.gun, rate);
        var buffRate = new AtomicInteger(rate);
        forEachAttachment(data, (modifier -> buffRate.set(modifier.modifyFireRate(buffRate.get(), data))));
        return Mth.clamp(buffRate.get(), 0, Integer.MAX_VALUE);
    }

    public static float getCriticalChance(GunData data) {
        var chance = new AtomicReference<>(0F);
        forEachAttachment(data, (modifier ->
                chance.updateAndGet(v -> v + modifier.criticalChance(data))));
        chance.updateAndGet(v -> v + GunEnchantmentHelper.getPuncturingChance(data.gun));

        return Mth.clamp(chance.get(), 0F, 1F);
    }

    public static MeleeMode getMeleeMode(GunData data) {
        var value = new AtomicReference<>(getMelee(data).getMode());
        forEachAttachment(data, (modifier -> value.set(modifier.modifyMeleeMode(value.get(), data))));
        return value.get();
    }

    public static int getMeleeCooldown(GunData data) {
        var time = getMelee(data).getCooldown();
        var finalTime = new AtomicInteger(time);
        forEachAttachment(data, (modifier -> finalTime.set(modifier.modifyMeleeCooldown(finalTime.get(), data))));
        return finalTime.get();
    }

    public static int getMeleeDelay(GunData data) {
        var time = getMelee(data).getDelay();
        var finalTime = new AtomicInteger(time);
        forEachAttachment(data, (modifier -> finalTime.set(modifier.modifyMeleeDelay(finalTime.get(), data))));
        return finalTime.get();
    }

    public static int getMeleeMaxTargets(GunData data) {
        var time = getMelee(data).getMaxTargets();
        var finalTime = new AtomicInteger(time);
        forEachAttachment(data, (modifier -> finalTime.set(modifier.modifyMeleeMaxTargets(finalTime.get(), data))));
        return finalTime.get();
    }

    public static float getMeleeDamage(GunData data) {
        var value = getMelee(data).getDamage();
        var finalValue = new AtomicReference<Float>(value);
        forEachAttachment(data, (modifier -> finalValue.set(modifier.modifyMeleeDamage(finalValue.get(), data))));
        return finalValue.get();
    }

    public static float getMeleeDistance(GunData data) {
        var value = getMelee(data).getDistance();
        var finalValue = new AtomicReference<Float>(value);
        forEachAttachment(data, (modifier -> finalValue.set(modifier.modifyMeleeDistance(finalValue.get(), data))));
        return finalValue.get();
    }

    public static float getMeleeAngle(GunData data) {
        var value = getMelee(data).getAngle();
        var finalValue = new AtomicReference<Float>(value);
        forEachAttachment(data, (modifier -> finalValue.set(modifier.modifyMeleeAngle(finalValue.get(), data))));
        return finalValue.get();
    }

    public static float getMeleeKnockback(GunData data) {
        var value = getMelee(data).getKnockback();
        var finalValue = new AtomicReference<Float>(value);
        forEachAttachment(data, (modifier -> finalValue.set(modifier.modifyMeleeKnockback(finalValue.get(), data))));
        return finalValue.get();
    }

    public static ProjectileConfig getProjectileConfig(ResourceLocation ammoId, GunData data) {
        var gun = getGun(data.gun);
        ProjectileConfig config = null;
        var item = GunStateHelper.getCurrentAmmoWithoutCheck(data);

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

    public static AmmoConfig getAmmoConfig(ResourceLocation ammoId, GunData data) {
        var finalValue = new AtomicReference<>(getGun(data.gun).getAmmoConfig(ammoId));
        forEachAttachment(data, (modifier -> finalValue.set(modifier.modifyAmmo(finalValue.get(), data))));
        return finalValue.get();
    }

    public static AmmoConfig getFuelAmmoConfig(ResourceLocation ammoId, GunData data) {
        var finalValue = new AtomicReference<>(getGun(data.gun).getFuelAmmoConfig(ammoId));
        forEachAttachment(data, (modifier -> finalValue.set(modifier.modifyFuelAmmo(finalValue.get(), data))));
        return finalValue.get();
    }

    public static Integer getMaxFuel(ResourceLocation type, GunData data) {
        var fuel = getFuel(type, data);
        int max = fuel != null ? fuel.getMax() : 0;
        var result = new AtomicReference<>(max);

        forEachAttachment(data, (modifier -> result.set(modifier.modifyMaxFuel(type, result.get(), data))));
        return result.get();
    }

    public static boolean isFuelMandatory(ResourceLocation ammoId, GunData data) {
        var finalValue = new AtomicReference<>(getGun(data.gun).getFuelConfig(ammoId).isMandatory());
        forEachAttachment(data, (modifier -> finalValue.set(modifier.modifyIsFuelMandatory(ammoId, finalValue.get(), data))));
        return finalValue.get();
    }

    public static int getFuelAmountPerUse(ResourceLocation ammoId, GunData data) {
        var finalValue = new AtomicReference<>(getGun(data.gun).getFuelConfig(ammoId).getAmountPerUse());
        forEachAttachment(data, (modifier -> finalValue.set(modifier.modifyFuelAmountPerUse(ammoId, finalValue.get(), data))));
        return finalValue.get();
    }

    private static void forEachAttachment(GunData data, Consumer<IGunModifier> consumer){
        var gun = data.gun;
        var config = getGun(gun);
        var attachments = config.getModules().getAttachments();

        for (var attachmentType : attachments.keySet()) {
            var attachmentItem = GunStateHelper.getAttachmentItem(attachmentType, gun);
            data.attachment = attachmentItem;
            var modifiers = getAttachmentModifiers(attachmentItem);

            for (var modifier : modifiers) {
                consumer.accept(modifier);
            }
//            applyModifiers(consumer, modifiers);
        }

        var gunItem = (WeaponItem) gun.getItem();

        for (var modifier : gunItem.getGunModifiers()) {
            consumer.accept(modifier);
        }
//        applyModifiers(consumer, gunItem.getGunModifiers());
    }

    private static IGunModifier[] getAttachmentModifiers(ItemStack attachmentItem) {
        if (!attachmentItem.isEmpty() && attachmentItem.getItem() instanceof IAttachment<?> attachment) {
            var modifiers = attachment.getProperties().getModifiers();
            var configModifiers = attachment.getAttachmentConfig().getModifiers();
            return ArrayUtils.add(modifiers, configModifiers);
        }
        return EMPTY;
    }
}
