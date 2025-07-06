package com.nukateam.ntgl.common.foundation.item.interfaces;

import com.nukateam.ntgl.common.data.config.MeleeWeaponConfig;

public interface IMelee extends IConfigConsumer<MeleeWeaponConfig>{
    MeleeWeaponConfig getConfig();
}
