package com.nukateam.ntgl.common.data.util;

import com.nukateam.ntgl.common.base.gun.GripType;
import com.nukateam.ntgl.common.base.gun.Gun;
import com.nukateam.ntgl.common.data.constants.Tags;
import com.nukateam.ntgl.common.data.interfaces.IGunModifier;
import com.nukateam.ntgl.common.foundation.item.GunItem;
import com.nukateam.ntgl.common.foundation.item.attachment.IAttachment;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;

/**
 * Author: MrCrayfish
 */
public class GunModifierHelper {
    private static final IGunModifier[] EMPTY = {};

    public static boolean isOneHanded(ItemStack itemStack){
        var gunItem = (GunItem)itemStack.getItem();
        return gunItem.getModifiedGun(itemStack).getGeneral().getGripType() != GripType.ONE_HANDED;
    }

    public static boolean isWeaponFull(ItemStack stack) {
        var tag = stack.getOrCreateTag();
        var gun = ((GunItem)stack.getItem()).getModifiedGun(stack);
        return tag.getInt(Tags.AMMO_COUNT) >= GunEnchantmentHelper.getAmmoCapacity(stack, gun);
    }

    public static boolean canRenderInOffhand(Player player){
        var mainHandItem = player.getMainHandItem();
        var offhandItem = player.getOffhandItem();

        return canRenderInOffhand(mainHandItem) && canRenderInOffhand(offhandItem);
    }

    public static boolean canRenderInOffhand(ItemStack stack){
        if(stack.getItem() instanceof GunItem gunItem){
            var animation = gunItem.getModifiedGun(stack).getGeneral().getGripType().getHeldAnimation();
            return animation.canRenderOffhandItem();
        }
        return true;
    }

    private static IGunModifier[] getModifiers(ItemStack weapon, ResourceLocation type) {
        ItemStack stack = Gun.getAttachmentItem(type, weapon);
        if (!stack.isEmpty() && stack.getItem() instanceof IAttachment<?> attachment) {
            return attachment.getProperties().getModifiers();
        }
        return EMPTY;
    }

    public static Gun getGun(ItemStack weapon) {
        var gunItem = (GunItem) weapon.getItem();
        return gunItem.getModifiedGun(weapon);
    }

    public static Map<ResourceLocation, ArrayList<Gun.Modules.Attachment>> getGunAttachments(ItemStack weapon) {
        var gun = getGun(weapon);
        return gun.getModules().getAttachments();
    }

    public static int getModifiedProjectileLife(ItemStack weapon, int life) {
//        var gun = getGun(weapon);
//        var attachments = gun.getModules().getAttachments();
//
//        for (var att : attachments.keySet()) {
//            IGunModifier[] modifiers = getModifiers(weapon, att);
//            for (IGunModifier modifier : modifiers) {
//                life = modifier.modifyProjectileLife(life);
//            }
//        }

        var finalLife = new AtomicInteger(life);
        forEachAttachment(weapon, (modifier -> finalLife.set(modifier.modifyProjectileLife(finalLife.get()))));
        return finalLife.get();
    }

    public static double getModifiedProjectileGravity(ItemStack weapon, double gravity) {
//        var attachments = getGunAttachments(weapon);
//
//        for (var att : attachments.keySet()) {
//            IGunModifier[] modifiers = getModifiers(weapon, att);
//            for (IGunModifier modifier : modifiers) {
//                gravity = modifier.modifyProjectileGravity(gravity);
//            }
//        }
//        for (var att : attachments.keySet()) {
//            IGunModifier[] modifiers = getModifiers(weapon, att);
//            for (IGunModifier modifier : modifiers) {
//                gravity += modifier.additionalProjectileGravity();
//            }
//        }

        var finalGravity = new AtomicReference<>(gravity);

        forEachAttachment(weapon, (modifier -> finalGravity.set(modifier.modifyProjectileGravity(finalGravity.get()))));
        forEachAttachment(weapon, (modifier -> finalGravity.updateAndGet(v -> v + modifier.additionalProjectileGravity())));

        return finalGravity.get();
    }

    public static float getModifiedSpread(ItemStack weapon, float spread) {
//        var gun = getGun(weapon);
//        var attachments = gun.getModules().getAttachments();
//
//        for (var att : attachments.keySet()) {
//            IGunModifier[] modifiers = getModifiers(weapon, att);
//            for (IGunModifier modifier : modifiers) {
//                spread = modifier.modifyProjectileSpread(spread);
//            }
//        }

        var finalSpread = new AtomicReference<>(spread);
        forEachAttachment(weapon, (modifier -> finalSpread.set(modifier.modifyProjectileSpread(finalSpread.get()))));
        return finalSpread.get();
    }

