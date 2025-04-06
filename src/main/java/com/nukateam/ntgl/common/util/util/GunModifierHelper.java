package com.nukateam.ntgl.common.util.util;

import com.nukateam.ntgl.common.data.config.Ammo;
import com.nukateam.ntgl.common.data.config.gun.Gun;
import com.nukateam.ntgl.common.data.config.gun.Modules;
import com.nukateam.ntgl.common.base.holders.*;
import com.nukateam.ntgl.common.data.constants.Tags;
import com.nukateam.ntgl.common.util.interfaces.IGunModifier;
import com.nukateam.ntgl.common.foundation.item.GunItem;
import com.nukateam.ntgl.common.data.attachment.IAttachment;
import com.nukateam.ntgl.common.foundation.item.interfaces.IAmmo;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.ArrayList;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;

/**
 * Author: MrCrayfish
 */
public class GunModifierHelper {
    private static final IGunModifier[] EMPTY = {};
    public static final String FIRE_MODE = "FireMode";
    public static final Ammo AMMO = new Ammo();

    public static boolean isAuto(ItemStack itemStack) {
        return getCurrentFireMode(itemStack) == FireMode.AUTO;
    }

    public static boolean isWeaponFull(ItemStack stack) {
        var tag = stack.getOrCreateTag();
        var gun = ((GunItem)stack.getItem()).getModifiedGun(stack);
        return tag.getInt(Tags.AMMO_COUNT) >= GunEnchantmentHelper.getAmmoCapacity(stack);
    }

    public static boolean canRenderInOffhand(Player player){
        var mainHandItem = player.getMainHandItem();
        var offhandItem = player.getOffhandItem();

        return isOneHanded(new GunData(mainHandItem, player))
                && isOneHanded(new GunData(offhandItem, player));
    }

    public static boolean isOneHanded(GunData data){
        if(data.stack.getItem() instanceof GunItem){
            return GunModifierHelper.getGripType(data).isOneHanded();
        }
        return true;
    }

    private static IGunModifier[] getAttachmentModifiers(ItemStack weapon, AttachmentType type) {
        var attachmentItem = Gun.getAttachmentItem(type, weapon);

        if (!attachmentItem.isEmpty() && attachmentItem.getItem() instanceof IAttachment<?> attachment) {
            var modifiers = attachment.getProperties().getModifiers();


            return modifiers;
        }
        return EMPTY;
    }

    public static boolean isGun(ItemStack weapon){
        var gunItem = weapon.getItem();
        return gunItem instanceof GunItem;
    }

    public static Gun getGun(ItemStack weapon) {
        var gunItem = (GunItem) weapon.getItem();
        return gunItem.getModifiedGun(weapon);
    }

    public static Map<AttachmentType, ArrayList<Modules.Attachment>> getGunAttachments(ItemStack weapon) {
        var gun = getGun(weapon);
        return gun.getModules().getAttachments();
    }

    public static int getMaxAmmo(ItemStack weapon) {
        var finalMaxAmmo = new AtomicInteger(getGun(weapon).getGeneral().getMaxAmmo());

        if (weapon != null && weapon.getItem() instanceof GunItem) {
            if (GunModifierHelper.getCurrentAmmo(weapon).isMagazineMode()) {
                var id = GunModifierHelper.getCurrentAmmoId(weapon);
                var item = ForgeRegistries.ITEMS.getValue(id);

                finalMaxAmmo.set(item.getMaxDamage(new ItemStack(item)));
            }
        }

        forEachAttachment(weapon, (modifier -> finalMaxAmmo.set(modifier.modifyMaxAmmo(finalMaxAmmo.get()))));
        return finalMaxAmmo.get();
    }

    public static boolean isAutoReloading(ItemStack weapon) {
        var autoReloading = new AtomicBoolean(getGun(weapon).getGeneral().isAutoReloading());
        forEachAttachment(weapon, (modifier -> autoReloading.set(modifier.modifyAutoReloading(autoReloading.get()))));
        return autoReloading.get();
    }

    public static LoadingType getLoadingType(ItemStack weapon) {
        var loadingType = new AtomicReference<>(getGun(weapon).getGeneral().getLoadingType());
        forEachAttachment(weapon, (modifier -> loadingType.set(modifier.modifyLoadingType(loadingType.get()))));
        return loadingType.get();
    }

