package com.nukateam.example.common.data.interfaces;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;

public interface IExplosiveOnHit {
    void explodeOnHit(Level pLevel, BlockPos pPos);
}
