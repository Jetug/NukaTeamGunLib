package com.nukateam.ntgl.common.util.interfaces;

import com.nukateam.ntgl.common.data.WeaponData;
import com.nukateam.ntgl.common.foundation.entity.ProjectileEntity;
import com.nukateam.ntgl.common.util.managers.ProjectileManager;
import net.minecraft.world.level.Level;

public interface IAttackFactory {
    ProjectileEntity create(WeaponData weaponData);
}
