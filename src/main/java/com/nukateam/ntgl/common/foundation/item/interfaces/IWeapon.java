package com.nukateam.ntgl.common.foundation.item.interfaces;

import com.nukateam.geo.interfaces.IResourceProvider;
import com.nukateam.ntgl.common.data.config.gun.Gun;
import com.nukateam.ntgl.common.util.interfaces.IConfigProvider;
import com.nukateam.ntgl.common.util.interfaces.IGunModifier;
import net.minecraft.world.item.ItemStack;

import javax.annotation.Nullable;

public interface IWeapon extends INtglItem, IConfigConsumer<Gun>, IConfigProvider<Gun>, IResourceProvider {
    Gun getModifiedConfig(ItemStack stack);

    default IGunModifier[] getModifiers() {
        return new IGunModifier[0];
    }
}