    public static int getProjectileAmount(ItemStack weapon) {
        var gunProjectileAmount = getGun(weapon).getGeneral().getProjectileAmount();
        var ammoProjectileAmount = getCurrentAmmo(weapon).getProjectileAmount();

        var finalProjectileAmount = new AtomicInteger(gunProjectileAmount * ammoProjectileAmount);
        forEachAttachment(weapon, (modifier -> finalProjectileAmount.set(modifier.modifyProjectileAmount(finalProjectileAmount.get()))));
        return finalProjectileAmount.get();
    }

    public static void switchFireMode(ItemStack weapon){
        var fireModes = getFireModes(weapon);
        var current = getCurrentFireMode(weapon);
        var newFireMode = cycleSet(fireModes, current);
        setCurrentFireMode(weapon, newFireMode);
    }

    public static void setCurrentFireMode(ItemStack weapon, FireMode fireMode) {
        var tag = weapon.getOrCreateTag();
        tag.putString(FIRE_MODE, fireMode.toString());
        weapon.setTag(tag);
    }

    public static FireMode getCurrentFireMode(ItemStack weapon) {
        var tag = weapon.getOrCreateTag();
        if (!tag.contains(FIRE_MODE, Tag.TAG_STRING)) {
            var buff = new ArrayList<>(getFireModes(weapon).stream().toList());
            var fireMode = buff.get(0);

//            setCurrentFireMode(weapon, fireMode);
            return fireMode;
        }
        return FireMode.getType(tag.getString(FIRE_MODE));
    }

    public static Set<FireMode> getFireModes(ItemStack weapon) {
        var fireMode = getGun(weapon).getGeneral().getFireModes();
        var finalFireMode = new AtomicReference<>(fireMode);
        forEachAttachment(weapon, (modifier -> finalFireMode.set(modifier.modifyFireModes(finalFireMode.get()))));
        return finalFireMode.get();
    }

    public static GripType getGripType(GunData weapon) {
        var gripType = getGun(weapon.stack).getGeneral().getGripType();
        var finalGripType = new AtomicReference<>(gripType);
        forEachAttachment(weapon.stack, (modifier -> finalGripType.set(modifier.modifyGripType(finalGripType.get(), null))));
        return finalGripType.get();
    }

    public static int getFireDelay(ItemStack weapon) {
        var chargeTime = new AtomicInteger(getGun(weapon).getGeneral().getFireDelay());
        forEachAttachment(weapon, (modifier -> chargeTime.set(modifier.modifyFireDelay(chargeTime.get()))));
        return chargeTime.get();
    }

    public static boolean needsFullCharge(ItemStack weapon) {
        var needsFullCharge = new AtomicBoolean(getGun(weapon).getGeneral().isFullCharge());
        forEachAttachment(weapon, (modifier -> needsFullCharge.set(modifier.modifyNeedsFullCharge(needsFullCharge.get()))));
        return needsFullCharge.get();
    }

    public static boolean isOneTimeCharge(ItemStack weapon) {
        var oneTimeCharge = new AtomicBoolean(getGun(weapon).getGeneral().isOneTimeCharge());
        forEachAttachment(weapon, (modifier -> oneTimeCharge.set(modifier.modifyIsOneTimeCharge(oneTimeCharge.get()))));
        return oneTimeCharge.get();
    }

    public static void switchAmmo(ItemStack weapon){
        var ammoItems = getAmmoItems(weapon);
        var current = getCurrentAmmoId(weapon);
        var newAmmo = cycleSet(ammoItems, current);

        setCurrentAmmo(weapon, newAmmo);
    }

    public static void setCurrentAmmo(ItemStack weapon, ResourceLocation ammo) {
        var tag = weapon.getOrCreateTag();
        tag.putString("Ammo", ammo.toString());
        weapon.setTag(tag);
    }

    public static ResourceLocation getCurrentAmmoId(ItemStack weapon) {
        var tag = weapon.getOrCreateTag();
        if (tag.contains("Ammo", Tag.TAG_STRING)) {
            return ResourceLocation.tryParse(tag.getString("Ammo"));
        }
        return getFirstAmmoItem(weapon);
    }

    public static Item getCurrentAmmoItem(ItemStack weapon) {
        return ForgeRegistries.ITEMS.getValue(getCurrentAmmoId(weapon));
    }

    public static AmmoType getCurrentAmmoType(ItemStack weapon) {
        var ammo = getCurrentAmmo(weapon);
        return ammo.getType();
    }

    public static Ammo getCurrentAmmo(ItemStack weapon) {
        var gun = getGun(weapon);
        var ammoId = getCurrentAmmoId(weapon);

        if(gun.hasAmmo(ammoId)) {
            return gun.getAmmo(ammoId);
        }
        else if(getCurrentAmmoItem(weapon) instanceof IAmmo ammo) {
            return ammo.getAmmo();
        }
        else return AMMO;
    }

