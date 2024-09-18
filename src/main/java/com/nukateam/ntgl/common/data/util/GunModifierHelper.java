package com.nukateam.ntgl.common.data.util;

import com.nukateam.ntgl.common.base.gun.AttachmentType;
import com.nukateam.ntgl.common.base.gun.FireMode;
import com.nukateam.ntgl.common.base.gun.GripType;
import com.nukateam.ntgl.common.base.gun.Gun;
import com.nukateam.ntgl.common.data.constants.Tags;
import com.nukateam.ntgl.common.data.interfaces.IGunModifier;
import com.nukateam.ntgl.common.foundation.item.GunItem;
import com.nukateam.ntgl.common.foundation.item.attachment.IAttachment;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;

/**
 * Author: MrCrayfish
 */
public class GunModifierHelper {
    private static final IGunModifier[] EMPTY = {};
    public static final String FIRE_MODE = "FireMode";

    public static boolean isOneHanded(ItemStack itemStack){
        var gunItem = (GunItem)itemStack.getItem();
        return GunModifierHelper.getGripType(itemStack) != GripType.ONE_HANDED;
    }

    public static boolean isWeaponFull(ItemStack stack) {
        var tag = stack.getOrCreateTag();
        var gun = ((GunItem)stack.getItem()).getModifiedGun(stack);
        return tag.getInt(Tags.AMMO_COUNT) >= GunEnchantmentHelper.getAmmoCapacity(stack);
    }

    public static boolean canRenderInOffhand(Player player){
        var mainHandItem = player.getMainHandItem();
        var offhandItem = player.getOffhandItem();
        return canRenderInOffhand(mainHandItem) && canRenderInOffhand(offhandItem);
    }

    public static boolean canRenderInOffhand(ItemStack stack){
        if(stack.getItem() instanceof GunItem){
            var animation = GunModifierHelper.getGripType(stack).getHeldAnimation();
            return animation.canRenderOffhandItem();
        }
        return true;
    }

    private static IGunModifier[] getModifiers(ItemStack weapon, AttachmentType type) {
        var stack = Gun.getAttachmentItem(type, weapon);
        if (!stack.isEmpty() && stack.getItem() instanceof IAttachment<?> attachment) {
            return attachment.getProperties().getModifiers();
        }
        return EMPTY;
    }

    public static Gun getGun(ItemStack weapon) {
        var gunItem = (GunItem) weapon.getItem();
        return gunItem.getModifiedGun(weapon);
    }

    public static Map<AttachmentType, ArrayList<Gun.Modules.Attachment>> getGunAttachments(ItemStack weapon) {
        var gun = getGun(weapon);
        return gun.getModules().getAttachments();
    }

    public static int getMaxAmmo(ItemStack weapon) {
        var finalMaxAmmo = new AtomicInteger(getGun(weapon).getGeneral().getMaxAmmo(weapon));
        forEachAttachment(weapon, (modifier -> finalMaxAmmo.set(modifier.modifyMaxAmmo(finalMaxAmmo.get()))));
        return finalMaxAmmo.get();
    }

    public static void switchFireMode(ItemStack weapon){
        var fireModes = getFireModes(weapon);
        var current = getCurrentFireMode(weapon);
        var buff = new ArrayList<>(fireModes.stream().toList());
        var i = buff.indexOf(current);

        if(i == buff.size() - 1)
            i = 0;
        else i++;

        setCurrentFireMode(weapon, (FireMode)buff.get(i));
    }

    public static void setCurrentFireMode(ItemStack weapon, FireMode fireMode) {
        var tag = weapon.getOrCreateTag();
        tag.putString(FIRE_MODE, fireMode.toString());
    }

    public static FireMode getCurrentFireMode(ItemStack weapon) {
        var tag = weapon.getOrCreateTag();
        if (!tag.contains(FIRE_MODE, Tag.TAG_STRING)) {
            var buff = new ArrayList<>(getFireModes(weapon).stream().toList());
            var fireMode = (FireMode) buff.get(0);

//            setCurrentFireMode(weapon, fireMode);
            return fireMode;
        }
        return FireMode.getType(tag.getString(FIRE_MODE));
    }

