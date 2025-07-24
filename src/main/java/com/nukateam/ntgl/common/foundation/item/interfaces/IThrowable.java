package com.nukateam.ntgl.common.foundation.item.interfaces;

import com.nukateam.ntgl.common.data.config.Projectile;
import com.nukateam.ntgl.common.data.config.ThrowableConfig;
import com.nukateam.ntgl.common.util.interfaces.IGunModifier;
import net.minecraft.nbt.CompoundTag;
import net.minecraftforge.common.util.INBTSerializable;

public interface IThrowable extends IConfigConsumer<ThrowableConfig> {
    ThrowableConfig getConfig();

    boolean isPreparing();

    boolean isThrowing();

    int getPrepareTime();

    int getThrowTime();
}