    public static Set<ResourceLocation> getAmmoItems(ItemStack weapon) {
        var items = getGun(weapon).getGeneral().getAmmo();
        var ammoItem = new AtomicReference<>(items);
        forEachAttachment(weapon, (modifier -> ammoItem.set(modifier.modifyAmmoItems(ammoItem.get()))));
        return ammoItem.get();
    }

    public static ResourceLocation getFirstAmmoItem(ItemStack weapon) {
        var items = getAmmoItems(weapon);
        return items.iterator().next();
    }

    public static int getReloadStart(ItemStack weapon) {
        var reloadTime = getGun(weapon).getGeneral().getReloadStart();
        var finalReloadTime = new AtomicInteger(reloadTime);
        forEachAttachment(weapon, (modifier -> finalReloadTime.set(modifier.modifyReloadStart(finalReloadTime.get()))));
        return finalReloadTime.get();
    }

    public static int getReloadTime(ItemStack weapon) {
        var reloadTime = getGun(weapon).getGeneral().getReloadTime();
        var finalReloadTime = new AtomicInteger(reloadTime);
        forEachAttachment(weapon, (modifier -> finalReloadTime.set(modifier.modifyReloadTime(finalReloadTime.get()))));
        return finalReloadTime.get();
    }

    public static int getReloadEnd(ItemStack weapon) {
        var reloadTime = getGun(weapon).getGeneral().getReloadEnd();
        var finalReloadTime = new AtomicInteger(reloadTime);
        forEachAttachment(weapon, (modifier -> finalReloadTime.set(modifier.modifyReloadEnd(finalReloadTime.get()))));
        return finalReloadTime.get();
    }

    public static int getModifiedProjectileLife(ItemStack weapon, int life) {
        var finalLife = new AtomicInteger(life);
        forEachAttachment(weapon, (modifier -> finalLife.set(modifier.modifyProjectileLife(finalLife.get()))));
        return finalLife.get();
    }

    public static double getModifiedProjectileGravity(ItemStack weapon, double gravity) {
        var finalGravity = new AtomicReference<>(gravity);
        forEachAttachment(weapon, (modifier -> finalGravity.set(modifier.modifyProjectileGravity(finalGravity.get()))));
        forEachAttachment(weapon, (modifier -> finalGravity.updateAndGet(v -> v + modifier.additionalProjectileGravity())));
        return finalGravity.get();
    }

    public static float getModifiedSpread(ItemStack weapon) {
        var gunSpread = getGun(weapon).getGeneral().getSpread();
        var ammoSpread = getCurrentAmmo(weapon).getSpread();
        var spread = Math.max(gunSpread + ammoSpread, 0);
        var finalSpread = new AtomicReference<>(spread);

        forEachAttachment(weapon, (modifier -> finalSpread.set(modifier.modifyProjectileSpread(finalSpread.get()))));
        return finalSpread.get();
    }

    public static double getModifiedProjectileSpeed(ItemStack weapon, double speed) {
        var finalSpeed = new AtomicReference<>(speed);
        forEachAttachment(weapon, (modifier -> finalSpeed.set(modifier.modifyProjectileSpeed(finalSpeed.get()))));
        return finalSpeed.get();
    }

    public static float getFireSoundVolume(ItemStack weapon) {
        var volume = new AtomicReference<>(1.0F);
        forEachAttachment(weapon, (modifier -> volume.set(modifier.modifyFireSoundVolume(volume.get()))));
        return Mth.clamp(volume.get(), 0.0F, 16.0F);
    }

    @Deprecated(since = "1.3.0", forRemoval = true)
    public static double getMuzzleFlashSize(ItemStack weapon, double size) {
        var finalSize = new AtomicReference<>(size);
        forEachAttachment(weapon, (modifier -> finalSize.set(modifier.modifyMuzzleFlashSize(finalSize.get()))));
        return finalSize.get();
    }

    public static double getMuzzleFlashScale(ItemStack weapon, double scale) {
        var finalScale = new AtomicReference<>(scale);
        forEachAttachment(weapon, (modifier -> finalScale.set(modifier.modifyMuzzleFlashScale(scale))));
        return finalScale.get();
    }

    public static float getKickReduction(ItemStack weapon) {
        var kickReduction = new AtomicReference<>(1.0F);
        forEachAttachment(weapon, (modifier -> kickReduction.updateAndGet(v -> v * Mth.clamp(modifier.kickModifier(), 0.0F, 1.0F))));
        return 1.0F - kickReduction.get();
    }

