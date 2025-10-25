package com.nukateam.ntgl.common.util.util;

import com.nukateam.ntgl.Ntgl;
import com.nukateam.ntgl.common.data.WeaponData;
import com.nukateam.ntgl.common.data.attachment.IAttachment;
import com.nukateam.ntgl.common.data.attachment.impl.Scope;
import com.nukateam.ntgl.common.data.config.weapon.AmmoConfig;
import com.nukateam.ntgl.common.data.holders.AmmoHolder;
import com.nukateam.ntgl.common.data.holders.AttachmentType;
import com.nukateam.ntgl.common.data.holders.FireMode;
import com.nukateam.ntgl.common.data.config.weapon.ProjectileConfig;
import com.nukateam.ntgl.common.data.constants.Tags;
import com.nukateam.ntgl.common.debug.Debug;

import com.nukateam.ntgl.common.foundation.item.attachment.ScopeItem;
import com.nukateam.ntgl.common.foundation.item.interfaces.IThrowable;
import com.nukateam.ntgl.common.foundation.item.interfaces.IWeapon;
import com.nukateam.ntgl.modules.enchantment.ModEnchantments;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collection;

public class WeaponStateHelper {
    public static final String AMMO_TAG = "Ammo";
    public static final String FIRE_MODE = "FireMode";
    public static final String ATTACHMENTS = "Attachments";

    //AMMO
    public static void switchAmmo(WeaponData data){
        var ammoItems = WeaponModifierHelper.getAmmoItems(data);
        var current = getCurrentAmmo(data);
        var newAmmo = SetUtils.cycleSet(ammoItems, current);

        setCurrentAmmo(data, newAmmo.getId());
    }

    public static ResourceKey<DamageType> getDamageType(WeaponData data){
        var ammo = getProjectileConfig(data);
        return ammo.getDamageType();
    }

    public static int getAmmoCount(WeaponData data) {
        var tag = data.weapon.getOrCreateTag();
        return tag.getInt(Tags.AMMO_COUNT);
    }

    public static void addAmmo(WeaponData data, int amount) {
        var tag = data.weapon.getOrCreateTag();
        var maxAmmo = WeaponModifierHelper.getMaxAmmo(data);
        var result = Math.min(tag.getInt(Tags.AMMO_COUNT) + amount, maxAmmo);
        tag.putInt(Tags.AMMO_COUNT, result);
        data.weapon.setTag(tag);
    }

    public static void setCurrentAmmo(WeaponData data, ResourceLocation ammo) {
        var tag = data.weapon.getOrCreateTag();
        tag.putString(AMMO_TAG, ammo.toString());
        data.weapon.setTag(tag);
    }

    public static AmmoHolder getCurrentAmmo(WeaponData data) {
        var tag = data.weapon.getOrCreateTag();

        if(tag.contains(AMMO_TAG, Tag.TAG_STRING)){
            var ammoId = tag.getString(AMMO_TAG);
            return AmmoHolder.getType(ammoId);
        }
        else {
            var ammoItems = WeaponModifierHelper.getAmmoItems(data);
            return SetUtils.getFirst(ammoItems);
        }
    }

    public static AmmoHolder getCurrentAmmoWithoutCheck(WeaponData data) {
        var tag = data.weapon.getOrCreateTag();
        var ammoItems = WeaponModifierHelper.getAmmoItems(data);

        if(tag.contains(AMMO_TAG, Tag.TAG_STRING)) {
            return AmmoHolder.getType(tag.getString(AMMO_TAG));
        }
        else {
            var firstAmmo = SetUtils.getFirst(ammoItems);
            setCurrentAmmo(data, firstAmmo.getId());
            return firstAmmo;
        }
    }

    public static boolean isAcceptable(WeaponData weaponData, ItemStack item) {
        return getCurrentAmmo(weaponData).isAcceptable(item);
    }

//    public static Item getAmmoItem(GunData data) {
//        return ITEMS.getValue(getAmmoHolder(data));
//    }

    public static AmmoConfig getAmmoConfig(WeaponData data) {
        var ammoId = getCurrentAmmo(data).getId();
        return WeaponModifierHelper.getAmmoConfig(ammoId, data);
    }

    public static @NotNull ProjectileConfig getProjectileConfig(WeaponData data) {
        var ammoId = getCurrentAmmoWithoutCheck(data).getId();
        return WeaponModifierHelper.getProjectileConfig(ammoId, data);
    }

    //FIRE MODE______________________________________
    public static void switchFireMode(WeaponData data){
        var fireModes = WeaponModifierHelper.getFireModes(data);
        var current = getFireMode(data);
        var newFireMode = SetUtils.cycleSet(fireModes, current);
        setFireMode(data, newFireMode);
    }

