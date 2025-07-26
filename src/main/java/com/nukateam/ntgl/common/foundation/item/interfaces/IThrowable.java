package com.nukateam.ntgl.common.foundation.item.interfaces;

import com.nukateam.ntgl.common.data.config.Projectile;
import com.nukateam.ntgl.common.data.config.ThrowableConfig;
import com.nukateam.ntgl.common.util.interfaces.IGunModifier;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.util.INBTSerializable;

public interface IThrowable extends IConfigConsumer<ThrowableConfig> {
    ThrowableConfig getConfig();

    void explode(LivingEntity entityLiving);

    void throwItem(ItemStack stack, LivingEntity entityLiving, int timeLeft);
}
