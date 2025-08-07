package com.nukateam.ntgl.common.util.util;

import com.nukateam.ntgl.Ntgl;
import com.nukateam.ntgl.common.data.GunData;
import com.nukateam.ntgl.common.data.attachment.IAttachment;
import com.nukateam.ntgl.common.data.attachment.impl.Scope;
import com.nukateam.ntgl.common.data.config.gun.Gun;
import com.nukateam.ntgl.common.data.holders.AmmoType;
import com.nukateam.ntgl.common.data.holders.AttachmentType;
import com.nukateam.ntgl.common.data.holders.FireMode;
import com.nukateam.ntgl.common.data.config.ProjectileConfig;
import com.nukateam.ntgl.common.data.constants.Tags;
import com.nukateam.ntgl.common.debug.Debug;
import com.nukateam.ntgl.common.foundation.item.WeaponItem;
import com.nukateam.ntgl.common.foundation.item.attachment.ScopeItem;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Set;

import static net.minecraftforge.registries.ForgeRegistries.ITEMS;

public class GunStateHelper {
    public static final String AMMO_TAG = "Projectile";
    public static final String FIRE_MODE = "FireMode";
    public static final String ATTACHMENTS = "Attachments";

    //AMMO
    public static void switchAmmo(GunData data){
        var ammoItems = GunModifierHelper.getAmmoItems(data);
        var current = getAmmoId(data);
        var newAmmo = cycleSet(ammoItems, current);

        setCurrentAmmo(data, newAmmo);
    }

    public static ResourceKey<DamageType> getDamageType(GunData data){
        var ammo = getAmmoConfig(data);
        return ammo.getDamageType();
    }

    public static int getAmmoCount(ItemStack gunStack) {
        var tag = gunStack.getOrCreateTag();
        return tag.getInt(Tags.AMMO_COUNT);
    }

    public static void setCurrentAmmo(GunData data, ResourceLocation ammo) {
        var tag = data.gun.getOrCreateTag();
        tag.putString(AMMO_TAG, ammo.toString());
        data.gun.setTag(tag);
    }

    public static ResourceLocation getAmmoId(GunData data) {
        var tag = data.gun.getOrCreateTag();
        var ammoItems = GunModifierHelper.getAmmoItems(data);
        var currentAmmo = getAmmoId(tag);

        if (currentAmmo == null) {
            return getFirst(ammoItems);
        }
        else return currentAmmo;
    }

    private static @Nullable ResourceLocation getAmmoId(CompoundTag tag) {
        if(tag.contains(AMMO_TAG, Tag.TAG_STRING))
            return ResourceLocation.tryParse(tag.getString(AMMO_TAG));
        else return null;
    }

    public static boolean isCurrentAmmo(GunData gunData, Item item) {
        return getAmmoId(gunData).equals(ITEMS.getKey(item));
    }

    public static Item getAmmoItem(GunData data) {
        return ITEMS.getValue(getAmmoId(data));
    }

    public static AmmoType getAmmoType(GunData data) {
        var ammo = getAmmoConfig(data);
        return ammo.getType();
    }

    public static @NotNull ProjectileConfig getAmmoConfig(GunData data) {
        var ammoId = getAmmoId(data);
        return GunModifierHelper.getAmmoConfig(ammoId, data);
    }

    //FIRE MODE______________________________________
    public static void switchFireMode(GunData data){
        var fireModes = GunModifierHelper.getFireModes(data);
        var current = getFireMode(data);
        var newFireMode = cycleSet(fireModes, current);
        setFireMode(data, newFireMode);
    }

    public static void setFireMode(GunData data, FireMode fireMode) {
        var tag = data.gun.getOrCreateTag();
        tag.putString(FIRE_MODE, fireMode.toString());
        data.gun.setTag(tag);
    }

    public static FireMode getFireMode(GunData data) {
        var fireModes = GunModifierHelper.getFireModes(data);
        var currentFireMode = getFireMode(data.gun);

        if (currentFireMode == null || !fireModes.contains(currentFireMode)) {
            setFireMode(data,getFirst(fireModes));
            return getFirst(fireModes);
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

    private static <T> T cycleSet(Set<T> set, T value) {
        var buff = new ArrayList<>(set.stream().toList());
        var i = buff.indexOf(value);

        if(i == set.size() - 1)
            i = 0;
        else i++;

        return buff.get(i);
    }

    public static <T> T getFirst(Set<T> set) {
        return set.iterator().next();
    }

    public static boolean isMaxAmmo(GunData data) {
        var ammo = getAmmoCount(data.gun);
        var maxAmmo = GunModifierHelper.getMaxAmmo(data);
        return ammo == maxAmmo;
    }

    public static void setAmmo(ItemStack gunStack, int amount) {
        var tag = gunStack.getOrCreateTag();
        tag.putInt(Tags.AMMO_COUNT, amount);
    }

    public static boolean hasAmmo(ItemStack gunStack) {
        var tag = gunStack.getOrCreateTag();
        return tag.getBoolean("IgnoreAmmo") || tag.getInt(Tags.AMMO_COUNT) > 0;
    }

    public static void fillAmmo(GunData data) {
        if (data.gun.getItem() instanceof WeaponItem weaponItem) {
            var tag = data.gun.getOrCreateTag();
//            var maxAmmo = weaponItem.getModifiedGun(gunStack).getGeneral().getMaxAmmo(gunStack);
            var maxAmmo = GunModifierHelper.getMaxAmmo(data);

            tag.putInt(Tags.AMMO_COUNT, maxAmmo);
        }
    }

    public static float getAdditionalDamage(ItemStack gunStack) {
        var tag = gunStack.getOrCreateTag();
        return tag.getFloat("AdditionalDamage");
    }

    public static boolean hasNoAmmo(LivingEntity player, ItemStack weapon) {
        return InventoryUtil.findAmmo(player, weapon).stack().isEmpty();
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
        var gun = GunModifierHelper.getGun(stack);
        if (!gun.canAttachType(type, gun))
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

    public static float getFovModifier(ItemStack stack, Gun modifiedGun) {
        float modifier = 0.0F;
        if (hasAttachmentEquipped(stack, AttachmentType.SCOPE)) {
            var scope = getScope(stack);
            if (scope != null) {
                if (scope.getFovModifier() < 1.0F) {
                    return Mth.clamp(scope.getFovModifier(), 0.01F, 1.0F);
                }
                modifier -= scope.getFovModifier();
            }
        }
        var zoom = modifiedGun.getModules().getZoom();
        return zoom != null ? modifier + zoom.getFovModifier() : 0F;
    }

    public static boolean isAmmoIgnored(ItemStack stack) {
        var tag = stack.getOrCreateTag();
        return tag.contains("IgnoreAmmo", Tag.TAG_BYTE);
    }

    public static void saveAttachments(ItemStack weapon, Iterable<ItemStack> attachments){
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
}
