package com.nukateam.ntgl.common.util.interfaces;

import com.nukateam.ntgl.common.foundation.entity.ProjectileEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

/**
 * An interface for notifying a block it has been hit by a projectile.
 */
public interface IDamageable {
    default void onBlockDamaged(ProjectileEntity projectile, BlockState state, BlockPos pos, float damage) {}
}
