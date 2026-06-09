package com.nukateam.ntgl.common.data;

import com.nukateam.ntgl.common.data.holders.WeaponMode;
import net.minecraft.core.HolderLookup;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;

import javax.annotation.Nullable;

public class WeaponData{
    @Nullable public final ItemStack weapon;
    @Nullable public ItemStack attachment;
    @Nullable public final LivingEntity wielder;
    @Nullable private final HolderLookup.Provider registryAccess;
    public WeaponMode weaponMode = WeaponMode.PRIMARY;

    public WeaponData(ItemStack weapon, LivingEntity wielder) {
        this(weapon, wielder, null);
    }

    public WeaponData(ItemStack weapon, LivingEntity wielder, @Nullable HolderLookup.Provider registryAccess) {
        this.weapon = weapon;
        this.wielder = wielder;
        this.registryAccess = registryAccess;
    }

    public WeaponData setAttachment(@Nullable ItemStack attachment) {
        this.attachment = attachment;
        return this;
    }

    public WeaponData setWeaponMode(WeaponMode weaponMode) {
        this.weaponMode = weaponMode;
        return this;
    }

    @Override
    public WeaponData clone(){
        return new WeaponData(weapon, wielder, registryAccess).setWeaponMode(weaponMode).setAttachment(attachment);
    }

    @Nullable
    public HolderLookup.Provider registryAccess() {
        if (registryAccess != null) {
            return registryAccess;
        }

        return wielder != null ? wielder.level().registryAccess() : null;
    }

    public HolderLookup.Provider requireRegistryAccess() {
        var provider = registryAccess();
        if (provider == null) {
            throw new IllegalStateException("WeaponData requires registry access. Provide a wielder or tooltip registry context.");
        }
        return provider;
    }

}
