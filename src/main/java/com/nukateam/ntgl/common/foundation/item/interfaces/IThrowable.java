package com.nukateam.ntgl.common.foundation.item.interfaces;

import com.nukateam.ntgl.common.data.config.ThrowableConfig;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;

public interface IThrowable extends IConfigConsumer<ThrowableConfig> {
    ThrowableConfig getConfig();

    void explode(LivingEntity entityLiving);

    void throwItem(ItemStack stack, LivingEntity entityLiving, int timeLeft);
}