    public static float getRecoilModifier(ItemStack weapon) {
        var recoilReduction = new AtomicReference<>(1.0F);
        forEachAttachment(weapon, (modifier -> recoilReduction.updateAndGet(
                v -> v * Mth.clamp(modifier.recoilModifier(), 0.0F, 1.0F))));

        return 1.0F - recoilReduction.get();
    }

    public static boolean isSilencedFire(ItemStack weapon) {
        var gun = getGun(weapon);
        var attachments = gun.getModules().getAttachments();

        for (var att : attachments.keySet()) {
            var modifiers = getAttachmentModifiers(weapon, att);
            for (var modifier : modifiers) {
                if (modifier.silencedFire())
                    return true;
            }
        }
        return false;
    }

    public static double getModifiedFireSoundRadius(ItemStack weapon, double radius) {
        var minRadius = new AtomicReference<>(radius);
        forEachAttachment(weapon, (modifier -> {
            var newRadius = modifier.modifyFireSoundRadius(radius);
            if (newRadius < minRadius.get()) {
                minRadius.set(newRadius);
            }
        }));
        return Mth.clamp(minRadius.get(), 0.0, Double.MAX_VALUE);
    }

    public static float getAdditionalDamage(ItemStack weapon) {
        var additionalDamage = new AtomicReference<>(0.0F);
        forEachAttachment(weapon, (modifier -> additionalDamage.updateAndGet(
                v -> v + modifier.additionalDamage())));
        return additionalDamage.get();
    }

    public static float getModifiedProjectileDamage(ItemStack weapon, float damage) {
        var finalDamage = new AtomicReference<>(damage);
        forEachAttachment(weapon, (modifier -> finalDamage.set(modifier.modifyDamage(finalDamage.get()))));
        return finalDamage.get();
    }

    public static float getModifiedDamage(ItemStack weapon) {
        var gun = getGun(weapon);
        var damage = gun.getGeneral().getDamage();
        damage *= getAmmoDamageMultiplier(weapon);
        var finalDamage = new AtomicReference<>(damage);
        forEachAttachment(weapon, (modifier -> finalDamage.set(modifier.modifyDamage(finalDamage.get()))));
        forEachAttachment(weapon, (modifier -> finalDamage.updateAndGet(v -> v + modifier.additionalDamage())));

        return finalDamage.get();
    }

    public static float getAmmoDamageMultiplier(ItemStack weapon){
        return getCurrentAmmo(weapon).getDamage();
    }

    public static double getModifiedAimDownSightSpeed(ItemStack weapon, double speed) {
        var buffSpeed = new AtomicReference<>(speed);

        forEachAttachment(weapon, (modifier ->
                buffSpeed.set(modifier.modifyAimDownSightSpeed(buffSpeed.get()))));

        return Mth.clamp(buffSpeed.get(), 0.01, Double.MAX_VALUE);
    }

    public static int getRate(ItemStack weapon) {
        var gun = getGun(weapon);
        var rate = GunEnchantmentHelper.getRate(weapon, gun);
        var buffRate = new AtomicInteger(rate);
        forEachAttachment(weapon, (modifier -> buffRate.set(modifier.modifyFireRate(buffRate.get()))));
        return Mth.clamp(buffRate.get(), 0, Integer.MAX_VALUE);
    }

    public static float getCriticalChance(ItemStack weapon) {
        var chance = new AtomicReference<>(0F);
        forEachAttachment(weapon, (modifier ->
                chance.updateAndGet(v -> v + modifier.criticalChance())));
        chance.updateAndGet(v -> v + GunEnchantmentHelper.getPuncturingChance(weapon));

        return Mth.clamp(chance.get(), 0F, 1F);
    }

    ///PRIVATE
    private static void forEachAttachment(ItemStack weapon, Consumer<IGunModifier> consumer){
        var gun = getGun(weapon);
        var attachments = gun.getModules().getAttachments();

        for (var att : attachments.keySet()) {
            var modifiers = getAttachmentModifiers(weapon, att);
            applyModifiers(consumer, modifiers);
        }

        applyDynamicModifier(weapon, consumer);
    }

    private static void applyDynamicModifier(ItemStack weapon, Consumer<IGunModifier> consumer) {
        var gunItem = (GunItem) weapon.getItem();
        var dynamicModifier = gunItem.getGunModifier(weapon);

        if(dynamicModifier != null) {
            applyModifiers(consumer, dynamicModifier);
        }
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
