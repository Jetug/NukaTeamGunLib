package com.nukateam.ntgl.common.data;

import com.nukateam.ntgl.common.data.holders.WeaponMode;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;

import javax.annotation.Nullable;

public class WeaponData{
    @Nullable public final ItemStack weapon;
    @Nullable public ItemStack attachment;
    @Nullable public final LivingEntity wielder;
    public WeaponMode weaponMode = WeaponMode.PRIMARY;

    public WeaponData(ItemStack weapon, LivingEntity wielder) {
        this.weapon = weapon;
        this.wielder = wielder;
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
        return new WeaponData(weapon, wielder).setWeaponMode(weaponMode).setAttachment(attachment);
    }
}
