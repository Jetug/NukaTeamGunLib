package com.nukateam.ntgl.common.util.interfaces;

import com.nukateam.ntgl.common.data.config.ProjectileConfig;
import com.nukateam.ntgl.common.foundation.entity.throwable.ThrowableItemEntity;
import com.nukateam.ntgl.common.foundation.item.interfaces.IThrowable;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;

public interface IThrowableProjectileFactory {
    <T extends Item & IThrowable> ThrowableItemEntity create(Level world, LivingEntity entity, T projectile, int timeLeft);
}
