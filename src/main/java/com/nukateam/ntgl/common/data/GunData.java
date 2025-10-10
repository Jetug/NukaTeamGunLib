package com.nukateam.ntgl.common.data;

import com.nukateam.ntgl.common.data.holders.AttackMode;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;

import javax.annotation.Nullable;

public class GunData {
    @Nullable public final ItemStack gun;
    @Nullable public ItemStack attachment;
    @Nullable public final LivingEntity shooter;
    public AttackMode weaponAction = AttackMode.PRIMARY;

    public GunData(ItemStack gun, LivingEntity shooter) {
        this.gun = gun;
        this.shooter = shooter;
    }

    public GunData setAttachment(@Nullable ItemStack attachment) {
        this.attachment = attachment;
        return this;
    }

    public GunData setWeaponAction(AttackMode weaponAction) {
        this.weaponAction = weaponAction;
        return this;
    }
}
