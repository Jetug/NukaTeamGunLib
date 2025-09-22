package com.nukateam.ntgl.common.foundation.item.interfaces;

import com.nukateam.ntgl.common.data.config.ThrowableConfig;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;

public interface IThrowable extends INtglItem, IConfigConsumer<ThrowableConfig>{
    ThrowableConfig getConfig();

    void expire(LivingEntity entityLiving);

    void throwItem(ItemStack stack, LivingEntity entityLiving, int timeLeft);
}