    public static void setFireMode(WeaponData data, FireMode fireMode) {
        var tag = data.weapon.getOrCreateTag();
        tag.putString(FIRE_MODE, fireMode.toString());
        data.weapon.setTag(tag);
    }

    public static FireMode getFireMode(WeaponData data) {
        var fireModes = WeaponModifierHelper.getFireModes(data);
        var currentFireMode = getFireMode(data.weapon);

        if (currentFireMode == null || !fireModes.contains(currentFireMode)) {
            setFireMode(data, SetUtils.getFirst(fireModes));
            return SetUtils.getFirst(fireModes);
        }
        else return currentFireMode;
    }

    @Nullable
    private static FireMode getFireMode(ItemStack gun) {
        var tag = gun.getOrCreateTag();
        if(tag.contains(FIRE_MODE, Tag.TAG_STRING))
            return FireMode.getType(tag.getString(FIRE_MODE));
        else return null;
    }

    public static boolean isMaxAmmo(WeaponData data) {
        var ammo = getAmmoCount(data);
        var maxAmmo = WeaponModifierHelper.getMaxAmmo(data);
        return ammo == maxAmmo;
    }

    public static void setAmmo(ItemStack gunStack, int amount) {
        var tag = gunStack.getOrCreateTag();
        tag.putInt(Tags.AMMO_COUNT, amount);
    }

    public static void setMaxAmmo(WeaponData data) {
        WeaponStateHelper.setAmmo(data.weapon, WeaponModifierHelper.getMaxAmmo(data));
    }

    public static boolean hasAmmo(ItemStack gunStack) {
        var tag = gunStack.getOrCreateTag();
        return tag.getBoolean("IgnoreAmmo") || tag.getInt(Tags.AMMO_COUNT) > 0;
    }

    public static void fillAmmo(WeaponData data) {
        if (data.weapon.getItem() instanceof IWeapon) {
            var tag = data.weapon.getOrCreateTag();
            var maxAmmo = WeaponModifierHelper.getMaxAmmo(data);

            tag.putInt(Tags.AMMO_COUNT, maxAmmo);
        }
    }

    public static float getAdditionalDamage(ItemStack gunStack) {
        var tag = gunStack.getOrCreateTag();
        return tag.getFloat("AdditionalDamage");
    }

    public static ArrayList<ItemStack> getAttachmentItems(ItemStack gun) {
        var compound = gun.getTag();
        var result = new ArrayList<ItemStack>();

        if (compound != null && compound.contains(ATTACHMENTS, Tag.TAG_COMPOUND)) {
            var attachment = compound.getCompound(ATTACHMENTS);
            for (var slot: attachment.getAllKeys()){
                if (attachment.contains(slot, Tag.TAG_COMPOUND))
                    result.add(ItemStack.of(attachment.getCompound(slot)));
            }
        }
        return result;
    }

    public static ItemStack getAttachmentItem(AttachmentType type, ItemStack gun) {
        var compound = gun.getTag();
        if (compound != null && compound.contains(ATTACHMENTS, Tag.TAG_COMPOUND)) {
            var attachment = compound.getCompound(ATTACHMENTS);
            if (attachment.contains(type.toString(), Tag.TAG_COMPOUND)) {
                return ItemStack.of(attachment.getCompound(type.toString()));
            }
        }
        return ItemStack.EMPTY;
    }

    public static boolean hasScopeOverlay(ItemStack gun) {
        var scope = getScopeItem(gun);
        return scope != null && scope.getProperties().hasOverlay();
    }

    @Nullable
    public static ScopeItem getScopeItem(ItemStack gun) {
        var attachment = getAttachmentItem(AttachmentType.SCOPE, gun);
        if(!attachment.isEmpty() ){
            return (ScopeItem)attachment.getItem();
        }

        return null;
    }

    public static boolean hasAttachmentEquipped(ItemStack stack, AttachmentType type) {
        var gun = WeaponModifierHelper.getConfig(stack);
        if (!gun.canAttachType(type))
            return false;

        var compound = stack.getTag();
        if (compound != null && compound.contains(ATTACHMENTS, Tag.TAG_COMPOUND)) {
            CompoundTag attachment = compound.getCompound(ATTACHMENTS);
            return attachment.contains(type.toString(), Tag.TAG_COMPOUND);
        }
        return false;
    }

    public static ItemStack getScopeStack(ItemStack gun) {
        return getAttachmentItem(AttachmentType.SCOPE, gun);
    }