    public static boolean isAuto(ItemStack itemStack) {
        return getCurrentFireMode(itemStack) == FireMode.AUTO;
    }

    public static Set<FireMode> getFireModes(ItemStack weapon) {
        var fireMode = getGun(weapon).getGeneral().getFireModes();
        var finalFireMode = new AtomicReference<>(fireMode);
        forEachAttachment(weapon, (modifier -> finalFireMode.set(modifier.modifyFireModes(finalFireMode.get()))));
        return finalFireMode.get();
    }

    public static GripType getGripType(ItemStack weapon) {
        var gripType = getGun(weapon).getGeneral().getGripType();
        var finalGripType = new AtomicReference<>(gripType);
        forEachAttachment(weapon, (modifier -> finalGripType.set(modifier.modifyGripType(finalGripType.get()))));
        return finalGripType.get();
    }

    public static ResourceLocation getAmmoItem(ItemStack weapon) {
        var id = getGun(weapon).getProjectile().getItem();
        var ammoItem = new AtomicReference<>(id);
        forEachAttachment(weapon, (modifier -> ammoItem.set(modifier.modifyAmmoItem(ammoItem.get()))));
        return ammoItem.get();
    }

    public static int getReloadTime(ItemStack weapon) {
        var reloadTime = getGun(weapon).getGeneral().getReloadTime();
        var finalReloadTime = new AtomicInteger(reloadTime);
        forEachAttachment(weapon, (modifier -> finalReloadTime.set(modifier.modifyReloadTime(finalReloadTime.get()))));
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

    public static float getModifiedSpread(ItemStack weapon, float spread) {
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
            var modifiers = getModifiers(weapon, att);
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
        forEachAttachment(weapon, (modifier -> finalDamage.set(modifier.modifyProjectileDamage(finalDamage.get()))));
        return finalDamage.get();
    }

    public static float getModifiedDamage(ItemStack weapon, float damage) {
        var finalDamage = new AtomicReference<>(damage);
        forEachAttachment(weapon, (modifier -> finalDamage.set(modifier.modifyProjectileDamage(finalDamage.get()))));
        forEachAttachment(weapon, (modifier -> finalDamage.updateAndGet(v -> v + modifier.additionalDamage())));

        return finalDamage.get();
    }

    public static double getModifiedAimDownSightSpeed(ItemStack weapon, double speed) {
        var buffSpeed = new AtomicReference<>(speed);
        forEachAttachment(weapon, (modifier -> buffSpeed.set(
                modifier.modifyAimDownSightSpeed(buffSpeed.get()))));

        return Mth.clamp(speed, 0.01, Double.MAX_VALUE);
    }

    public static int getModifiedRate(ItemStack weapon, int rate) {
        var buffRate = new AtomicInteger(rate);
        forEachAttachment(weapon, (modifier -> buffRate.set(modifier.modifyFireRate(buffRate.get()))));
        return Mth.clamp(rate, 0, Integer.MAX_VALUE);
    }

    public static float getCriticalChance(ItemStack weapon) {
        var chance = new AtomicReference<>(0F);
        forEachAttachment(weapon, (modifier ->
                chance.updateAndGet(v -> v + modifier.criticalChance())));
        chance.updateAndGet(v -> v + GunEnchantmentHelper.getPuncturingChance(weapon));

        return Mth.clamp(chance.get(), 0F, 1F);
    }

    private static void forEachAttachment(ItemStack weapon, Consumer<IGunModifier> consumer){
        var gun = getGun(weapon);
        var attachments = gun.getModules().getAttachments();

        for (var att : attachments.keySet()) {
            var modifiers = getModifiers(weapon, att);
            for (var modifier : modifiers) {
                consumer.accept(modifier);
            }
        }
    }
}
