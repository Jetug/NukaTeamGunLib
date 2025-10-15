package com.nukateam.ntgl.common.foundation.item.interfaces;

import com.nukateam.geo.interfaces.IResourceProvider;
import com.nukateam.ntgl.common.data.config.gun.WeaponConfig;
import com.nukateam.ntgl.common.util.interfaces.IConfigProvider;
import com.nukateam.ntgl.common.util.interfaces.IGunModifier;
import net.minecraft.world.item.ItemStack;

public interface IWeapon extends INtglItem, IConfigConsumer<WeaponConfig>, IConfigProvider<WeaponConfig>, IResourceProvider {
    WeaponConfig getModifiedConfig(ItemStack stack);

    default IGunModifier[] getModifiers() {
        return new IGunModifier[0];
    }
}
