package com.nukateam.ntgl.common.foundation.item.interfaces;

import com.nukateam.geo.interfaces.IResourceProvider;
import com.nukateam.ntgl.common.data.WeaponData;
import com.nukateam.ntgl.common.data.config.weapon.WeaponConfig;
import com.nukateam.ntgl.common.util.interfaces.IConfigProvider;
import com.nukateam.ntgl.common.util.interfaces.IWeaponModifier;
import com.nukateam.ntgl.common.util.util.WeaponStateHelper;
import net.minecraft.world.item.ItemStack;

public interface IWeapon extends IConfigConsumer<WeaponConfig>, IConfigProvider<WeaponConfig>, IResourceProvider {
    WeaponConfig getModifiedConfig(ItemStack stack);

    default IWeaponModifier[] getModifiers() {
        return new IWeaponModifier[0];
    }

    default void setDefaultTag(ItemStack stack){
        WeaponStateHelper.setAmmoCount(new WeaponData(stack, null), getConfig().getGeneral().getMaxAmmo());
    }
}
