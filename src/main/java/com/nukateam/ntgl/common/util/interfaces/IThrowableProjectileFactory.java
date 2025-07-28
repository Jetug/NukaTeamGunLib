package com.nukateam.ntgl.common.util.interfaces;

import com.nukateam.ntgl.common.base.utils.managers.ProjectileManager;
import com.nukateam.ntgl.common.data.config.ProjectileConfig;
import com.nukateam.ntgl.common.data.config.gun.Gun;
import com.nukateam.ntgl.common.foundation.entity.ProjectileEntity;
import com.nukateam.ntgl.common.foundation.entity.ThrowableItemEntity;
import com.nukateam.ntgl.common.foundation.item.WeaponItem;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public interface IThrowableProjectileFactory {
    ThrowableItemEntity create(Level world, LivingEntity entity, ProjectileConfig projectile, int timeLeft);
}