    public static double getModifiedProjectileSpeed(ItemStack weapon, double speed) {
//        var gun = getGun(weapon);
//        var attachments = gun.getModules().getAttachments();
//
//        for (var att : attachments.keySet()) {
//            IGunModifier[] modifiers = getModifiers(weapon, att);
//            for (IGunModifier modifier : modifiers) {
//                speed = modifier.modifyProjectileSpeed(speed);
//            }
//        }
//
        var finalSpeed = new AtomicReference<>(speed);
        forEachAttachment(weapon, (modifier -> finalSpeed.set(modifier.modifyProjectileSpeed(finalSpeed.get()))));
        return finalSpeed.get();
    }

    public static float getFireSoundVolume(ItemStack weapon) {
//        var gun = getGun(weapon);
//        var attachments = gun.getModules().getAttachments();

//        for (var att : attachments.keySet()) {
//            IGunModifier[] modifiers = getModifiers(weapon, att);
//            for (IGunModifier modifier : modifiers) {
//                volume = modifier.modifyFireSoundVolume(volume);
//            }
//        }
        var volume = new AtomicReference<>(1.0F);
        forEachAttachment(weapon, (modifier -> volume.set(modifier.modifyFireSoundVolume(volume.get()))));
        return Mth.clamp(volume.get(), 0.0F, 16.0F);
    }

    @Deprecated(since = "1.3.0", forRemoval = true)
    public static double getMuzzleFlashSize(ItemStack weapon, double size) {
//        var gun = getGun(weapon);
//        var attachments = gun.getModules().getAttachments();
//
//        for (var att : attachments.keySet()) {
//            IGunModifier[] modifiers = getModifiers(weapon, att);
//            for (IGunModifier modifier : modifiers) {
//
//                size = modifier.modifyMuzzleFlashSize(size);
//            }
//        }

        var finalSize = new AtomicReference<>(size);
        forEachAttachment(weapon, (modifier -> finalSize.set(modifier.modifyMuzzleFlashSize(finalSize.get()))));
        return finalSize.get();
    }

    public static double getMuzzleFlashScale(ItemStack weapon, double scale) {
//        var gun = getGun(weapon);
//        var attachments = gun.getModules().getAttachments();

        var finalScale = new AtomicReference<>(scale);

//        for (var att : attachments.keySet()) {
//            IGunModifier[] modifiers = getModifiers(weapon, att);
//            for (IGunModifier modifier : modifiers) {
//                scale = modifier.modifyMuzzleFlashScale(scale);
//            }
//        }
//
        forEachAttachment(weapon, (modifier -> finalScale.set(modifier.modifyMuzzleFlashScale(scale))));
        return finalScale.get();
    }

    public static float getKickReduction(ItemStack weapon) {
//        var gun = getGun(weapon);
//        var attachments = gun.getModules().getAttachments();

//        for (var att : attachments.keySet()) {
//            var modifiers = getModifiers(weapon, att);
//            for (var modifier : modifiers) {
//                kickReduction *= Mth.clamp(modifier.kickModifier(), 0.0F, 1.0F);
//            }
//        }

        var kickReduction = new AtomicReference<>(1.0F);
        forEachAttachment(weapon, (modifier -> kickReduction.updateAndGet(v -> v * Mth.clamp(modifier.kickModifier(), 0.0F, 1.0F))));
        return 1.0F - kickReduction.get();
    }

    public static float getRecoilModifier(ItemStack weapon) {
        var gun = getGun(weapon);
        var attachments = gun.getModules().getAttachments();

        AtomicReference<Float> recoilReduction = new AtomicReference<>(1.0F);
//        for (var att : attachments.keySet()) {
//            IGunModifier[] modifiers = getModifiers(weapon, att);
//            for (IGunModifier modifier : modifiers) {
//                recoilReduction *= Mth.clamp(modifier.recoilModifier(), 0.0F, 1.0F);
//            }
//        }

        forEachAttachment(weapon, (modifier -> {
            recoilReduction.updateAndGet(v -> v * Mth.clamp(modifier.recoilModifier(), 0.0F, 1.0F));

        }));

        return 1.0F - recoilReduction.get();
    }

    public static boolean isSilencedFire(ItemStack weapon) {
        var gun = getGun(weapon);
        var attachments = gun.getModules().getAttachments();

        for (var att : attachments.keySet()) {
            var modifiers = getModifiers(weapon, att);
            for (var modifier : modifiers) {
                if (modifier.silencedFire()) {
                    return true;
                }
            }
        }
        return false;
    }

