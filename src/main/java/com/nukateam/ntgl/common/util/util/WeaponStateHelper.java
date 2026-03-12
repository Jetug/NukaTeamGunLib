package com.nukateam.ntgl.common.util.util;

import com.nukateam.ntgl.Ntgl;
import com.nukateam.ntgl.common.data.WeaponData;
import com.nukateam.ntgl.common.data.attachment.IAttachment;
import com.nukateam.ntgl.common.data.attachment.impl.Scope;
import com.nukateam.ntgl.common.data.holders.AmmoHolder;
import com.nukateam.ntgl.common.data.holders.AttachmentType;
import com.nukateam.ntgl.common.data.holders.FireMode;
import com.nukateam.ntgl.common.data.config.weapon.ProjectileConfig;
import com.nukateam.ntgl.common.data.holders.ThrowMode;
import com.nukateam.ntgl.common.debug.Debug;

import com.nukateam.ntgl.common.foundation.init.NtglComponents;
import com.nukateam.ntgl.common.foundation.item.WeaponItem;
import com.nukateam.ntgl.common.foundation.item.attachment.ScopeItem;
import com.nukateam.ntgl.common.foundation.item.interfaces.IThrowable;
import com.nukateam.ntgl.common.foundation.item.interfaces.IWeapon;
import net.minecraft.core.HolderLookup;
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
    //AMMO COUNT
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

    public static float getProjectileDamage(WeaponData data) {
        var ammo = getCurrentAmmo(data).getId();
        return WeaponModifierHelper.getProjectileDamage(ammo, data);
    }

    public static int getAmmoCount(WeaponData data) {
        return data.weapon.getOrDefault(NtglComponents.AMMO_COUNT, 0);
    }

    public static void addAmmo(WeaponData data, int amount) {
        var tag = NtglComponents.getWeaponTag(data.weapon);
        var maxAmmo = WeaponModifierHelper.getMaxAmmo(data);
        var ammoCount = getAmmoCount(data);
        var result = Math.min(ammoCount + amount, maxAmmo);
        setAmmoCount(data, result);
        NtglComponents.setWeaponTag(data.weapon, tag);
    }

    public static void setAmmoCount(WeaponData data, int amount) {
        data.weapon.set(NtglComponents.AMMO_COUNT, amount);
    }

    public static void setMaxAmmo(WeaponData data) {
        WeaponStateHelper.setAmmoCount(data, WeaponModifierHelper.getMaxAmmo(data));
    }

    public static boolean hasAmmo(WeaponData data) {
        var ammoCount = getAmmoCount(data);
        var isAmmoIgnored = isAmmoIgnored(data);
        return isAmmoIgnored || ammoCount > 0;
    }

    public static boolean isMaxAmmo(WeaponData data) {
        var ammo = getAmmoCount(data);
        var maxAmmo = WeaponModifierHelper.getMaxAmmo(data);
        return ammo == maxAmmo;
    }

    public static void fillAmmo(WeaponData data) {
        if (data.weapon.getItem() instanceof IWeapon) {
            var maxAmmo = WeaponModifierHelper.getMaxAmmo(data);
            setAmmoCount(data, maxAmmo);
        }
    }

    //AMMO IGNORED
    public static boolean isAmmoIgnored(WeaponData data) {
        return data.weapon.getOrDefault(NtglComponents.IGNORE_AMMO, false);
    }

    //AMMO TYPE
    public static void setCurrentAmmo(WeaponData data, ResourceLocation ammo) {
        assert data.weapon != null;
        data.weapon.set(NtglComponents.AMMO, ammo.toString());
    }

    public static AmmoHolder getCurrentAmmo(WeaponData data) {
        assert data.weapon != null;
        var ammoId = getAmmo(data.weapon);
        if(!ammoId.isEmpty()){
            return AmmoHolder.getType(ammoId);
        }
        else {
            var ammoItems = WeaponModifierHelper.getAmmoItems(data);
            return SetUtils.getFirst(ammoItems);
        }
    }

    public static AmmoHolder getCurrentAmmoWithoutCheck(WeaponData data) {
        var ammoItems = WeaponModifierHelper.getAmmoItems(data);

        var ammoId = getAmmo(data.weapon);
        if(!ammoId.isEmpty()){
            return AmmoHolder.getType(ammoId);
        }
        else {
            var firstAmmo = SetUtils.getFirst(ammoItems);
            setCurrentAmmo(data, firstAmmo.getId());
            return firstAmmo;
        }
    }

    private static String getAmmo(ItemStack stack) {
        return stack.getOrDefault(NtglComponents.AMMO, "");
    }

    public static @NotNull ProjectileConfig getProjectileConfig(WeaponData data) {
        var ammoId = getCurrentAmmoWithoutCheck(data).getId();
        return WeaponModifierHelper.getProjectileConfig(ammoId, data);
    }

    //FIRE MODE______________________________________
    public static FireMode getFireMode(WeaponData data) {
        var fireModes = WeaponModifierHelper.getFireModes(data);
        assert data.weapon != null;
        var fireModId = getFireMode(data.weapon);

        if(!fireModId.isEmpty()){
            var currentFireMode = FireMode.getType(fireModId);
            if (currentFireMode == null || !fireModes.contains(currentFireMode)) {
                setFireMode(data, SetUtils.getFirst(fireModes));
                return SetUtils.getFirst(fireModes);
            }
            else return currentFireMode;
        }
        else {
            setFireMode(data, SetUtils.getFirst(fireModes));
            return SetUtils.getFirst(fireModes);
        }
    }

    public static void setFireMode(WeaponData data, FireMode fireMode) {
        assert data.weapon != null;
        data.weapon.set(NtglComponents.FIRE_MODE, fireMode.toString());
    }

    public static void switchFireMode(WeaponData data){
        var fireModes = WeaponModifierHelper.getFireModes(data);
        var current = getFireMode(data);
        var newFireMode = SetUtils.cycleSet(fireModes, current);
        setFireMode(data, newFireMode);
    }

    private static String getFireMode(ItemStack stack) {
        return stack.getOrDefault(NtglComponents.FIRE_MODE, "");
    }

    //ATTACHAEMTS
    public static CompoundTag getAttachments(ItemStack stack){
        return stack.getOrDefault(NtglComponents.ATTACHMENTS, new CompoundTag());
    }

    public static ArrayList<ItemStack> getAttachmentItems(ItemStack weapon, HolderLookup.Provider lookupProvider) {
        var result = new ArrayList<ItemStack>();

        var attachment = getAttachments(weapon);
        for (var slot: attachment.getAllKeys()){
            if (attachment.contains(slot, Tag.TAG_COMPOUND)) {
                result.add(ItemStack.parseOptional(lookupProvider, attachment.getCompound(slot)));
            }
        }
        return result;
    }

    public static ItemStack getAttachmentItem(AttachmentType type, WeaponData data) {
        var attachment = getAttachments(data.weapon);
        if (attachment.contains(type.toString(), Tag.TAG_COMPOUND)) {
            return ItemStack.parseOptional(data.registryAccess(), attachment.getCompound(type.toString()));
        }

        return ItemStack.EMPTY;
    }

    public static boolean hasAttachmentEquipped(ItemStack stack, AttachmentType type) {
        var gun = WeaponModifierHelper.getConfig(new WeaponData(stack, null));
        if (!gun.canAttachType(type))
            return false;

        var attachment = getAttachments(stack);
        return attachment.contains(type.toString(), Tag.TAG_COMPOUND);

    }

    public static void writeAttachments(Collection<ItemStack> attachments, WeaponData data){
        var tag = new CompoundTag();

        for (var itemStack : attachments) {
            if (itemStack.getItem() instanceof IAttachment attachment) {
                var tagKey = attachment.getType();
                tag.put(tagKey.toString(), itemStack.save(data.registryAccess(), new CompoundTag()));
            }
        }

        setAttachments(data.weapon, tag);
    }

    public static void setAttachments(ItemStack stack, CompoundTag attachments){
        stack.set(NtglComponents.ATTACHMENTS, attachments);
    }

    //SCOPE
    public static ItemStack getScopeStack(WeaponData data) {
        return getAttachmentItem(AttachmentType.SCOPE, data);
    }

    public static boolean hasScopeOverlay(WeaponData data) {
        var scope = getScopeItem(data);
        return scope != null && scope.getProperties().hasOverlay();
    }

    @Nullable
    public static ScopeItem getScopeItem(WeaponData data) {
        var attachment = getAttachmentItem(AttachmentType.SCOPE, data);
        if(!attachment.isEmpty() ){
            return (ScopeItem)attachment.getItem();
        }

        return null;
    }

    @Nullable
    public static Scope getScope(WeaponData gun) {
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
            var scope = getScope(data);
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

            var remainingAmmo = Math.max(0, ammoCount - ammoPerShot);
            setAmmoCount(data, remainingAmmo);
        }
    }

    public static int getEquipTime(ItemStack slot, LivingEntity shooter) {
        var equipTime = 0;
        if (slot.getItem() instanceof IWeapon) {
            var data = new WeaponData(slot, shooter);
            equipTime = WeaponModifierHelper.getEquipTime(data);
        }
        return equipTime;
    }

    public static String getVariant(ItemStack stack) {
        var gunTag = NtglComponents.getWeaponTag(stack);
        if (!gunTag.contains(WeaponItem.VARIANT, Tag.TAG_STRING)) {
            gunTag.putString(WeaponItem.VARIANT, "default");
            NtglComponents.setWeaponTag(stack, gunTag);
        }

        return gunTag.getString(WeaponItem.VARIANT);
    }

    public static void switchThrowMode(WeaponData data){
        if(data.weapon.getItem() instanceof IThrowable) {
            var stack = data.weapon;
            var modes = WeaponModifierHelper.getThrowModes(data);
            var current = getThrowMode(data);
            var newMode = SetUtils.cycleSet(modes, current);
            setThrowMode(stack, newMode);
        }
    }

    public static ThrowMode getThrowMode(WeaponData data) {
        var stack = data.weapon;
        assert stack != null;
        var modes = WeaponModifierHelper.getThrowModes(data);
        var modeId = getThrowMode(stack);
        if(!modeId.isEmpty()){
            var currentMode = ThrowMode.getType(modeId);

            if (currentMode == null || !modes.contains(currentMode)) {
                setThrowMode(stack, SetUtils.getFirst(modes));
                return SetUtils.getFirst(modes);
            }
            else return currentMode;
        }
        else {
            setThrowMode(stack, SetUtils.getFirst(modes));
            return SetUtils.getFirst(modes);
        }
    }

    private static String getThrowMode(ItemStack stack){
        return stack.getOrDefault(NtglComponents.THROW_MODE, "");
    }

    public static void setThrowMode(ItemStack stack, ThrowMode throwMode) {
        stack.set(NtglComponents.THROW_MODE, throwMode.toString());
    }
}
