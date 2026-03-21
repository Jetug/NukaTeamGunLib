package com.nukateam.ntgl.mixin.ntgl.client;

import com.nukateam.ntgl.common.foundation.entity.ProjectileEntity;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(ProjectileEntity.class)
public interface ProjectileEntityAccessor {
    @Accessor("shooter")
    LivingEntity ntgl$getShooter();
}