    public static double getModifiedFireSoundRadius(ItemStack weapon, double radius) {
//        var gun = getGun(weapon);
//        var attachments = gun.getModules().getAttachments();

//        for (var att : attachments.keySet()) {
//            IGunModifier[] modifiers = getModifiers(weapon, att);
//            for (IGunModifier modifier : modifiers) {
//                double newRadius = modifier.modifyFireSoundRadius(radius);
//                if (newRadius < minRadius) {
//                    minRadius = newRadius;
//                }
//            }
//        }

        var minRadius = new AtomicReference<>(radius);
        forEachAttachment(weapon, (modifier -> {
            double newRadius = modifier.modifyFireSoundRadius(radius);
            if (newRadius < minRadius.get()) {
                minRadius.set(newRadius);
            }
        }));
        return Mth.clamp(minRadius.get(), 0.0, Double.MAX_VALUE);
    }

    public static float getAdditionalDamage(ItemStack weapon) {
//        var gun = getGun(weapon);
//        var attachments = gun.getModules().getAttachments();

//        for (var att : attachments.keySet()) {
//            IGunModifier[] modifiers = getModifiers(weapon, att);
//            for (IGunModifier modifier : modifiers) {
//                additionalDamage += modifier.additionalDamage();
//            }
//        }

        var additionalDamage = new AtomicReference<>(0.0F);
        forEachAttachment(weapon, (modifier -> additionalDamage.updateAndGet(
                v -> v + modifier.additionalDamage())));
        return additionalDamage.get();
    }

    public static float getModifiedProjectileDamage(ItemStack weapon, float damage) {
//        var gun = getGun(weapon);
//        var attachments = gun.getModules().getAttachments();

//        for (var att : attachments.keySet()) {
//            IGunModifier[] modifiers = getModifiers(weapon, att);
//            for (IGunModifier modifier : modifiers) {
//                finalDamage = modifier.modifyProjectileDamage(finalDamage);
//            }
//        }

        var finalDamage = new AtomicReference<>(damage);
        forEachAttachment(weapon, (modifier -> finalDamage.set(modifier.modifyProjectileDamage(finalDamage.get()))));
        return finalDamage.get();
    }

    public static float getModifiedDamage(ItemStack weapon, Gun modifiedGun, float damage) {
//        var gun = getGun(weapon);
//        var attachments = gun.getModules().getAttachments();

//        for (var att : attachments.keySet()) {
//            var modifiers = getModifiers(weapon, att);
//            for (var modifier : modifiers) {
//                finalDamage.set(modifier.modifyProjectileDamage(finalDamage.get()));
//            }
//        }
//        for (var att : attachments.keySet()) {
//            var modifiers = getModifiers(weapon, att);
//            for (var modifier : modifiers) {
//                finalDamage.updateAndGet(v -> v + modifier.additionalDamage());
//            }
//        }
//

        var finalDamage = new AtomicReference<>(damage);
        forEachAttachment(weapon, (modifier -> finalDamage.set(modifier.modifyProjectileDamage(finalDamage.get()))));
        forEachAttachment(weapon, (modifier -> finalDamage.updateAndGet(v -> v + modifier.additionalDamage())));

        return finalDamage.get();
    }

    public static double getModifiedAimDownSightSpeed(ItemStack weapon, double speed) {
//        var gun = getGun(weapon);
//        var attachments = gun.getModules().getAttachments();
//
//        for (var att : attachments.keySet()) {
//            IGunModifier[] modifiers = getModifiers(weapon, att);
//            for (IGunModifier modifier : modifiers) {
//                speed = modifier.modifyAimDownSightSpeed(speed);
//            }
//        }

        var buffSpeed = new AtomicReference<Double>(speed);
        forEachAttachment(weapon, (modifier -> buffSpeed.set(
                modifier.modifyAimDownSightSpeed(buffSpeed.get()))));

        return Mth.clamp(speed, 0.01, Double.MAX_VALUE);
    }

    public static int getModifiedRate(ItemStack weapon, int rate) {
//        var gun = getGun(weapon);
//        var attachments = gun.getModules().getAttachments();
//
//        for (var att : attachments.keySet()) {
//            IGunModifier[] modifiers = getModifiers(weapon, att);
//            for (IGunModifier modifier : modifiers) {
//                rate = modifier.modifyFireRate(rate);
//            }
//        }

        var buffRate = new AtomicInteger(rate);
        forEachAttachment(weapon, (modifier -> buffRate.set(modifier.modifyFireRate(buffRate.get()))));
        return Mth.clamp(rate, 0, Integer.MAX_VALUE);
    }

    public static float getCriticalChance(ItemStack weapon) {
//        var gun = getGun(weapon);
//        var attachments = gun.getModules().getAttachments();

        var chance = new AtomicReference<>(0F);
//        for (var att : attachments.keySet()) {
//            IGunModifier[] modifiers = getModifiers(weapon, att);
//            for (IGunModifier modifier : modifiers) {
//                chance += modifier.criticalChance();
//            }
//        }
//
//
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