    @Nullable
    public static Scope getScope(ItemStack gun) {
        var scopeStack = getScopeStack(gun);

        if (scopeStack.getItem() instanceof ScopeItem scopeItem) {
            if (Ntgl.isDebugging())
                return Debug.getScope(scopeItem);

            return scopeItem.getProperties();
        }
        return null;
    }

    public static float getFovModifier(WeaponData data) {
        float modifier = 0.0F;
        var weapon = data.weapon;
        if (hasAttachmentEquipped(weapon, AttachmentType.SCOPE)) {
            var scope = getScope(weapon);
            if (scope != null) {
                if (scope.getFovModifier() < 1.0F) {
                    return Mth.clamp(scope.getFovModifier(), 0.01F, 1.0F);
                }
                modifier -= scope.getFovModifier();
            }
        }

        var fovMod = WeaponModifierHelper.getFovModifier(data);
        return modifier + fovMod;
    }

    public static boolean isAmmoIgnored(ItemStack stack) {
        var tag = stack.getOrCreateTag();
        return tag.contains("IgnoreAmmo", Tag.TAG_BYTE);
    }

    public static void saveAttachments(WeaponData data, Collection<ItemStack> attachments){
        var weapon = data.weapon;
//        var currentAttachments = getAttachmentItems(weapon);
//        var isServerSide = data.shooter != null && !data.shooter.level().isClientSide;
//        var attachmentsChanged = !containsItem(attachments, currentAttachments);
//
//        if (isServerSide && attachmentsChanged){
//            ServerPlayHandler.unloadGun((ServerPlayer)data.shooter, data.gun);
//        }

        var attachmentsTag = new CompoundTag();

        for (var itemStack : attachments) {
            if (itemStack.getItem() instanceof IAttachment attachment) {
                var tagKey = attachment.getType();
                attachmentsTag.put(tagKey.toString(), itemStack.save(new CompoundTag()));
            }
        }

        var tag = weapon.getOrCreateTag();
        tag.put(Tags.ATTACHMENTS, attachmentsTag);
    }

    private static boolean containsItem(Collection<ItemStack> whereFind, Collection<ItemStack> whatFind) {
        for (var att : whatFind) {
            if (!contains(whereFind, att))
                return false;
        }
        return true;

//        return !whatFind.contains(whereFind);
    }

    public static boolean contains(Collection<ItemStack> list, ItemStack toFind) {
        for (var stack : list) {
//            ItemStack.matches()
            if(stack.getItem() == toFind.getItem()){
                return true;
            }
        }
        return false;
    }

    public static void saveAttachment(ItemStack weapon, ItemStack attachmentStack){
        var tag = weapon.getOrCreateTag();

        var attachmentsTag = new CompoundTag();

        if(tag.contains(Tags.ATTACHMENTS, Tag.TAG_COMPOUND)){
            attachmentsTag = tag.getCompound(Tags.ATTACHMENTS);
        }

        if (attachmentStack.getItem() instanceof IAttachment attachment) {
            var tagKey = attachment.getType().toString();
            attachmentsTag.put(tagKey, attachmentStack.save(new CompoundTag()));
        }

        tag.put(Tags.ATTACHMENTS, attachmentsTag);
    }

    public static void consumeAmmo(WeaponData data) {
        var shooter = data.wielder;
        var heldItem = data.weapon;
        if(shooter != null && heldItem != null){
            var ammoPerShot = WeaponModifierHelper.getAmmoPerShot(data);
            var fireMode = WeaponStateHelper.getFireMode(data);
            var multishotAmount = WeaponModifierHelper.getMultishotAmount(data);
            var ammoCount = WeaponStateHelper.getAmmoCount(data);

            if(fireMode == FireMode.MULTI && multishotAmount > 1){
                multishotAmount = Math.min(ammoCount, multishotAmount);
                ammoPerShot *= multishotAmount;
            }

            int level = heldItem.getEnchantmentLevel(ModEnchantments.RECLAIMED.get());

            if (level == 0 || shooter.level().random.nextInt(4 - Mth.clamp(level, 1, 2)) != 0) {
                var remainingAmmo = Math.max(0, ammoCount - ammoPerShot);
                setAmmo(heldItem, remainingAmmo);
            }
        }
    }

    public static int getEquipTime(ItemStack slot, LivingEntity shooter) {
        var equipTime = 0;
        if (slot.getItem() instanceof IWeapon) {
            var data = new WeaponData(slot, shooter);
            equipTime = WeaponModifierHelper.getEquipTime(data);
        }
        else if (slot.getItem() instanceof IThrowable throwable) {
            equipTime = throwable.getConfig().getGeneral().getEquipTime();
        }
        return equipTime;
    }
}
