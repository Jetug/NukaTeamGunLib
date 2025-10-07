package com.nukateam.ntgl.common.foundation.item.interfaces;

import com.nukateam.geo.interfaces.IResourceProvider;
import com.nukateam.ntgl.common.data.config.gun.Gun;
import com.nukateam.ntgl.common.util.interfaces.IConfigProvider;
import net.minecraft.world.item.ItemStack;

public interface IWeapon extends INtglItem, IConfigConsumer<Gun>, IConfigProvider<Gun>, IResourceProvider {
    Gun getModifiedConfig(ItemStack stack);
}
