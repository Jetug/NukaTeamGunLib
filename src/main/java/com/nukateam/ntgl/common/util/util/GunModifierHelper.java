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

    public static boolean isAuto(GunData itemStack) {
        return getCurrentFireMode(itemStack) == FireMode.AUTO;
    }

    public static boolean isWeaponFull(GunData data) {
        var tag = data.stack.getOrCreateTag();
        var gun = ((GunItem)data.stack.getItem()).getModifiedGun(data.stack);
        return tag.getInt(Tags.AMMO_COUNT) >= GunEnchantmentHelper.getAmmoCapacity(data);
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

    private static IGunModifier[] getAttachmentModifiers(GunData weapon, AttachmentType type) {
        var attachmentItem = Gun.getAttachmentItem(type, weapon.stack);

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

    public static int getMaxAmmo(GunData weapon) {
        var finalMaxAmmo = new AtomicInteger(getGun(weapon.stack).getGeneral().getMaxAmmo());

        if (weapon != null && weapon.stack.getItem() instanceof GunItem) {
            if (GunModifierHelper.getCurrentAmmo(weapon).isMagazineMode()) {
                var id = GunModifierHelper.getCurrentAmmoId(weapon);
                var item = ForgeRegistries.ITEMS.getValue(id);

                finalMaxAmmo.set(item.getMaxDamage(new ItemStack(item)));
            }
        }

        forEachAttachment(weapon, (modifier -> finalMaxAmmo.set(modifier.modifyMaxAmmo(finalMaxAmmo.get()))));
        return finalMaxAmmo.get();
    }

    public static boolean isAutoReloading(GunData weapon) {
        var autoReloading = new AtomicBoolean(getGun(weapon.stack).getGeneral().isAutoReloading());
        forEachAttachment(weapon, (modifier -> autoReloading.set(modifier.modifyAutoReloading(autoReloading.get()))));
        return autoReloading.get();
    }

    public static LoadingType getLoadingType(GunData weapon) {
        var loadingType = new AtomicReference<>(getGun(weapon.stack).getGeneral().getLoadingType());
        forEachAttachment(weapon, (modifier -> loadingType.set(modifier.modifyLoadingType(loadingType.get()))));
        return loadingType.get();
    }

    public static int getProjectileAmount(GunData weapon) {
        var gunProjectileAmount = getGun(weapon.stack).getGeneral().getProjectileAmount();
        var ammoProjectileAmount = getCurrentAmmo(weapon).getProjectileAmount();

        var finalProjectileAmount = new AtomicInteger(gunProjectileAmount * ammoProjectileAmount);
        forEachAttachment(weapon, (modifier -> finalProjectileAmount.set(modifier.modifyProjectileAmount(finalProjectileAmount.get()))));
        return finalProjectileAmount.get();
    }

    public static void switchFireMode(GunData weapon){
        var fireModes = getFireModes(weapon);
        var current = getCurrentFireMode(weapon);
        var newFireMode = cycleSet(fireModes, current);
        setCurrentFireMode(weapon, newFireMode);
    }

    public static void setCurrentFireMode(GunData weapon, FireMode fireMode) {
        var tag = weapon.stack.getOrCreateTag();
        tag.putString(FIRE_MODE, fireMode.toString());
        weapon.stack.setTag(tag);
    }

    public static FireMode getCurrentFireMode(GunData weapon) {
        var tag = weapon.stack.getOrCreateTag();
        if (!tag.contains(FIRE_MODE, Tag.TAG_STRING)) {
            var buff = new ArrayList<>(getFireModes(weapon).stream().toList());
            var fireMode = buff.get(0);

//            setCurrentFireMode(weapon, fireMode);
            return fireMode;
        }
        return FireMode.getType(tag.getString(FIRE_MODE));
    }

    public static Set<FireMode> getFireModes(GunData weapon) {
        var fireMode = getGun(weapon.stack).getGeneral().getFireModes();
        var finalFireMode = new AtomicReference<>(fireMode);
        forEachAttachment(weapon, (modifier -> finalFireMode.set(modifier.modifyFireModes(finalFireMode.get()))));
        return finalFireMode.get();
    }

    public static GripType getGripType(GunData weapon) {
        var gripType = getGun(weapon.stack).getGeneral().getGripType();
        var finalGripType = new AtomicReference<>(gripType);
        forEachAttachment(weapon, (modifier -> finalGripType.set(modifier.modifyGripType(finalGripType.get(), weapon))));
        return finalGripType.get();
    }

    public static int getFireDelay(GunData weapon) {
        var chargeTime = new AtomicInteger(getGun(weapon.stack).getGeneral().getFireDelay());
        forEachAttachment(weapon, (modifier -> chargeTime.set(modifier.modifyFireDelay(chargeTime.get()))));
        return chargeTime.get();
    }

    public static boolean needsFullCharge(GunData weapon) {
        var needsFullCharge = new AtomicBoolean(getGun(weapon.stack).getGeneral().isFullCharge());
        forEachAttachment(weapon, (modifier -> needsFullCharge.set(modifier.modifyNeedsFullCharge(needsFullCharge.get()))));
        return needsFullCharge.get();
    }

    public static boolean isOneTimeCharge(GunData weapon) {
        var oneTimeCharge = new AtomicBoolean(getGun(weapon.stack).getGeneral().isOneTimeCharge());
        forEachAttachment(weapon, (modifier -> oneTimeCharge.set(modifier.modifyIsOneTimeCharge(oneTimeCharge.get()))));
        return oneTimeCharge.get();
    }

    public static void switchAmmo(GunData weapon){
        var ammoItems = getAmmoItems(weapon);
        var current = getCurrentAmmoId(weapon);
        var newAmmo = cycleSet(ammoItems, current);

        setCurrentAmmo(weapon, newAmmo);
    }

    public static void setCurrentAmmo(GunData weapon, ResourceLocation ammo) {
        var tag = weapon.stack.getOrCreateTag();
        tag.putString("Ammo", ammo.toString());
        weapon.stack.setTag(tag);
    }

    public static ResourceLocation getCurrentAmmoId(GunData weapon) {
        var tag = weapon.stack.getOrCreateTag();
        if (tag.contains("Ammo", Tag.TAG_STRING)) {
            return ResourceLocation.tryParse(tag.getString("Ammo"));
        }
        return getFirstAmmoItem(weapon);
    }

    public static Item getCurrentAmmoItem(GunData weapon) {
        return ForgeRegistries.ITEMS.getValue(getCurrentAmmoId(weapon));
    }

    public static AmmoType getCurrentAmmoType(GunData weapon) {
        var ammo = getCurrentAmmo(weapon);
        return ammo.getType();
    }

    public static Ammo getCurrentAmmo(GunData weapon) {
        var gun = getGun(weapon.stack);
        var ammoId = getCurrentAmmoId(weapon);

        if(gun.hasAmmo(ammoId)) {
            return gun.getAmmo(ammoId);
        }
        else if(getCurrentAmmoItem(weapon) instanceof IAmmo ammo) {
            return ammo.getAmmo();
        }
        else return AMMO;
    }

    public static Set<ResourceLocation> getAmmoItems(GunData weapon) {
        var items = getGun(weapon.stack).getGeneral().getAmmo();
        var ammoItem = new AtomicReference<>(items);
        forEachAttachment(weapon, (modifier -> ammoItem.set(modifier.modifyAmmoItems(ammoItem.get()))));
        return ammoItem.get();
    }

    public static ResourceLocation getFirstAmmoItem(GunData weapon) {
        var items = getAmmoItems(weapon);
        return items.iterator().next();
    }

    public static int getReloadStart(GunData weapon) {
        var reloadTime = getGun(weapon.stack).getGeneral().getReloadStart();
        var finalReloadTime = new AtomicInteger(reloadTime);
        forEachAttachment(weapon, (modifier -> finalReloadTime.set(modifier.modifyReloadStart(finalReloadTime.get()))));
        return finalReloadTime.get();
    }

    public static int getReloadTime(GunData weapon) {
        var reloadTime = getGun(weapon.stack).getGeneral().getReloadTime();
        var finalReloadTime = new AtomicInteger(reloadTime);
        forEachAttachment(weapon, (modifier -> finalReloadTime.set(modifier.modifyReloadTime(finalReloadTime.get()))));
        return finalReloadTime.get();
    }

    public static int getReloadEnd(GunData weapon) {
        var reloadTime = getGun(weapon.stack).getGeneral().getReloadEnd();
        var finalReloadTime = new AtomicInteger(reloadTime);
        forEachAttachment(weapon, (modifier -> finalReloadTime.set(modifier.modifyReloadEnd(finalReloadTime.get()))));
        return finalReloadTime.get();
    }

    public static int getModifiedProjectileLife(GunData weapon, int life) {
        var finalLife = new AtomicInteger(life);
        forEachAttachment(weapon, (modifier -> finalLife.set(modifier.modifyProjectileLife(finalLife.get()))));
        return finalLife.get();
    }

    public static double getModifiedProjectileGravity(GunData weapon, double gravity) {
        var finalGravity = new AtomicReference<>(gravity);
        forEachAttachment(weapon, (modifier -> finalGravity.set(modifier.modifyProjectileGravity(finalGravity.get()))));
        forEachAttachment(weapon, (modifier -> finalGravity.updateAndGet(v -> v + modifier.additionalProjectileGravity())));
        return finalGravity.get();
    }

    public static float getModifiedSpread(GunData weapon) {
        var gunSpread = getGun(weapon.stack).getGeneral().getSpread();
        var ammoSpread = getCurrentAmmo(weapon).getSpread();
        var spread = Math.max(gunSpread + ammoSpread, 0);
        var finalSpread = new AtomicReference<>(spread);

        forEachAttachment(weapon, (modifier -> finalSpread.set(modifier.modifyProjectileSpread(finalSpread.get()))));
        return finalSpread.get();
    }

    public static double getModifiedProjectileSpeed(GunData weapon, double speed) {
        var finalSpeed = new AtomicReference<>(speed);
        forEachAttachment(weapon, (modifier -> finalSpeed.set(modifier.modifyProjectileSpeed(finalSpeed.get()))));
        return finalSpeed.get();
    }

    public static float getFireSoundVolume(GunData weapon) {
        var volume = new AtomicReference<>(1.0F);
        forEachAttachment(weapon, (modifier -> volume.set(modifier.modifyFireSoundVolume(volume.get()))));
        return Mth.clamp(volume.get(), 0.0F, 16.0F);
    }

    @Deprecated(since = "1.3.0", forRemoval = true)
    public static double getMuzzleFlashSize(GunData weapon, double size) {
        var finalSize = new AtomicReference<>(size);
        forEachAttachment(weapon, (modifier -> finalSize.set(modifier.modifyMuzzleFlashSize(finalSize.get()))));
        return finalSize.get();
    }

    public static double getMuzzleFlashScale(GunData weapon, double scale) {
        var finalScale = new AtomicReference<>(scale);
        forEachAttachment(weapon, (modifier -> finalScale.set(modifier.modifyMuzzleFlashScale(scale))));
        return finalScale.get();
    }

    public static float getKickReduction(GunData weapon) {
        var kickReduction = new AtomicReference<>(1.0F);
        forEachAttachment(weapon, (modifier -> kickReduction.updateAndGet(v -> v * Mth.clamp(modifier.kickModifier(), 0.0F, 1.0F))));
        return 1.0F - kickReduction.get();
    }

    public static float getRecoilModifier(GunData weapon) {
        var recoilReduction = new AtomicReference<>(1.0F);
        forEachAttachment(weapon, (modifier -> recoilReduction.updateAndGet(
                v -> v * Mth.clamp(modifier.recoilModifier(), 0.0F, 1.0F))));

        return 1.0F - recoilReduction.get();
    }

    public static boolean isSilencedFire(GunData weapon) {
        var gun = getGun(weapon.stack);
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

    public static double getModifiedFireSoundRadius(GunData weapon, double radius) {
        var minRadius = new AtomicReference<>(radius);
        forEachAttachment(weapon, (modifier -> {
            var newRadius = modifier.modifyFireSoundRadius(radius);
            if (newRadius < minRadius.get()) {
                minRadius.set(newRadius);
            }
        }));
        return Mth.clamp(minRadius.get(), 0.0, Double.MAX_VALUE);
    }

    public static float getAdditionalDamage(GunData weapon) {
        var additionalDamage = new AtomicReference<>(0.0F);
        forEachAttachment(weapon, (modifier -> additionalDamage.updateAndGet(
                v -> v + modifier.additionalDamage())));
        return additionalDamage.get();
    }

    public static float getModifiedProjectileDamage(GunData weapon, float damage) {
        var finalDamage = new AtomicReference<>(damage);
        forEachAttachment(weapon, (modifier -> finalDamage.set(modifier.modifyDamage(finalDamage.get()))));
        return finalDamage.get();
    }

    public static float getModifiedDamage(GunData weapon) {
        var gun = getGun(weapon.stack);
        var damage = gun.getGeneral().getDamage();
        damage *= getAmmoDamageMultiplier(weapon);
        var finalDamage = new AtomicReference<>(damage);
        forEachAttachment(weapon, (modifier -> finalDamage.set(modifier.modifyDamage(finalDamage.get()))));
        forEachAttachment(weapon, (modifier -> finalDamage.updateAndGet(v -> v + modifier.additionalDamage())));

        return finalDamage.get();
    }

    public static float getAmmoDamageMultiplier(GunData weapon){
        return getCurrentAmmo(weapon).getDamage();
    }

    public static double getModifiedAimDownSightSpeed(GunData weapon, double speed) {
        var buffSpeed = new AtomicReference<>(speed);

        forEachAttachment(weapon, (modifier ->
                buffSpeed.set(modifier.modifyAimDownSightSpeed(buffSpeed.get()))));

        return Mth.clamp(buffSpeed.get(), 0.01, Double.MAX_VALUE);
    }

    public static int getRate(GunData weapon) {
        var gun = getGun(weapon.stack);
        var rate = GunEnchantmentHelper.getRate(weapon.stack, gun);
        var buffRate = new AtomicInteger(rate);
        forEachAttachment(weapon, (modifier -> buffRate.set(modifier.modifyFireRate(buffRate.get()))));
        return Mth.clamp(buffRate.get(), 0, Integer.MAX_VALUE);
    }

    public static float getCriticalChance(GunData weapon) {
        var chance = new AtomicReference<>(0F);
        forEachAttachment(weapon, (modifier ->
                chance.updateAndGet(v -> v + modifier.criticalChance())));
        chance.updateAndGet(v -> v + GunEnchantmentHelper.getPuncturingChance(weapon.stack));

        return Mth.clamp(chance.get(), 0F, 1F);
    }

    ///PRIVATE
    private static void forEachAttachment(GunData weapon, Consumer<IGunModifier> consumer){
        var gun = getGun(weapon.stack);
        var attachments = gun.getModules().getAttachments();

        for (var att : attachments.keySet()) {
            var modifiers = getAttachmentModifiers(weapon, att);
            applyModifiers(consumer, modifiers);
        }

        applyDynamicModifier(weapon, consumer);
    }

    private static void applyDynamicModifier(GunData weapon, Consumer<IGunModifier> consumer) {
        var gunItem = (GunItem) weapon.stack.getItem();
        var dynamicModifier = gunItem.getGunModifier(weapon.stack);

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
