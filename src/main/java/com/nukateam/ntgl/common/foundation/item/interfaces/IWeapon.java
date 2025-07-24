package com.nukateam.ntgl.common.foundation.item.interfaces;

import com.nukateam.geo.interfaces.IResourceProvider;
import com.nukateam.ntgl.common.data.config.gun.Gun;
import com.nukateam.ntgl.common.util.interfaces.IConfigProvider;

public interface IWeapon extends IConfigConsumer<Gun>, IConfigProvider<Gun>, IResourceProvider {

}
