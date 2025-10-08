package com.nukateam.ntgl.common.data;

import com.nukateam.ntgl.common.data.config.WeaponAction;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;

import javax.annotation.Nullable;

public class GunData {
    @Nullable public final ItemStack gun;
    @Nullable public ItemStack attachment;
    @Nullable public final LivingEntity shooter;
    public WeaponAction weaponAction = WeaponAction.PRIMARY;

    public GunData(ItemStack gun, LivingEntity shooter) {
        this.gun = gun;
        this.shooter = shooter;
    }

    public GunData setAttachment(@Nullable ItemStack attachment) {
        this.attachment = attachment;
        return this;
    }

    public GunData setWeaponAction(WeaponAction weaponAction) {
        this.weaponAction = weaponAction;
        return this;
    }
}
