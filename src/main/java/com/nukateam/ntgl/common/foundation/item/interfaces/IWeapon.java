package com.nukateam.ntgl.common.foundation.item.interfaces;

import com.nukateam.geo.interfaces.IResourceProvider;
import com.nukateam.ntgl.common.data.config.weapon.WeaponConfig;
import com.nukateam.ntgl.common.util.interfaces.IConfigProvider;
import com.nukateam.ntgl.common.util.interfaces.IWeaponModifier;
import net.minecraft.world.item.ItemStack;

public interface IWeapon extends INtglItem, IConfigConsumer<WeaponConfig>, IConfigProvider<WeaponConfig>, IResourceProvider {
    WeaponConfig getModifiedConfig(ItemStack stack);

    default IWeaponModifier[] getModifiers() {
        return new IWeaponModifier[0];
    }
}
