package com.nukateam.ntgl.common.util.interfaces;

import com.nukateam.ntgl.common.data.config.ProjectileConfig;
import com.nukateam.ntgl.common.foundation.entity.ThrowableItemEntity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;

public interface IThrowableProjectileFactory {
    ThrowableItemEntity create(Level world, LivingEntity entity, ProjectileConfig projectile, int timeLeft);
}
