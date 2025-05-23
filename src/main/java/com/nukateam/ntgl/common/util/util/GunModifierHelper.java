package com.nukateam.ntgl.common.util.util;

import com.nukateam.ntgl.common.data.config.Ammo;
import com.nukateam.ntgl.common.data.config.gun.General;
import com.nukateam.ntgl.common.data.config.gun.Gun;
import com.nukateam.ntgl.common.data.config.gun.Modules;
import com.nukateam.ntgl.common.base.holders.*;
import com.nukateam.ntgl.common.data.constants.Tags;
import com.nukateam.ntgl.common.util.interfaces.IGunModifier;
import com.nukateam.ntgl.common.foundation.item.GunItem;
import com.nukateam.ntgl.common.data.attachment.IAttachment;
import com.nukateam.ntgl.common.foundation.item.interfaces.IAmmo;
import com.nukateam.ntgl.modules.enchantment.GunEnchantmentHelper;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Map;
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
    public static final Ammo AMMO = new Ammo();

    public static boolean isAuto(GunData itemStack) {
        return getCurrentFireMode(itemStack) == FireMode.AUTO;
    }

    public static boolean isWeaponFull(GunData data) {
        var tag = data.gun.getOrCreateTag();
        var gun = ((GunItem)data.gun.getItem()).getModifiedGun(data.gun);
        return tag.getInt(Tags.AMMO_COUNT) >= GunEnchantmentHelper.getAmmoCapacity(data);
    }

    public static boolean canRenderInOffhand(LivingEntity player){
        var mainHandItem = player.getMainHandItem();
        var offhandItem = player.getOffhandItem();

        return isOneHanded(new GunData(mainHandItem, player))
                && isOneHanded(new GunData(offhandItem, player));
    }

    public static boolean isOneHanded(GunData data){
        if(data.gun.getItem() instanceof GunItem){
            return GunModifierHelper.getGripType(data).isOneHanded();
        }
        return true;
    }

    public static boolean isGun(ItemStack data){
        var gunItem = data.getItem();
        return gunItem instanceof GunItem;
    }

    public static Gun getGun(ItemStack stack) {
        var gunItem = (GunItem) stack.getItem();
        return gunItem.getModifiedGun(stack);
    }

    private static General getGeneral(Gun gun) {
        return gun.getGeneral();
    }

    public static Map<AttachmentType, ArrayList<Modules.Attachment>> getAttachmentTypes(GunData data) {
        var gun = getGun(data.gun);
        return gun.getModules().getAttachments();
    }

    public static ArrayList<AttachmentType> getSortedAttachmentTypes(GunData data) {
        var attachments = getAttachmentTypes(data);
        var sortedTypes = new ArrayList<>(attachments.keySet());
        sortedTypes.sort(Comparator.comparing(AttachmentType::toString));
        return sortedTypes;
    }

    public static int getMaxAmmo(GunData data) {
        var finalMaxAmmo = new AtomicInteger(getGeneral(getGun(data.gun)).getMaxAmmo());

        if (data != null && data.gun.getItem() instanceof GunItem) {
            if (GunModifierHelper.getCurrentAmmo(data).isMagazineMode()) {
                var id = GunModifierHelper.getCurrentAmmoId(data);
                var item = ITEMS.getValue(id);

                finalMaxAmmo.set(item.getMaxDamage(new ItemStack(item)));
            }
        }

        forEachAttachment(data, (modifier -> finalMaxAmmo.set(modifier.modifyMaxAmmo(finalMaxAmmo.get(),  data))));
        return finalMaxAmmo.get();
    }

    public static boolean isAutoReloading(GunData data) {
        var autoReloading = new AtomicBoolean(getGeneral(getGun(data.gun)).isAutoReloading());
        forEachAttachment(data, (modifier -> autoReloading.set(modifier.modifyAutoReloading(autoReloading.get(), data))));
        return autoReloading.get();
    }

    public static ResourceLocation getFireSound(GunData data) {
        var fireSound = new AtomicReference<>(getGun(data.gun).getSounds().getFire());
        forEachAttachment(data, (modifier -> fireSound.set(modifier.modifyFireSound(fireSound.get(), data))));
        return fireSound.get();
    }

    public static boolean shouldRenderHud(GunData data) {
        var renderHud = new AtomicBoolean(getGeneral(getGun(data.gun)).shouldRenderHud());
        forEachAttachment(data, (modifier -> renderHud.set(modifier.modifyShouldRenderHud(renderHud.get(), data))));
        return renderHud.get();
    }

    public static LoadingType getLoadingType(GunData data) {
        var loadingType = new AtomicReference<>(getGeneral(getGun(data.gun)).getLoadingType());
        forEachAttachment(data, (modifier -> loadingType.set(modifier.modifyLoadingType(loadingType.get(), data))));
        return loadingType.get();
    }

    public static int getProjectileAmount(GunData data) {
        var gunProjectileAmount = getGeneral(getGun(data.gun)).getProjectileAmount();
        var ammoProjectileAmount = getCurrentAmmo(data).getProjectileAmount();

        var finalProjectileAmount = new AtomicInteger(gunProjectileAmount * ammoProjectileAmount);
        forEachAttachment(data, (modifier -> finalProjectileAmount.set(modifier.modifyProjectileAmount(finalProjectileAmount.get(), data))));
        return finalProjectileAmount.get();
    }

    public static void switchFireMode(GunData data){
        var fireModes = getFireModes(data);
        var current = getCurrentFireMode(data);
        var newFireMode = cycleSet(fireModes, current);
        setCurrentFireMode(data, newFireMode);
    }

    public static void setCurrentFireMode(GunData data, FireMode fireMode) {
        var tag = data.gun.getOrCreateTag();
        tag.putString(Tags.FIRE_MODE, fireMode.toString());
        data.gun.setTag(tag);
    }

    public static FireMode getCurrentFireMode(GunData data) {
        var tag = data.gun.getOrCreateTag();
        if (!tag.contains(Tags.FIRE_MODE, Tag.TAG_STRING)) {
            var buff = new ArrayList<>(getFireModes(data).stream().toList());
            var fireMode = buff.get(0);

//            setCurrentFireMode(data, fireMode);
            return fireMode;
        }
        return FireMode.getType(tag.getString(Tags.FIRE_MODE));
    }

    public static Set<FireMode> getFireModes(GunData data) {
        var fireMode = getGeneral(getGun(data.gun)).getFireModes();
        var finalFireMode = new AtomicReference<>(fireMode);
        forEachAttachment(data, (modifier -> finalFireMode.set(modifier.modifyFireModes(finalFireMode.get(), data))));
        return finalFireMode.get();
    }

    public static GripType getGripType(GunData data) {
        var gripType = getGeneral(getGun(data.gun)).getGripType();
        var finalGripType = new AtomicReference<>(gripType);
        forEachAttachment(data, (modifier -> finalGripType.set(modifier.modifyGripType(finalGripType.get(), data))));
        return finalGripType.get();
    }

    public static int getFireDelay(GunData data) {
        var chargeTime = new AtomicInteger(getGeneral(getGun(data.gun)).getFireDelay());
        forEachAttachment(data, (modifier -> chargeTime.set(modifier.modifyFireDelay(chargeTime.get(), data))));
        return chargeTime.get();
    }

    public static Set<FuelType> getFuelTypes(GunData data) {
        var keys = getGun(data.gun).getFuel().keySet();
        var fuel = new AtomicReference<>(keys);
        forEachAttachment(data, (modifier -> fuel.set(modifier.modifyFuel(fuel.get(), data))));
        return fuel.get();
    }

    public static Integer getMaxFuel(GunData data, FuelType type) {
        var fuel = getGun(data.gun).getFuel().get(type);
        int max = fuel != null ? fuel.getMax() : 0;
        var result = new AtomicReference<>(max);

        forEachAttachment(data, (modifier -> result.set(modifier.modifyMaxFuel(result.get(), type, data))));
        return result.get();
    }

    public static boolean needsFullCharge(GunData data) {
        var needsFullCharge = new AtomicBoolean(getGeneral(getGun(data.gun)).isFullCharge());
        forEachAttachment(data, (modifier -> needsFullCharge.set(modifier.modifyNeedsFullCharge(needsFullCharge.get(), data))));
        return needsFullCharge.get();
    }

    public static boolean isOneTimeCharge(GunData data) {
        var oneTimeCharge = new AtomicBoolean(getGeneral(getGun(data.gun)).isOneTimeCharge());
        forEachAttachment(data, (modifier -> oneTimeCharge.set(modifier.modifyIsOneTimeCharge(oneTimeCharge.get(), data))));
        return oneTimeCharge.get();
    }

    public static void switchAmmo(GunData data){
        var ammoItems = getAmmoItems(data);
        var current = getCurrentAmmoId(data);
        var newAmmo = cycleSet(ammoItems, current);

        setCurrentAmmo(data, newAmmo);
    }

    public static void setCurrentAmmo(GunData data, ResourceLocation ammo) {
        var tag = data.gun.getOrCreateTag();
        tag.putString("Ammo", ammo.toString());
        data.gun.setTag(tag);
    }

    public static ResourceLocation getCurrentAmmoId(GunData data) {
        var tag = data.gun.getOrCreateTag();
        if (tag.contains("Ammo", Tag.TAG_STRING)) {
            return ResourceLocation.tryParse(tag.getString("Ammo"));
        }
        return getFirstAmmoItem(data);
    }

    public static boolean isCurrentAmmo(GunData gunData, Item item) {
        return getCurrentAmmoId(gunData).equals(ITEMS.getKey(item));
    }

    public static Item getCurrentAmmoItem(GunData data) {
        return ITEMS.getValue(getCurrentAmmoId(data));
    }

    public static AmmoType getCurrentAmmoType(GunData data) {
        var ammo = getCurrentAmmo(data);
        return ammo.getType();
    }

    public static Ammo getCurrentAmmo(GunData data) {
        var gun = getGun(data.gun);
        var ammoId = getCurrentAmmoId(data);

        if(gun.hasAmmo(ammoId)) {
            return gun.getAmmo(ammoId);
        }
        else if(getCurrentAmmoItem(data) instanceof IAmmo ammo) {
            return ammo.getAmmo();
        }
        else return AMMO;
    }

    public static Set<ResourceLocation> getAmmoItems(GunData data) {
        var items = getGeneral(getGun(data.gun)).getAmmo();
        var ammoItem = new AtomicReference<>(items);
        forEachAttachment(data, (modifier -> ammoItem.set(modifier.modifyAmmoItems(ammoItem.get(), data))));
        return ammoItem.get();
    }

    public static ResourceLocation getFirstAmmoItem(GunData data) {
        var items = getAmmoItems(data);
        return items.iterator().next();
    }

    public static int getReloadStart(GunData data) {
        var reloadTime = getGeneral(getGun(data.gun)).getReloadStart();
        var finalReloadTime = new AtomicInteger(reloadTime);
        forEachAttachment(data, (modifier -> finalReloadTime.set(modifier.modifyReloadStart(finalReloadTime.get(), data))));
        return finalReloadTime.get();
    }

    public static int getReloadTime(GunData data) {
        var reloadTime = getGeneral(getGun(data.gun)).getReloadTime();
        var finalReloadTime = new AtomicInteger(reloadTime);
        forEachAttachment(data, (modifier -> finalReloadTime.set(modifier.modifyReloadTime(finalReloadTime.get(), data))));
        return finalReloadTime.get();
    }

    public static int getReloadEnd(GunData data) {
        var reloadTime = getGeneral(getGun(data.gun)).getReloadEnd();
        var finalReloadTime = new AtomicInteger(reloadTime);
        forEachAttachment(data, (modifier -> finalReloadTime.set(modifier.modifyReloadEnd(finalReloadTime.get(), data))));
        return finalReloadTime.get();
    }

    public static int getEquipTime(GunData data) {
        var equipTime = getGeneral(getGun(data.gun)).getEquipTime();
        var finalEquipTime = new AtomicInteger(equipTime);
        forEachAttachment(data, (modifier -> finalEquipTime.set(modifier.modifyEquipTime(finalEquipTime.get(), data))));
        return finalEquipTime.get();
    }

    public static int getAmmoPerShot(GunData data) {
        var equipTime = getGeneral(getGun(data.gun)).getAmmoPerShot();
        var finalEquipTime = new AtomicInteger(equipTime);
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
        var gunSpread = getGeneral(getGun(data.gun)).getSpread();
        var ammoSpread = getCurrentAmmo(data).getSpread();
        var spread = Math.max(gunSpread + ammoSpread, 0);
        var finalSpread = new AtomicReference<>(spread);

        forEachAttachment(data, (modifier -> finalSpread.set(modifier.modifyProjectileSpread(finalSpread.get(), data))));
        return finalSpread.get();
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
        var gun = getGun(data.gun);
        var attachments = gun.getModules().getAttachments();

        for (var attachmentType : attachments.keySet()) {
            var modifiers = getAttachmentModifiers(data.gun, attachmentType);
            for (var modifier : modifiers) {
                if (modifier.silencedFire(data))
                    return true;
            }
        }
        return false;
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
        var gun = getGun(data.gun);
        var damage = getGeneral(gun).getDamage();
        damage *= getAmmoDamageMultiplier(data);
        var finalDamage = new AtomicReference<>(damage);
        forEachAttachment(data, (modifier -> finalDamage.set(modifier.modifyDamage(finalDamage.get(), data))));
        forEachAttachment(data, (modifier -> finalDamage.updateAndGet(v -> v + modifier.additionalDamage(data))));

        return finalDamage.get();
    }

    public static float getAmmoDamageMultiplier(GunData data){
        return getCurrentAmmo(data).getDamage();
    }

    public static double getModifiedAimDownSightSpeed(GunData data, double speed) {
        var buffSpeed = new AtomicReference<>(speed);

        forEachAttachment(data, (modifier ->
                buffSpeed.set(modifier.modifyAimDownSightSpeed(buffSpeed.get(), data))));

        return Mth.clamp(buffSpeed.get(), 0.01, Double.MAX_VALUE);
    }

    public static int getRate(GunData data) {
        var gun = getGun(data.gun);
        var rate = GunEnchantmentHelper.getRate(data.gun, gun);
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

    ///PRIVATE______________
    private static void forEachAttachment(GunData data, Consumer<IGunModifier> consumer){
        var gun = data.gun;
        var config = getGun(gun);
        var attachments = config.getModules().getAttachments();

        for (var attachmentType : attachments.keySet()) {
            var modifiers = getAttachmentModifiers(gun, attachmentType);
            applyModifiers(consumer, modifiers);
        }

        var gunItem = (GunItem) gun.getItem();
        applyModifiers(consumer, gunItem.getGunModifiers());
    }

    private static IGunModifier[] getAttachmentModifiers(ItemStack gun, AttachmentType type) {
        var attachmentItem = Gun.getAttachmentItem(type, gun);

        if (!attachmentItem.isEmpty() && attachmentItem.getItem() instanceof IAttachment<?> attachment) {
            var modifiers = attachment.getProperties().getModifiers();
            return modifiers;
        }
        return EMPTY;
    }

    private static void applyModifiers(Consumer<IGunModifier> consumer, IGunModifier... gunModifiers) {
        for (var modifier : gunModifiers) {
            consumer.accept(modifier);
        }
    }

    private static <T> T cycleSet(Set<T> set, T value) {
        var buff = new ArrayList<>(set.stream().toList());
        var i = buff.indexOf(value);

        if(i == set.size() - 1)
            i = 0;
        else i++;

        return buff.get(i);
    }
}
