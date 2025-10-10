package com.nukateam.ntgl.common.foundation.components;

import net.minecraft.core.component.DataComponentType;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.codec.ByteBufCodecs;

public class NTGLComponents {
    public static final DataComponentType<CompoundTag> GUNCOMPONENT =
            DataComponentType.<CompoundTag>builder()
                    .persistent(CompoundTag.CODEC)
                    .networkSynchronized(ByteBufCodecs.COMPOUND_TAG)
                    .build();
}
